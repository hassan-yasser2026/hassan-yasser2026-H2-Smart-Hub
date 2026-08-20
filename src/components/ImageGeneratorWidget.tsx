import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  Sparkles, Image as ImageIcon, Wand2, ShieldCheck, Download, AlertCircle, 
  HelpCircle, Layout, Palette, Compass, RefreshCw, Crown, CreditCard, ShieldAlert,
  Zap, CheckCircle2
} from "lucide-react";
import { UserAccount } from "../types";

interface ImageGeneratorWidgetProps {
  currentUser: UserAccount;
}

export default function ImageGeneratorWidget({ currentUser }: ImageGeneratorWidgetProps) {
  const [prompt, setPrompt] = useState("");
  const [style, setStyle] = useState("photorealistic");
  const [aspectRatio, setAspectRatio] = useState("1:1");
  const [isLoading, setIsLoading] = useState(false);
  
  const [generatedUrl, setGeneratedUrl] = useState<string | null>(null);
  const [isFallback, setIsFallback] = useState(false);
  const [feedbackMsg, setFeedbackMsg] = useState<string | null>(null);
  const [safetyNotice, setSafetyNotice] = useState<string | null>(null);

  // Monetization State for Image Generation
  const [imagesUsed, setImagesUsed] = useState(0);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  // Load user's images count from localStorage
  useEffect(() => {
    const key = `hj_images_count_${currentUser.username}`;
    const stored = localStorage.getItem(key);
    if (stored) {
      setImagesUsed(parseInt(stored, 10));
    } else {
      setImagesUsed(0);
    }
  }, [currentUser]);

  // Style presets list
  const stylePresets = [
    { id: "photorealistic", nameAr: "واقعية فائقة", icon: "📷", desc: "إضاءة واقعية مذهلة وتفاصيل دقيقة" },
    { id: "3d_render", nameAr: "رسم ثلاثي الأبعاد", icon: "🧱", desc: "أبعاد ثلاثية وعمق مجسم حديث" },
    { id: "watercolor", nameAr: "ألوان مائية فنية", icon: "🎨", desc: "لمسة رسم مائي ناعم وألوان مدمجة" },
    { id: "minimalist_vector", nameAr: "فيكتور مبسط", icon: "📐", desc: "تصميم فيكتور حديث وشعارات مسطحة" },
    { id: "digital_fantasy", nameAr: "خيالي / فانتاسي", icon: "🌌", desc: "عوالم خيالية ساحرة وألوان سريالية" },
    { id: "pixel_art", nameAr: "بيكسل آرت (ألعاب)", icon: "👾", desc: "رسوم ألعاب ريترو بيكسل كلاسيكية" }
  ];

  // Aspect ratio presets
  const ratioPresets = [
    { id: "1:1", label: "مربع (1:1)", desc: "وسائل التواصل والرمزيات" },
    { id: "16:9", label: "عريض (16:9)", desc: "خلفيات وحواسيب" },
    { id: "9:16", label: "طولي (9:16)", desc: "ستوري وموبايل" },
    { id: "4:3", label: "كلاسيك (4:3)", desc: "بطاقات مصغرة" }
  ];

  // Local safety and NSFW client-side check
  const performLocalSafetyCheck = (input: string): boolean => {
    const normalized = input.toLowerCase();
    const prohibited = [
      "naked", "nudity", "nsfw", "sexy", "erotic", "porn", "porno", "sex", "unclothed", "vagina", "penis", "breast", "boobs", "ass", "striptease", "vulg", "عاري", "جنس", "إباحي", "ثدي", "مؤخرة", "عرى", "مفصخ"
    ];
    for (const word of prohibited) {
      if (normalized.includes(word)) {
        return false;
      }
    }
    return true;
  };

  const handleGenerateImage = async () => {
    if (!prompt.trim() || isLoading) return;

    // Check images count for non-VIP
    if (!currentUser.isVIP && imagesUsed >= 5) {
      setShowUpgradeModal(true);
      return;
    }

    setSafetyNotice(null);
    setFeedbackMsg(null);
    setIsFallback(false);

    // Client-side Safety NSFW prompt checking
    if (!performLocalSafetyCheck(prompt)) {
      setSafetyNotice("⚠️ تم حظر طلب توليد الصورة تلقائياً نظراً لاحتواء الوصف على كلمات غير ملائمة أو خادشة للحياء العام. يرجى تعديل الوصف ليتناسب مع أخلاقيات العمل المنشأة.");
      return;
    }

    setIsLoading(true);

    try {
      const res = await fetch("/api/image/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          prompt,
          aspectRatio,
          style
        })
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.error || "Failed to generate image.");
      }

      setGeneratedUrl(data.imageUrl);
      
      // Increment and save image generation count
      const nextCount = imagesUsed + 1;
      setImagesUsed(nextCount);
      localStorage.setItem(`hj_images_count_${currentUser.username}`, nextCount.toString());

      if (data.isFallback) {
        setIsFallback(true);
        setFeedbackMsg(data.fallbackExplanation);
      } else {
        setFeedbackMsg("تم توليد صورتك بنجاح بنسبة 100% عبر خوارزميات الذكاء الاصطناعي.");
      }

    } catch (e: any) {
      console.error(e);
      setSafetyNotice(`⚠️ خطأ: ${e.message || "حدث مشكلة غير متوقعة أثناء معالجة الصورة."}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-neutral-900/60 rounded-2xl border border-neutral-800 p-6 backdrop-blur-md shadow-xl flex flex-col gap-6">
      
      {/* Header */}
      <div className="border-b border-neutral-800 pb-4 flex flex-col sm:flex-row items-center justify-between gap-4 text-right">
        
        {/* Status display */}
        <div className="flex items-center gap-2">
          {currentUser.isVIP ? (
            <span className="bg-gradient-to-r from-amber-500 to-yellow-500 text-black font-bold text-[10px] px-3 py-1 rounded-full flex items-center gap-1.5 shadow-md shadow-amber-500/10">
              <Crown className="h-3 w-3 fill-black" />
              <span>اشتراك VIP غير محدود مجاناً</span>
            </span>
          ) : (
            <button 
              onClick={() => setShowUpgradeModal(true)}
              className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-[10px] px-3 py-1 rounded-full flex items-center gap-1.5 transition-all"
            >
              <Zap className="h-3 w-3 text-yellow-300" />
              <span>ترقية للـ VIP (200 ج.م)</span>
            </button>
          )}

          <span className="bg-neutral-950 border border-neutral-800 text-neutral-400 text-[10px] px-2.5 py-1 rounded-full font-mono">
            الصور المستخدمة اليوم: {imagesUsed} / 5
          </span>
        </div>

        <div className="text-right">
          <h2 className="text-xl font-display font-medium text-white flex items-center justify-end gap-2">
            <Wand2 className="h-5 w-5 text-indigo-400" />
            توليد وتعديل الصور بالذكاء الاصطناعي (AI Image Suite)
          </h2>
          <p className="text-xs text-neutral-400 mt-1">
            أطلق العنان لخيالك الفني وقم بتوليد لوحات وشعارات احترافية مع نظام فلترة أمان فائق ضد الصور غير اللائقة.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        
        {/* Controls Column */}
        <div className="lg:col-span-2 space-y-4 text-right">
          
          {/* Prompt input */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono text-neutral-400 tracking-wider flex items-center justify-end gap-1">
              <span>أوصف الصورة بالتفصيل (يدعم عربي وإنجليزي):</span>
              <Compass className="h-3.5 w-3.5 text-neutral-500" />
            </label>
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="مثال: رائد فضاء عربي يقرأ كتاباً فوق سطح القمر محاطاً بهالة نيون زرقاء برّاقة..."
              rows={3}
              className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-3.5 text-sm text-neutral-200 focus:outline-none focus:border-indigo-500 placeholder:text-neutral-600 text-right dir-rtl"
            />
          </div>

          {/* Aspect Ratio picker */}
          <div className="space-y-2">
            <span className="text-xs text-neutral-400 flex items-center justify-end gap-1">
              <span>أبعاد الصورة (Aspect Ratio):</span>
              <Layout className="h-3.5 w-3.5 text-neutral-500" />
            </span>
            <div className="grid grid-cols-2 gap-2">
              {ratioPresets.map(r => (
                <button
                  key={r.id}
                  onClick={() => setAspectRatio(r.id)}
                  className={`p-2.5 rounded-xl border text-right transition-all ${
                    aspectRatio === r.id 
                      ? "bg-indigo-600/10 border-indigo-500 text-white shadow" 
                      : "bg-neutral-950 border-neutral-800/80 text-neutral-400 hover:border-neutral-700"
                  }`}
                >
                  <p className="text-xs font-semibold leading-none">{r.label}</p>
                  <p className="text-[9px] text-neutral-500 mt-1 leading-none">{r.desc}</p>
                </button>
              ))}
            </div>
          </div>

          {/* Styles grid */}
          <div className="space-y-2">
            <span className="text-xs text-neutral-400 flex items-center justify-end gap-1">
              <span>النمط أو الطراز الفني (Style Preset):</span>
              <Palette className="h-3.5 w-3.5 text-neutral-500" />
            </span>
            <div className="grid grid-cols-2 gap-2 max-h-[160px] overflow-y-auto pr-1">
              {stylePresets.map(s => (
                <button
                  key={s.id}
                  onClick={() => setStyle(s.id)}
                  className={`p-2 rounded-xl border text-right transition-all flex items-center justify-between gap-1.5 ${
                    style === s.id 
                      ? "bg-indigo-600/15 border-indigo-500 text-white" 
                      : "bg-neutral-950 border-neutral-800/80 text-neutral-400 hover:border-neutral-700"
                  }`}
                >
                  <span className="text-xs text-neutral-500 truncate font-mono">{s.desc.slice(0, 16)}</span>
                  <div className="flex items-center gap-1.5 overflow-hidden">
                    <span className="text-xs font-medium truncate">{s.nameAr}</span>
                    <span className="text-base">{s.icon}</span>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Generate Action Button */}
          <button
            onClick={handleGenerateImage}
            disabled={isLoading || !prompt.trim()}
            className="w-full bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 disabled:from-neutral-800 disabled:to-neutral-800 disabled:text-neutral-500 text-white font-medium text-xs py-3.5 rounded-xl transition shadow-lg flex items-center justify-center gap-2"
          >
            {isLoading ? (
              <>
                <RefreshCw className="h-4 w-4 animate-spin" />
                <span>جاري الرسم والفلترة...</span>
              </>
            ) : (
              <>
                <Sparkles className="h-4 w-4" />
                <span>رسم الصورة الآن</span>
              </>
            )}
          </button>
        </div>

        {/* Output Screen Column */}
        <div className="lg:col-span-3 bg-neutral-950/40 border border-neutral-800/80 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[340px] relative overflow-hidden group">
          
          {/* Safety warnings or system alerts */}
          {safetyNotice && (
            <div className="absolute top-4 left-4 right-4 z-20 bg-neutral-900 border border-rose-500/30 p-3.5 rounded-xl flex items-start gap-2.5 text-xs text-rose-300 shadow-xl text-right dir-rtl">
              <AlertCircle className="h-4 w-4 text-rose-400 flex-shrink-0 mt-0.5" />
              <span>{safetyNotice}</span>
            </div>
          )}

          {/* Active generated image viewer */}
          {generatedUrl ? (
            <div className="w-full h-full flex flex-col items-center justify-center relative">
              
              {/* Image Frame */}
              <div className={`relative overflow-hidden rounded-xl border border-neutral-800 shadow-2xl max-w-[85%] ${
                aspectRatio === "16:9" ? "aspect-video w-full" : 
                aspectRatio === "9:16" ? "aspect-[9/16] max-h-[300px]" :
                aspectRatio === "4:3" ? "aspect-[4/3] w-[80%]" : "aspect-square w-[70%]"
              }`}>
                <img 
                  src={generatedUrl} 
                  alt={prompt}
                  referrerPolicy="no-referrer"
                  className="w-full h-full object-cover transition duration-500" 
                />
                
                {/* Fallback Watermark */}
                {isFallback && (
                  <div className="absolute bottom-2 left-2 bg-black/80 border border-neutral-800 backdrop-blur px-2 py-0.5 rounded text-[8px] font-mono text-neutral-400">
                    Artistic Stock Fallback Active
                  </div>
                )}
              </div>

              {/* Status / Notice details */}
              {feedbackMsg && (
                <div className="mt-4 p-3 bg-neutral-900 border border-neutral-800/80 rounded-xl text-[10px] font-mono text-neutral-400 text-center max-w-[90%]">
                  {feedbackMsg}
                </div>
              )}

              {/* Action bar below generated image */}
              <div className="mt-4 flex gap-3">
                <a 
                  href={generatedUrl} 
                  download={`hj_smart_hub_${Date.now()}.png`}
                  className="px-4 py-1.5 rounded-lg bg-neutral-900 border border-neutral-800 hover:border-neutral-700 text-[10px] font-mono text-neutral-300 flex items-center gap-1.5 transition-all"
                  title="تحميل الصورة بجودة عالية"
                  target="_blank"
                  rel="noreferrer"
                >
                  <Download className="h-3.5 w-3.5" />
                  <span>تحميل الصورة</span>
                </a>
                <button
                  onClick={() => {
                    setPrompt("");
                    setGeneratedUrl(null);
                    setIsFallback(false);
                    setFeedbackMsg(null);
                  }}
                  className="px-4 py-1.5 rounded-lg bg-neutral-900/50 border border-neutral-800/40 hover:border-neutral-800 text-[10px] font-mono text-neutral-500 hover:text-neutral-300 transition-all"
                >
                  مسح وإعادة البدء
                </button>
              </div>

            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-20 text-center text-neutral-600">
              <ImageIcon className="h-14 w-14 text-neutral-800 mb-2.5 animate-pulse" />
              <p className="text-xs font-semibold text-neutral-400">استوديو تخليق الصور بالذكاء الاصطناعي</p>
              <p className="text-[10px] text-neutral-500 mt-1 max-w-[320px]">
                أدخل الوصف المناسب للصورة واختر الأبعاد المفضلة، وسيقوم نظام المنصة بتوليد لوحتك الفنية في بضع ثوانٍ.
              </p>
              <div className="mt-4 flex items-center gap-1 text-[9px] font-mono text-neutral-500 bg-neutral-900/60 border border-neutral-800/40 px-2.5 py-1 rounded">
                <ShieldCheck className="h-3 w-3 text-emerald-500" />
                <span>NSFW Filter enabled automatically</span>
              </div>
            </div>
          )}

        </div>

      </div>

      {/* Modern, high-fidelity Paymob-inspired VIP & Checkout Overlay */}
      <AnimatePresence>
        {showUpgradeModal && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-neutral-950/90 backdrop-blur-md z-50 flex items-center justify-center p-4 overflow-y-auto"
          >
            <motion.div 
              initial={{ scale: 0.95, y: 15 }}
              animate={{ scale: 1, y: 0 }}
              exit={{ scale: 0.95, y: -15 }}
              className="bg-neutral-900 border border-neutral-800 rounded-2xl w-full max-w-xl overflow-hidden shadow-2xl relative"
            >
              
              {/* Success Screen */}
              {paymentSuccess ? (
                <div className="p-10 flex flex-col items-center justify-center text-center gap-4">
                  <div className="h-16 w-16 bg-emerald-500/20 border border-emerald-500/40 rounded-full flex items-center justify-center text-emerald-400 mb-2">
                    <CheckCircle2 className="h-10 w-10 animate-bounce" />
                  </div>
                  <h3 className="text-xl font-bold text-white">تمت عملية الدفع بأمان وسرعة!</h3>
                  <p className="text-xs text-neutral-400 max-w-xs">
                    شكراً لك! تم استلام دفعتك بأمان عبر بوابة الدفع الآمنة المعتمدة. جاري تفعيل اشتراك الـ VIP اللانهائي لحسابك...
                  </p>
                  <div className="h-1.5 w-full max-w-[200px] bg-neutral-950 rounded-full overflow-hidden mt-4">
                    <div className="bg-emerald-500 h-full animate-pulse" style={{ width: "100%" }} />
                  </div>
                </div>
              ) : (
                <div>
                  
                  {/* Modal Header */}
                  <div className="p-5 border-b border-neutral-800 bg-neutral-950/40 flex items-center justify-between gap-4">
                    <button 
                      onClick={() => {
                        setShowUpgradeModal(false);
                      }}
                      className="text-xs text-neutral-500 hover:text-white px-2.5 py-1 rounded-lg bg-neutral-900 border border-neutral-800"
                    >
                      إلغاء
                    </button>
                    <div className="text-right">
                      <h3 className="text-base font-bold text-white flex items-center justify-end gap-1.5">
                        <span>ترقية الحساب وبوابة الدفع الآمنة</span>
                        <Crown className="h-4 w-4 text-amber-400" />
                      </h3>
                      <p className="text-[10px] text-neutral-400 mt-0.5">بوابة دفع مشفرة 100% متوافقة مع فودافون كاش وبطاقات الدفع</p>
                    </div>
                  </div>

                  {/* Pricing Panel */}
                  <div className="p-6 space-y-6 text-right">
                    
                    {/* Notice board */}
                    <div className="bg-indigo-950/40 border border-indigo-500/30 rounded-xl p-4 flex items-start gap-3">
                      <ShieldCheck className="h-5 w-5 text-indigo-400 flex-shrink-0 mt-0.5" />
                      <div>
                        <h4 className="text-xs font-bold text-indigo-200">لقد وصلت للحد الأقصى اليومي لتوليد الصور</h4>
                        <p className="text-[11px] text-neutral-400 mt-1 leading-relaxed">
                          يسمح النظام بـ 5 صور مجانية يومياً للمستخدمين العاديين. لتوليد المزيد من الصور فائقة الدقة والوصول لميزات صناعة الفيديوهات اللانهائية، قم بالاشتراك الآن في باقة VIP.
                        </p>
                      </div>
                    </div>

                    {/* Pricing Cards */}
                    <div className="bg-neutral-950 p-5 rounded-2xl border border-neutral-800 flex flex-col items-center justify-between gap-4 text-center">
                      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
                        <Crown className="h-6 w-6 fill-amber-500/10" />
                      </div>
                      <div>
                        <h4 className="text-sm font-bold text-white">باقة VIP اللانهائية الشاملة</h4>
                        <p className="text-xs text-neutral-400 mt-1 max-w-sm">
                          تفتح وصولاً غير محدود لكل من الصور (بدون سقف الـ 5 اليومي) ومقاطع الفيديو مجاناً طوال الشهر!
                        </p>
                      </div>
                      <div className="flex items-baseline gap-1 justify-center w-full pt-3 border-t border-neutral-900">
                        <span className="text-2xl font-bold text-amber-400">200</span>
                        <span className="text-xs text-neutral-500">جنيه مصري / شهرياً</span>
                      </div>
                    </div>

                    {/* Paymob integration display */}
                    <div className="border-t border-neutral-800 pt-5 space-y-4">
                      <div className="flex items-center justify-between gap-2 text-xs">
                        <span className="text-neutral-500 font-mono">Secured & Encrypted via Paymob SSL</span>
                        <span className="text-neutral-300 font-semibold">بوابة الدفع الإلكتروني المعتمدة</span>
                      </div>
                      
                      <div className="grid grid-cols-2 gap-2 text-center text-xs">
                        <div className="bg-neutral-950 p-2.5 rounded-xl border border-neutral-800 flex items-center justify-center gap-1.5 text-neutral-300">
                          <span>فودافون كاش (Vodafone)</span>
                        </div>
                        <div className="bg-neutral-950 p-2.5 rounded-xl border border-neutral-800 flex items-center justify-center gap-1.5 text-neutral-300">
                          <span>فوري ومحافظ ذكية (Fawry)</span>
                        </div>
                      </div>

                      {/* Pay Trigger Button */}
                      <button
                        onClick={() => {
                          setIsLoading(true);
                          setTimeout(() => {
                            setIsLoading(false);
                            setPaymentSuccess(true);
                            setTimeout(() => {
                              setPaymentSuccess(false);
                              setShowUpgradeModal(false);
                              currentUser.isVIP = true;
                            }, 2000);
                          }, 1500);
                        }}
                        disabled={isLoading}
                        className="w-full bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold text-xs py-3.5 rounded-xl transition shadow-xl flex items-center justify-center gap-2"
                      >
                        {isLoading ? (
                          <>
                            <RefreshCw className="h-4 w-4 animate-spin" />
                            <span>جاري الاتصال بنقاط الدفع الآمنة...</span>
                          </>
                        ) : (
                          <>
                            <CreditCard className="h-4 w-4" />
                            <span>تأكيد الدفع والترقية الفورية الآن</span>
                          </>
                        )}
                      </button>

                    </div>

                  </div>

                </div>
              )}

            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

    </div>
  );
}
