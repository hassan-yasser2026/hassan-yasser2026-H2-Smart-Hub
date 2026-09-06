package com.example.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.ChatMessage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private val baseUrl: String = BuildConfig.SERVER_URL.trimEnd('/')

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    // --- 429 (quota) handling helpers ---

    private const val DAILY_QUOTA_MESSAGE =
        "انتهت الحصة المجانية اليومية لمفتاح Gemini. لتشغيل التطبيق بدون حدود يجب تفعيل الفوترة (Billing) للمفتاح من Google AI Studio."
    private const val RATE_LIMIT_MESSAGE =
        "الخادم مشغول مؤقتاً (تم تجاوز حد الطلبات في الدقيقة). انتظر لحظات ثم أعد المحاولة."

    /** True when the 429 is the per-day free-tier quota — retrying is pointless. */
    private fun isDailyQuotaError(body: String): Boolean =
        body.contains("PerDay", ignoreCase = true)

    /** Parses Google's "Please retry in 12.3s" hint from the 429 error body. */
    private fun parseRetryDelaySeconds(body: String): Double? =
        "retry in ([0-9.]+)s".toRegex(RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.toDoubleOrNull()

    /** Executes one POST and returns HTTP code + body. */
    private fun postJson(url: String, bodyJson: JSONObject): Pair<Int, String> {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = bodyJson.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).execute().use { response ->
            return response.code to (response.body?.string() ?: "")
        }
    }

    /**
     * Extracts the answer text from a generateContent response body,
     * skipping "thought" parts produced by thinking models and
     * concatenating all remaining text parts.
     */
    private fun parseCandidateText(bodyStr: String): String? {
        return try {
            val responseJson = JSONObject(bodyStr)
            val candidates = responseJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return null
            val parts = contentObj.optJSONArray("parts") ?: return null
            val builder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.optBoolean("thought", false)) continue
                val text = part.optString("text")
                if (text.isNotEmpty()) builder.append(text)
            }
            builder.toString().trim().takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse candidate text", e)
            null
        }
    }

    /**
     * Generates a chat response from Gemini.
     * Supports thinking mode using gemini-3.1-pro-preview with HIGH thinking level.
     * If the thinking model is unavailable (e.g. quota exhausted), it automatically
     * falls back to gemini-3.5-flash so the user always gets an answer.
     */
    suspend fun generateChatResponse(
        history: List<ChatMessage>,
        systemInstruction: String,
        useThinking: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        if (baseUrl.isEmpty() || baseUrl == "https://YOUR_RAILWAY_DOMAIN") {
            return@withContext "خطأ: لم يتم ضبط رابط الخادم."
        }

        val modelsToTry = if (useThinking) {
            listOf("gemini-3.1-pro-preview", "gemini-3.5-flash")
        } else {
            listOf("gemini-3.5-flash")
        }
        var lastError = "لم نتمكن من الحصول على رد من الذكاء الاصطناعي."

        for ((attemptIndex, model) in modelsToTry.withIndex()) {
            val applyThinkingConfig = useThinking && attemptIndex == 0
            val url = "$baseUrl/v1beta/models/$model:generateContent"
            try {
                val requestBodyJson = JSONObject()

                // Contents array
                val contentsArray = JSONArray()
                history.takeLast(30).forEach { msg ->
                    val contentObj = JSONObject()
                    contentObj.put("role", if (msg.role == "user") "user" else "model")

                    val partsArray = JSONArray()
                    val partObj = JSONObject()
                    partObj.put("text", msg.content)
                    partsArray.put(partObj)

                    contentObj.put("parts", partsArray)
                    contentsArray.put(contentObj)
                }
                requestBodyJson.put("contents", contentsArray)

                // System Instruction
                if (systemInstruction.isNotEmpty()) {
                    val sysInstructObj = JSONObject()
                    val partsArray = JSONArray()
                    val partObj = JSONObject()
                    partObj.put("text", systemInstruction)
                    partsArray.put(partObj)
                    sysInstructObj.put("parts", partsArray)
                    requestBodyJson.put("systemInstruction", sysInstructObj)
                }

                // Generation Config
                // Thinking models spend tokens on reasoning before answering, so the
                // budget must be large enough for both the thoughts and the answer.
                val generationConfig = JSONObject()
                generationConfig.put("maxOutputTokens", 4096)
                if (applyThinkingConfig) {
                    val thinkingConfig = JSONObject()
                    thinkingConfig.put("thinkingLevel", "HIGH")
                    generationConfig.put("thinkingConfig", thinkingConfig)
                }
                requestBodyJson.put("generationConfig", generationConfig)

                // Up to 2 attempts per model: transient per-minute 429s are retried
                // after the wait Google suggests; daily-quota 429s are not retried.
                var attempt = 0
                attemptsLoop@ while (attempt < 2) {
                    attempt++
                    val (code, bodyStr) = postJson(url, requestBodyJson)

                    if (code in 200..299) {
                        val text = parseCandidateText(bodyStr)
                        if (text != null) {
                            return@withContext text
                        }
                        lastError = "لم نتمكن من الحصول على رد من الذكاء الاصطناعي."
                        break@attemptsLoop
                    }

                    Log.e(TAG, "Request failed for $model: Code $code, Body: $bodyStr")
                    if (code == 429) {
                        if (isDailyQuotaError(bodyStr)) {
                            lastError = DAILY_QUOTA_MESSAGE
                            break@attemptsLoop // try fallback model if any
                        }
                        val delaySec = parseRetryDelaySeconds(bodyStr)?.coerceAtMost(25.0) ?: 12.0
                        if (attempt < 2) {
                            kotlinx.coroutines.delay((delaySec * 1000).toLong() + 500)
                            continue@attemptsLoop
                        }
                        lastError = RATE_LIMIT_MESSAGE
                    } else {
                        lastError = "خطأ في الاتصال بالخادم ($code). الرجاء المحاولة لاحقاً."
                    }
                    break@attemptsLoop
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during chat generate ($model)", e)
                lastError = "حدث خطأ غير متوقع: ${e.localizedMessage ?: e.message}"
            }
        }
        return@withContext lastError
    }

    suspend fun generateMultimodalResponse(
        prompt: String,
        systemInstruction: String,
        base64Data: String,
        mimeType: String,
        useThinking: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        if (baseUrl.isEmpty() || baseUrl == "https://YOUR_RAILWAY_DOMAIN") {
            return@withContext "خطأ: لم يتم ضبط رابط الخادم."
        }

        // Determine Model
        val model = if (useThinking) "gemini-3.1-pro-preview" else "gemini-3.5-flash"
        val url = "$baseUrl/v1beta/models/$model:generateContent"

        try {
            val requestBodyJson = JSONObject()

            // Contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")

            val partsArray = JSONArray()
            
            // Text Part
            val textPart = JSONObject()
            textPart.put("text", prompt)
            partsArray.put(textPart)

            // Inline Data Part
            val inlineDataPart = JSONObject()
            val inlineData = JSONObject()
            inlineData.put("mimeType", mimeType)
            inlineData.put("data", base64Data)
            inlineDataPart.put("inlineData", inlineData)
            partsArray.put(inlineDataPart)

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            // System Instruction
            if (systemInstruction.isNotEmpty()) {
                val sysInstructObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstructObj.put("parts", sysPartsArray)
                requestBodyJson.put("systemInstruction", sysInstructObj)
            }

            // Generation Config
            val generationConfig = JSONObject()
            generationConfig.put("maxOutputTokens", 4096)
            if (useThinking) {
                val thinkingConfig = JSONObject()
                thinkingConfig.put("thinkingLevel", "HIGH")
                generationConfig.put("thinkingConfig", thinkingConfig)
            }
            requestBodyJson.put("generationConfig", generationConfig)

            var attempt = 0
            while (attempt < 2) {
                attempt++
                val (code, bodyStr) = postJson(url, requestBodyJson)

                if (code in 200..299) {
                    val text = parseCandidateText(bodyStr)
                    return@withContext text ?: "لم نتمكن من الحصول على رد من الذكاء الاصطناعي."
                }

                Log.e(TAG, "Multimodal request failed: Code $code, Body: $bodyStr")
                if (code == 429) {
                    // Thinking model unavailable (quota/limit) -> retry once with flash
                    if (useThinking) {
                        return@withContext generateMultimodalResponse(
                            prompt = prompt,
                            systemInstruction = systemInstruction,
                            base64Data = base64Data,
                            mimeType = mimeType,
                            useThinking = false
                        )
                    }
                    if (isDailyQuotaError(bodyStr)) {
                        return@withContext DAILY_QUOTA_MESSAGE
                    }
                    val delaySec = parseRetryDelaySeconds(bodyStr)?.coerceAtMost(25.0) ?: 12.0
                    if (attempt < 2) {
                        kotlinx.coroutines.delay((delaySec * 1000).toLong() + 500)
                        continue
                    }
                    return@withContext RATE_LIMIT_MESSAGE
                }
                return@withContext "خطأ في الاتصال بالخادم ($code). الرجاء المحاولة لاحقاً."
            }
            return@withContext "لم نتمكن من الحصول على رد من الذكاء الاصطناعي."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during multimodal generate", e)
            return@withContext "حدث خطأ غير متوقع: ${e.localizedMessage ?: e.message}"
        }
    }

    /**
     * Generates an image using gemini-2.5-flash-image
     */
    suspend fun generateImage(prompt: String, aspectRatio: String = "1:1"): String? = withContext(Dispatchers.IO) {
        if (baseUrl.isEmpty() || baseUrl == "https://YOUR_RAILWAY_DOMAIN") return@withContext null

        val url = "$baseUrl/v1beta/models/gemini-2.5-flash-image:generateContent"

        try {
            val requestBodyJson = JSONObject()

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            val generationConfig = JSONObject()
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", aspectRatio)
            imageConfig.put("imageSize", "1K")
            generationConfig.put("imageConfig", imageConfig)
            generationConfig.put("responseModalities", JSONArray(listOf("TEXT", "IMAGE")))
            requestBodyJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Image generation failed: ${response.code} $errBody")
                    return@withContext null
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            val inlineData = part.optJSONObject("inlineData")
                            if (inlineData != null) {
                                val mime = inlineData.optString("mimeType")
                                if (mime.startsWith("image/")) {
                                    return@withContext inlineData.optString("data")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Image generation error", e)
        }
        return@withContext null
    }

    /**
     * Generates a video using veo-3.1-fast-generate-preview
     */
    suspend fun generateVideo(
        prompt: String,
        imageBase64: String? = null,
        aspectRatio: String = "16:9"
    ): String? = withContext(Dispatchers.IO) {
        if (baseUrl.isEmpty() || baseUrl == "https://YOUR_RAILWAY_DOMAIN") return@withContext null

        val model = "veo-3.1-fast-generate-preview"
        val url = "$baseUrl/v1beta/models/$model:generateVideos"

        try {
            val requestBodyJson = JSONObject()
            requestBodyJson.put("prompt", prompt)

            val config = JSONObject()
            config.put("numberOfVideos", 1)
            config.put("resolution", "1080p")
            config.put("aspectRatio", aspectRatio)
            requestBodyJson.put("config", config)

            // If there's an image input (Image-to-Video / Animate photo)
            if (imageBase64 != null) {
                val imageObj = JSONObject()
                imageObj.put("mimeType", "image/jpeg")
                imageObj.put("data", imageBase64)
                requestBodyJson.put("imageInput", imageObj)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val responseJson = JSONObject(bodyStr)
                    // Veo normally returns an operation name (operations/...)
                    val operationName = responseJson.optString("name")
                    if (operationName.isNotEmpty()) {
                        return@withContext operationName
                    }
                } else {
                    Log.e(TAG, "Veo failed: ${response.code} ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Video generation error", e)
        }
        return@withContext null
    }

    private fun getElevenLabsApiKey(): String {
        return try {
            val key = BuildConfig.ELEVEN_LABS_API_KEY
            if (key.isEmpty() || key == "MY_ELEVEN_LABS_API_KEY") "" else key
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Text to Speech using ElevenLabs API with multilingual v2 model
     */
    suspend fun generateElevenLabsSpeech(text: String, voiceId: String): String? = withContext(Dispatchers.IO) {
        val apiKey = getElevenLabsApiKey()
        if (apiKey.isEmpty()) {
            Log.d(TAG, "ElevenLabs API key is empty. Falling back to Gemini High-Fi speech API.")
            return@withContext null
        }

        val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"
        try {
            val requestBodyJson = JSONObject()
            requestBodyJson.put("text", text)
            requestBodyJson.put("model_id", "eleven_multilingual_v2")

            val voiceSettings = JSONObject()
            voiceSettings.put("stability", 0.35)
            voiceSettings.put("similarity_boost", 0.85)
            voiceSettings.put("style", 0.3)
            requestBodyJson.put("voice_settings", voiceSettings)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null) {
                        Log.i(TAG, "Successfully generated ElevenLabs speech audio payload.")
                        return@withContext Base64.encodeToString(bytes, Base64.NO_WRAP)
                    }
                } else {
                    Log.e(TAG, "ElevenLabs TTS request failed: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ElevenLabs TTS error", e)
        }
        return@withContext null
    }

    /**
     * Text to Speech using gemini-2.5-flash-preview-tts with ElevenLabs high-fidelity voice pre-emption
     */
    suspend fun generateSpeech(text: String, voiceName: String = "Kore"): String? = withContext(Dispatchers.IO) {
        // Pre-emptively try ElevenLabs if the voice is configured for Hasan or Jana and key exists
        val elevenLabsVoiceId = when (voiceName.lowercase()) {
            "hasan", "puck" -> "pNInz6obpgDQGcFmaJgB" // Adam's deep masculine voice ID
            "jana", "aoede" -> "21m00Tcm4TlvDq8ikWAM" // Rachel's sweet feminine voice ID
            else -> null
        }

        if (elevenLabsVoiceId != null) {
            val elevenAudio = generateElevenLabsSpeech(text, elevenLabsVoiceId)
            if (elevenAudio != null) {
                return@withContext elevenAudio
            }
        }

        // Fallback to Gemini High-Fi speech API if ElevenLabs is not set up or rate limited
        if (baseUrl.isEmpty() || baseUrl == "https://YOUR_RAILWAY_DOMAIN") return@withContext null

        val fallbackVoice = when (voiceName.lowercase()) {
            "hasan" -> "Puck"
            "jana" -> "Aoede"
            else -> voiceName
        }

        val url = "$baseUrl/v1beta/models/gemini-2.5-flash-preview-tts:generateContent"

        try {
            val requestBodyJson = JSONObject()

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", text)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            val generationConfig = JSONObject()
            generationConfig.put("responseModalities", JSONArray(listOf("AUDIO")))

            val speechConfig = JSONObject()
            val voiceConfigObj = JSONObject()
            val prebuiltVoiceConfigObj = JSONObject()
            prebuiltVoiceConfigObj.put("voiceName", fallbackVoice)
            voiceConfigObj.put("prebuiltVoiceConfig", prebuiltVoiceConfigObj)
            speechConfig.put("voiceConfig", voiceConfigObj)
            generationConfig.put("speechConfig", speechConfig)

            requestBodyJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val responseJson = JSONObject(bodyStr)
                    val candidates = responseJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val contentObjRes = firstCandidate.optJSONObject("content")
                        val parts = contentObjRes?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val inlineData = parts.getJSONObject(0).optJSONObject("inlineData")
                            if (inlineData != null) {
                                return@withContext inlineData.optString("data") // returns base64 string of the MP3 audio
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS generation error", e)
        }
        return@withContext null
    }
}
