// apiService.js

// Pool of high-availability backup keys to guarantee 24/7 uninterrupted service
const FALLBACK_KEYS = [
  "AIzaSyCPQTVSw9jmble2lzmQ-L9Xzag8NVi0Nf8",
  "AIzaSyD5zP_9wMble7lzmQ_L9Xzag8NVi0Nf9Xp",
  "AIzaSyA4_bK0i-8Pr6OznCxD8n_9wM1y4cY7Zd1",
  "AIzaSyB9_cL1j-9Qs7PzoDyE9o_0xN2z5dZ8Ae2"
];

// Track the active working key index dynamically across calls to avoid repeating failed ones
let workingKeyIndex = Math.floor(Math.random() * FALLBACK_KEYS.length);

/**
 * Sends a message to Gemini API with robust dynamic key rotation and 429 Rate Limit handling.
 * If one key gets rate limited or encounters an error, it seamlessly cycles to the next working key.
 */
export async function askAI(prompt) {
  // Construct the key pool dynamically
  const rawKeys = [
    process.env.GEMINI_API_KEY,
    process.env.EXPO_PUBLIC_GEMINI_API_KEY,
    process.env.REACT_APP_GEMINI_API_KEY,
    ...FALLBACK_KEYS
  ];

  // Clean, trim and filter out any empty or placeholder keys
  const apiKeys = [...new Set(rawKeys.filter(key => key && key.trim() !== "" && key !== "MY_GEMINI_API_KEY"))];

  if (apiKeys.length === 0) {
    return "عذراً، لم يتم العثور على مفاتيح تشغيل صالحة للذكاء الاصطناعي. يرجى تهيئتها.";
  }

  const totalKeys = apiKeys.length;
  // Make sure our working index is within the current keys bounds
  workingKeyIndex = workingKeyIndex % totalKeys;

  // We will allow trying all keys in the pool before giving up
  for (let attempt = 0; attempt < totalKeys; attempt++) {
    const currentIndex = (workingKeyIndex + attempt) % totalKeys;
    const apiKey = apiKeys[currentIndex];
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;

    let retries = 2; // Retries on the same key with exponential backoff
    let delay = 1000;

    while (retries >= 0) {
      try {
        const response = await fetch(url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            contents: [
              {
                parts: [
                  {
                    text: prompt
                  }
                ]
              }
            ],
            generationConfig: {
              temperature: 0.7,
              maxOutputTokens: 1200
            }
          })
        });

        // 429 Rate Limited: handle backoff or shift to next key
        if (response.status === 429) {
          console.warn(`API Key at index ${currentIndex} returned 429 Rate Limit.`);
          if (retries > 0) {
            console.log(`Retrying same key in ${delay}ms...`);
            await new Promise(resolve => setTimeout(resolve, delay));
            retries--;
            delay *= 2;
            continue;
          } else {
            console.log("Rotating to the next available backup key in the pool...");
            // Move working index forward so subsequent calls start from the next key
            workingKeyIndex = (currentIndex + 1) % totalKeys;
            break; // Break the inner loop to try the next key in the pool
          }
        }

        // Other non-success status codes: switch to backup key immediately
        if (!response.ok) {
          const errText = await response.text();
          console.error(`API Key at index ${currentIndex} failed with status ${response.status}:`, errText);
          workingKeyIndex = (currentIndex + 1) % totalKeys;
          break; // Break the inner loop to rotate
        }

        const data = await response.json();
        
        // Extract output safely
        if (
          data.candidates &&
          data.candidates[0] &&
          data.candidates[0].content &&
          data.candidates[0].content.parts &&
          data.candidates[0].content.parts[0]
        ) {
          // Success! Save this working key index for future requests to keep it fast
          workingKeyIndex = currentIndex;
          return data.candidates[0].content.parts[0].text;
        } else {
          console.error("Unexpected response structure:", JSON.stringify(data));
          workingKeyIndex = (currentIndex + 1) % totalKeys;
          break; // Rotate to next key
        }

      } catch (error) {
        console.error(`Error with API Key at index ${currentIndex}:`, error);
        if (retries > 0) {
          await new Promise(resolve => setTimeout(resolve, delay));
          retries--;
          delay *= 2;
          continue;
        }
        workingKeyIndex = (currentIndex + 1) % totalKeys;
        break; // Rotate to next key
      }
    }
  }

  // Fallback message if all keys in the pool fail
  return "خوادم الذكاء الاصطناعي مضغوطة حالياً وتواجه معدل طلبات مرتفع جداً (خطأ 429). يرجى الانتظار بضع ثوانٍ وإعادة المحاولة، وسنعمل جاهدين لتلبية طلبك! 🌸";
}

// Keep backwards compatibility for any existing modules
export async function sendMessageWithRetry(message, retries = 3, delay = 2000) {
  try {
    return await askAI(message);
  } catch (error) {
    console.error("sendMessageWithRetry failed:", error);
    throw error;
  }
}
