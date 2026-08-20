import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  Video, Sparkles, AlertCircle, Play, Download, CreditCard, ShieldCheck, CheckCircle2,
  RefreshCw, Film, Volume2, ShieldAlert, Crown, ArrowLeft, Coins, Check, Zap
} from "lucide-react";
import { UserAccount } from "../types";

interface AIVideoWidgetProps {
  currentUser: UserAccount;
}

export default function AIVideoWidget({ currentUser }: AIVideoWidgetProps) {
  const [prompt, setPrompt] = useState("");
  const [style, setStyle] = useState("cinematic");
  const [aspectRatio, setAspectRatio] = useState("16:9");
  const [motionSpeed, setMotionSpeed] = useState("medium");
  const [cameraMovement, setCameraMovement] = useState("zoom-in");
  
  const [isLoading, setIsLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [generatedVideoUrl, setGeneratedVideoUrl] = useState<string | null>(null);
  
  // Monetization State
  const [videosCount, setVideosCount] = useState<number>(0);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);
  const [checkoutType, setCheckoutType] = useState<"video" | "vip" | null>(null);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  // Load user's video generation count from localStorage
  useEffect(() => {
    const key = `hj_videos_count_${currentUser.username}`;
    const stored = localStorage.getItem(key);
    if (stored) {
      setVideosCount(parseInt(stored, 10));
    } else {
      setVideosCount(0);
    }
  }, [currentUser]);

  // Save video generation count helper
  const incrementVideoCount = () => {
    const nextCount = videosCount + 1;
    setVideosCount(nextCount);
    localStorage.setItem(`hj_videos_count_${currentUser.username}`, nextCount.toString());
  };

  // Video style presets
  const stylePresets = [
    { id: "cinematic", nameAr: "سينمائي ثلاثي الأبعاد", icon: "🎬", desc: "إضاءة هوليوود وتفاصيل درامية" },
    { id: "anime", nameAr: "أنمي / كارتون", icon: "🌸", desc: "أسلوب رسم ياباني بألوان حيوية" },
    { id: "documentary", nameAr: "وثائقي واقعي", icon: "📹", desc: "شكل الكاميرات الوثائقية والواقعية" },
    { id: "cyberpunk", nameAr: "مستقبلي / نيون", icon: "🌆", desc: "أضواء نيون سايبربانك وخيال علمي" },
    { id: "watercolor", nameAr: "ألوان مائية متحركة", icon: "🎨", desc: "ضربات فرشاة فنية وألوان سائلة" }
  ];

  // Camera movements
  const cameraMovements = [
    { id: "zoom-in", label: "تقريب ببطء (Zoom In)" },
    { id: "zoom-out", label: "ابتعاد ببطء (Zoom Out)" },
    { id: "pan-right", label: "تحريك لليمين (Pan Right)" },
    { id: "tilt-up", label: "تحريك للأعلى (Tilt Up)" },
    { id: "static", label: "ثابت (Static Studio)" }
  ];

  // Verification helper for free / pay requirements
  const isGenerationAllowed = () => {
    if (currentUser.isVIP) return true;
    if (videosCount < 1) return true; // First video is free
    return false;
  };

  const handleGenerateVideo = () => {
    if (!prompt.trim() || isLoading) return;

    // Check monetization limits
    if (!isGenerationAllowed()) {
      setCheckoutType("video");
      setShowUpgradeModal(true);
      return;
    }

    setIsLoading(true);
    setProgress(0);
    setGeneratedVideoUrl(null);

    // Dynamic rendering progress simulation
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          return 100;
        }
        return prev + 5;
      });
    }, 250);

    // Simulate video generation finishing
    setTimeout(() => {
      // Standard scenic beautiful high-fidelity videos (looping canvas or MP4 fallback)
      setGeneratedVideoUrl("https://assets.mixkit.co/videos/preview/mixkit-stars-in-space-background-1611-large.mp4");
      setIsLoading(false);
      incrementVideoCount();
    }, 5500);
  };

  // Simulate payment completion
  const handleSimulatePayment = () => {
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      setPaymentSuccess(true);
      setTimeout(() => {
        if (checkoutType === "vip") {
          // Upgrade current user to VIP
          currentUser.isVIP = true;
        } else {
          // Give one free video slot by resetting count to 0 or reducing
          setVideosCount(0); // allows next video free
        }
        setPaymentSuccess(false);
        setShowUpgradeModal(false);
        setCheckoutType(null);
      }, 2000);
    }, 1500);
  };

  return (
    <div className="bg-neutral-900/60 rounded-2xl border border-neutral-800 p-6 backdrop-blur-md shadow-xl flex flex-col gap-6">
      
      {/* Top Header */}
      <div className="border-b border-neutral-800 pb-4 flex flex-col sm:flex-row items-center justify-between gap-4 text-right">
        
        {/* VIP Status badge */}
        <div className="flex items-center gap-2">
          {currentUser.isVIP ? (
            <span className="bg-gradient-to-r from-amber-500 to-yellow-500 text-black font-bold text-[10px] px-3 py-1 rounded-full flex items-center gap-1.5 shadow-md shadow-amber-500/10">
              <Crown className="h-3 w-3 fill-black" />
              <span>اشتراك VIP غير محدود مجاناً</span>
            </span>
          ) : (
            <button 
              onClick={() => {
                setCheckoutType("vip");
                setShowUpgradeModal(true);
              }}
              className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-[10px] px-3 py-1 rounded-full flex items-center gap-1.5 transition-all"
            >
              <Zap className="h-3 w-3 text-yellow-300" />
              <span>ترقية للـ VIP (200 ج.م)</span>
            </button>
          )}

          {/* Credits info */}
          <span className="bg-neutral-950 border border-neutral-800 text-neutral-400 text-[10px] px-2.5 py-1 rounded-full font-mono">
            الفيديوهات المصنوعة: {videosCount} {(!currentUser.isVIP && videosCount >= 1) && "• انتهت الميزات المجانية"}
          </span>
        </div>

        <div className="text-right">
          <h2 className="text-xl font-display font-medium text-white flex items-center justify-end gap-2">
            <Video className="h-5 w-5 text-indigo-400" />
            صناعة الفيديوهات بالذكاء الاصطناعي (AI Video Studio)
          </h2>
          <p className="text-xs text-neutral-400 mt-1">
            حوّل أي فكرة نصية إلى مقاطع فيديو ومؤثرات بصرية متحركة واحترافية في ثوانٍ معدودة.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        
        {/* Settings Column */}
        <div className="lg:col-span-2 space-y-4 text-right">
          
          {/* Prompt description */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono text-neutral-400 tracking-wider flex items-center justify-end gap-1">
              <span>أوصف مشهد الفيديو بالتفصيل:</span>
              <Film className="h-3.5 w-3.5 text-neutral-500" />
            </label>
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="مثال: غروب شمس سينمائي فوق جبال الهيمالايا مع غيوم متحركة ببطء ونهر يتدفق في الوادي..."
              rows={3}
              className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-3.5 text-sm text-neutral-200 focus:outline-none focus:border-indigo-500 placeholder:text-neutral-600 text-right dir-rtl"
            />
          </div>

          {/* Aspect ratio selector */}
          <div className="space-y-2">
            <span className="text-xs text-neutral-400">أبعاد الفيديو المفضل:</span>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => setAspectRatio("16:9")}
                className={`p-2.5 rounded-xl border text-right transition-all ${
                  aspectRatio === "16:9" 
                    ? "bg-indigo-600/10 border-indigo-500 text-white" 
                    : "bg-neutral-950 border-neutral-800/80 text-neutral-400"
                }`}
              >
                <p className="text-xs font-semibold">عريض (16:9)</p>
                <p className="text-[9px] text-neutral-500 mt-0.5">يوتيوب وشاشات ذكية</p>
              </button>
              <button
                onClick={() => setAspectRatio("9:16")}
                className={`p-2.5 rounded-xl border text-right transition-all ${
                  aspectRatio === "9:16" 
                    ? "bg-indigo-600/10 border-indigo-500 text-white" 
                    : "bg-neutral-950 border-neutral-800/80 text-neutral-400"
                }`}
              >
                <p className="text-xs font-semibold">طولي (9:16)</p>
                <p className="text-[9px] text-neutral-500 mt-0.5">ستوري وتيك توك</p>
              </button>
            </div>
          </div>

          {/* Camera Motion Selection */}
          <div className="space-y-1.5">
            <label className="text-xs text-neutral-400">حركة الكاميرا (Camera Motion):</label>
            <select
              value={cameraMovement}
              onChange={(e) => setCameraMovement(e.target.value)}
              className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-3 text-xs text-neutral-300 focus:outline-none focus:border-indigo-500 text-right dir-rtl"
            >
              {cameraMovements.map(m => (
                <option key={m.id} value={m.id}>{m.label}</option>
              ))}
            </select>
          </div>

          {/* Style Presets Grid */}
          <div className="space-y-2">
            <span className="text-xs text-neutral-400">النمط الفني للمشهد:</span>
            <div className="grid grid-cols-2 gap-2 max-h-[140px] overflow-y-auto pr-1">
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
                  <span className="text-[10px] text-neutral-500 truncate">{s.desc.slice(0, 16)}</span>
                  <div className="flex items-center gap-1 overflow-hidden">
                    <span className="text-xs font-medium truncate">{s.nameAr}</span>
                    <span>{s.icon}</span>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Action Button */}
          <button
            onClick={handleGenerateVideo}
            disabled={isLoading || !prompt.trim()}
            className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 disabled:from-neutral-800 disabled:to-neutral-800 disabled:text-neutral-500 text-white font-medium text-xs py-3.5 rounded-xl transition shadow-lg flex items-center justify-center gap-2"
          >
            {isLoading ? (
              <>
                <RefreshCw className="h-4 w-4 animate-spin" />
                <span>جاري معالجة وتوليد كادرات الفيديو...</span>
              </>
            ) : (
              <>
                <Video className="h-4 w-4" />
                <span>توليد وصناعة الفيديو الآن</span>
              </>
            )}
          </button>
          
          {/* Limitation details text */}
          {!currentUser.isVIP && (
            <p className="text-[10px] text-center text-neutral-500 mt-1">
              أول فيديو مجاني بالكامل • الفيديوهات اللاحقة بقيمة <span className="text-indigo-400 font-bold">50 جنيه</span> للمقطع
            </p>
          )}

        </div>

        {/* Video Output Column */}
        <div className="lg:col-span-3 bg-neutral-950/40 border border-neutral-800/80 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[360px] relative overflow-hidden">
          
          {isLoading ? (
            <div className="w-full flex flex-col items-center justify-center text-center p-8">
              <Film className="h-12 w-12 text-indigo-500 animate-spin mb-4" />
              <p className="text-sm font-semibold text-white">جاري إنشاء الفيديو بالذكاء الاصطناعي</p>
              <p className="text-xs text-neutral-400 mt-1 max-w-[320px]">
                نقوم الآن ببناء الكادرات الفنية، تقدير الضوء، وتحريك الكاميرا {cameraMovements.find(c => c.id === cameraMovement)?.label}..
              </p>
              
              {/* Progress bar container */}
              <div className="w-full max-w-xs bg-neutral-900 border border-neutral-800 h-2.5 rounded-full overflow-hidden mt-6">
                <motion.div 
                  className="bg-indigo-500 h-full"
                  initial={{ width: "0%" }}
                  animate={{ width: `${progress}%` }}
                  transition={{ duration: 0.2 }}
                />
              </div>
              <span className="text-xs font-mono mt-2 text-neutral-500">{progress}% مكتمل</span>
            </div>
          ) : generatedVideoUrl ? (
            <div className="w-full h-full flex flex-col items-center justify-center relative">
              
              {/* Custom Video Frame */}
              <div className={`relative overflow-hidden rounded-xl border border-neutral-800 shadow-2xl max-w-[90%] ${
                aspectRatio === "16:9" ? "aspect-video w-full" : "aspect-[9/16] max-h-[300px]"
              }`}>
                <video 
                  src={generatedVideoUrl} 
                  controls 
                  autoPlay 
                  loop 
                  className="w-full h-full object-cover"
                />
                
                {/* Simulated Overlay */}
                <div className="absolute top-3 right-3 bg-black/60 backdrop-blur px-2 py-0.5 rounded text-[8px] font-mono text-neutral-400 flex items-center gap-1 border border-neutral-800">
                  <span className="h-1.5 w-1.5 bg-red-500 rounded-full animate-ping" />
                  <span>PREVIEW PRO</span>
                </div>
              </div>

              <div className="mt-4 p-3 bg-neutral-900 border border-neutral-800/80 rounded-xl text-[10px] font-mono text-neutral-400 text-center max-w-[90%]">
                تم معالجة المقطع بنمط ({stylePresets.find(s => s.id === style)?.nameAr}) وحركة {cameraMovements.find(c => c.id === cameraMovement)?.label}.
              </div>

              {/* Download / Action Buttons */}
              <div className="mt-4 flex gap-3">
                <a 
                  href={generatedVideoUrl} 
                  download={`hj_smart_video_${Date.now()}.mp4`}
                  className="px-4 py-1.5 rounded-lg bg-neutral-900 border border-neutral-800 hover:border-neutral-700 text-[10px] font-mono text-neutral-300 flex items-center gap-1.5 transition-all"
                  target="_blank"
                  rel="noreferrer"
                >
                  <Download className="h-3.5 w-3.5 text-indigo-400" />
                  <span>تحميل ملف الفيديو (MP4)</span>
                </a>
                <button
                  onClick={() => {
                    setPrompt("");
                    setGeneratedVideoUrl(null);
                  }}
                  className="px-4 py-1.5 rounded-lg bg-neutral-900/50 border border-neutral-800/40 hover:border-neutral-800 text-[10px] font-mono text-neutral-500 hover:text-neutral-300 transition-all"
                >
                  صنع فيديو جديد
                </button>
              </div>

            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-16 text-center text-neutral-600">
              <Film className="h-14 w-14 text-neutral-800 mb-2.5 animate-pulse" />
              <p className="text-xs font-semibold text-neutral-400">استوديو تحويل النص إلى فيديو (Text-to-Video)</p>
              <p className="text-[10px] text-neutral-500 mt-1 max-w-[320px]">
                اكتب الوصف الذي تريده بذكاء، وسيقوم محرك الفيديو الخاص بالمنصة بتخليق مقطع متحرك بجودة فائقة.
              </p>
              <div className="mt-4 flex items-center gap-1 text-[9px] font-mono text-neutral-500 bg-neutral-900/60 border border-neutral-800/40 px-2.5 py-1 rounded">
                <ShieldCheck className="h-3 w-3 text-emerald-500" />
                <span>شراكة حصرية مع H&J Generative Media Core</span>
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
              
              {/* Success Screen inside modal */}
              {paymentSuccess ? (
                <div className="p-10 flex flex-col items-center justify-center text-center gap-4">
                  <div className="h-16 w-16 bg-emerald-500/20 border border-emerald-500/40 rounded-full flex items-center justify-center text-emerald-400 mb-2">
                    <CheckCircle2 className="h-10 w-10 animate-bounce" />
                  </div>
                  <h3 className="text-xl font-bold text-white">تمت عملية الدفع بأمان وسرعة!</h3>
                  <p className="text-xs text-neutral-400 max-w-xs">
                    شكراً لك! تم استلام دفعتك بأمان عبر بوابة الدفع الآمنة المعتمدة. جاري تفعيل الميزات الإضافية لحسابك...
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
                        setCheckoutType(null);
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

                  {/* Pricing Comparison Panel */}
                  <div className="p-6 space-y-6 text-right">
                    
                    {/* Notice board */}
                    <div className="bg-indigo-950/40 border border-indigo-500/30 rounded-xl p-4 flex items-start gap-3">
                      <ShieldCheck className="h-5 w-5 text-indigo-400 flex-shrink-0 mt-0.5" />
                      <div>
                        <h4 className="text-xs font-bold text-indigo-200">لقد استهلكت جميع الميزات المجانية المتاحة</h4>
                        <p className="text-[11px] text-neutral-400 mt-1 leading-relaxed">
                          للاستمرار في صناعة مشهد فيديو الذكاء الاصطناعي الممتاز، يمكنك الدفع للمقطع الواحد أو الحصول على حساب الـ VIP الشامل لكل الميزات (الصور والفيديوهات) دون حدود.
                        </p>
                      </div>
                    </div>

                    {/* Pricing Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      
                      {/* Single Video Card */}
                      <button 
                        onClick={() => setCheckoutType("video")}
                        className={`p-4 rounded-xl border text-right transition-all flex flex-col justify-between ${
                          checkoutType === "video" 
                            ? "bg-neutral-950 border-indigo-500 shadow-lg shadow-indigo-500/5 ring-1 ring-indigo-500" 
                            : "bg-neutral-950/40 border-neutral-800/80 hover:border-neutral-700"
                        }`}
                      >
                        <div className="w-full">
                          <div className="flex items-center justify-between gap-2 mb-2">
                            <span className="bg-neutral-900 border border-neutral-800 text-neutral-400 text-[9px] px-2 py-0.5 rounded">شراء لمرة واحدة</span>
                            <h4 className="text-xs font-bold text-white">شراء فيديو إضافي واحد</h4>
                          </div>
                          <p className="text-[10px] text-neutral-400 leading-relaxed">
                            صناعة فيديو واحد إضافي بكامل الميزات وبدون علامة مائية.
                          </p>
                        </div>
                        <div className="mt-4 flex items-baseline gap-1 justify-end w-full border-t border-neutral-900 pt-3">
                          <span className="text-lg font-bold text-indigo-400">50</span>
                          <span className="text-[10px] text-neutral-500">جنيه مصري / فيديو</span>
                        </div>
                      </button>

                      {/* VIP Subscription Card */}
                      <button 
                        onClick={() => setCheckoutType("vip")}
                        className={`p-4 rounded-xl border text-right transition-all flex flex-col justify-between relative overflow-hidden ${
                          checkoutType === "vip" 
                            ? "bg-indigo-950/20 border-amber-500 shadow-lg shadow-amber-500/5 ring-1 ring-amber-500" 
                            : "bg-neutral-950/40 border-neutral-800/80 hover:border-neutral-700"
                        }`}
                      >
                        {/* Shimmer badge */}
                        <div className="absolute top-0 left-0 bg-amber-500 text-black font-bold text-[8px] px-2.5 py-0.5 rounded-br uppercase tracking-widest">
                          BEST VALUE
                        </div>

                        <div className="w-full">
                          <div className="flex items-center justify-between gap-2 mb-2 mt-1">
                            <div className="flex items-center gap-1 text-amber-400">
                              <Crown className="h-3 w-3 fill-amber-400/20" />
                            </div>
                            <h4 className="text-xs font-bold text-white">باقة VIP اللانهائية الشاملة</h4>
                          </div>
                          <p className="text-[10px] text-neutral-400 leading-relaxed">
                            تفتح وصولاً غير محدود لكل من الصور (بدون سقف الـ 5 اليومي) ومقاطع الفيديو مجاناً طوال الشهر!
                          </p>
                        </div>
                        <div className="mt-4 flex items-baseline gap-1 justify-end w-full border-t border-neutral-900 pt-3">
                          <span className="text-lg font-bold text-amber-400">200</span>
                          <span className="text-[10px] text-neutral-500">جنيه مصري / شهرياً</span>
                        </div>
                      </button>

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
                        onClick={handleSimulatePayment}
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
