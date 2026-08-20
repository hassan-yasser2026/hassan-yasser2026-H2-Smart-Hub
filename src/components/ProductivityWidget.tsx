import React, { useState, useRef } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  BookOpen, FileText, FileSpreadsheet, Presentation, LayoutGrid, Check, 
  Sparkles, Download, ChevronLeft, ChevronRight, UploadCloud, Trash2, ArrowLeft, ArrowRight, Play, CheckCircle
} from "lucide-react";

export default function ProductivityWidget() {
  const [activeTab, setActiveTab] = useState<"summarizer" | "researcher" | "slides">("summarizer");
  const [isLoading, setIsLoading] = useState(false);

  // 1. Summarizer States
  const [pastedText, setPastedText] = useState("");
  const [uploadedFileName, setUploadedFileName] = useState("");
  const [summaryOutput, setSummaryOutput] = useState("");
  const [summaryFormat, setSummaryFormat] = useState("bullets");
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // 2. Researcher States
  const [researchTopic, setResearchTopic] = useState("");
  const [academicField, setAcademicField] = useState("علوم الحاسب والذكاء الاصطناعي");
  const [researchOutput, setResearchOutput] = useState("");

  // 3. Presentation Slides States
  const [slidesTopic, setSlidesTopic] = useState("");
  const [slidesCount, setSlidesCount] = useState(5);
  const [generatedSlides, setGeneratedSlides] = useState<any[]>([]);
  const [currentSlideIndex, setCurrentSlideIndex] = useState(0);

  // Handle simple file drag & drop or file uploads for text files
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadedFileName(file.name);
    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      setPastedText(text);
    };
    reader.readAsText(file);
  };

  const triggerFileSelect = () => {
    fileInputRef.current?.click();
  };

  const clearUploadedFile = () => {
    setUploadedFileName("");
    setPastedText("");
    setSummaryOutput("");
  };

  // Call API for Book Summarizer
  const handleSummarize = async () => {
    if (!pastedText.trim() || isLoading) return;
    setIsLoading(true);
    try {
      const res = await fetch("/api/summarize", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          documentText: pastedText,
          fileName: uploadedFileName || "مستند مدخل",
          format: summaryFormat
        })
      });
      const data = await res.json();
      if (res.ok) {
        setSummaryOutput(data.summary);
      } else {
        alert(data.error || "فشل التلخيص");
      }
    } catch (e) {
      console.error(e);
      alert("حدث خطأ أثناء الاتصال بالخادم.");
    } finally {
      setIsLoading(false);
    }
  };

  // Call API for Academic Research / Essay Generator
  const handleGenerateResearch = async () => {
    if (!researchTopic.trim() || isLoading) return;
    setIsLoading(true);
    try {
      const res = await fetch("/api/research", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          topic: researchTopic,
          academicField
        })
      });
      const data = await res.json();
      if (res.ok) {
        setResearchOutput(data.content);
      } else {
        alert(data.error || "فشل توليد البحث العلمي");
      }
    } catch (e) {
      console.error(e);
      alert("حدث خطأ أثناء تخليق الورقة البحثية.");
    } finally {
      setIsLoading(false);
    }
  };

  // Call API for Slide Presentations Generator
  const handleGenerateSlides = async () => {
    if (!slidesTopic.trim() || isLoading) return;
    setIsLoading(true);
    try {
      const res = await fetch("/api/presentation", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          topic: slidesTopic,
          slidesCount
        })
      });
      const data = await res.json();
      if (res.ok && data.slides) {
        setGeneratedSlides(data.slides);
        setCurrentSlideIndex(0);
      } else {
        alert(data.error || "فشل توليد شرائح العرض");
      }
    } catch (e) {
      console.error(e);
      alert("حدث خطأ في معالجة العرض التقديمي.");
    } finally {
      setIsLoading(false);
    }
  };

  // Helper to copy outputs to clipboard
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    alert("تم النسخ بنجاح!");
  };

  return (
    <div className="bg-neutral-900/60 rounded-2xl border border-neutral-800 p-6 backdrop-blur-md shadow-xl flex flex-col gap-6">
      
      {/* Sub-Header & Tabs Navigation */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between border-b border-neutral-800 pb-4 gap-4">
        <div className="text-right">
          <h2 className="text-xl font-display font-medium text-white flex items-center justify-end gap-2">
            <Sparkles className="h-5 w-5 text-amber-500" />
            أدوات الإنتاجية الذكية (Productivity Suite)
          </h2>
          <p className="text-xs text-neutral-400 mt-1">
            أبحاث أكاديمية، تلخيص وثائق وملفات، وتخليق عروض تقديمية تفاعلية بلمح البصر.
          </p>
        </div>

        {/* Tab Controls */}
        <div className="flex bg-neutral-950 p-1.5 rounded-xl border border-neutral-800 self-end md:self-center gap-1.5 overflow-x-auto scrollbar-none">
          <button
            onClick={() => setActiveTab("summarizer")}
            className={`px-4 py-2 rounded-lg text-xs font-medium transition-all flex items-center gap-2 ${
              activeTab === "summarizer" 
                ? "bg-blue-600 text-white shadow-md font-semibold" 
                : "text-neutral-400 hover:text-white"
            }`}
          >
            <BookOpen className="h-3.5 w-3.5" />
            <span>تلخيص كتب وملفات</span>
          </button>
          <button
            onClick={() => setActiveTab("researcher")}
            className={`px-4 py-2 rounded-lg text-xs font-medium transition-all flex items-center gap-2 ${
              activeTab === "researcher" 
                ? "bg-emerald-600 text-white shadow-md font-semibold" 
                : "text-neutral-400 hover:text-white"
            }`}
          >
            <FileText className="h-3.5 w-3.5" />
            <span>كتابة أبحاث ومقالات</span>
          </button>
          <button
            onClick={() => setActiveTab("slides")}
            className={`px-4 py-2 rounded-lg text-xs font-medium transition-all flex items-center gap-2 ${
              activeTab === "slides" 
                ? "bg-fuchsia-600 text-white shadow-md font-semibold" 
                : "text-neutral-400 hover:text-white"
            }`}
          >
            <Presentation className="h-3.5 w-3.5" />
            <span>عروض تقديمية وتقارير</span>
          </button>
        </div>
      </div>

      {/* Main Content Body */}
      <div className="min-h-[380px]">
        {activeTab === "summarizer" && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            {/* Input Side */}
            <div className="space-y-4 text-right">
              <label className="text-xs font-mono text-neutral-400 tracking-wider">
                1. أدخل محتوى الوثيقة أو ارفع ملف نصي:
              </label>

              {/* Upload Zone */}
              <div 
                onClick={triggerFileSelect}
                className="border-2 border-dashed border-neutral-800 hover:border-blue-500/50 bg-neutral-950/40 rounded-xl p-5 text-center cursor-pointer transition-all flex flex-col items-center justify-center gap-2 group"
              >
                <input 
                  type="file" 
                  ref={fileInputRef} 
                  onChange={handleFileUpload}
                  accept=".txt,.md,.json,.csv,.xml,.js,.ts"
                  className="hidden" 
                />
                <UploadCloud className="h-8 w-8 text-neutral-600 group-hover:text-blue-400 transition-colors" />
                {uploadedFileName ? (
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-mono text-blue-400 font-semibold">{uploadedFileName}</span>
                    <button 
                      onClick={(e) => { e.stopPropagation(); clearUploadedFile(); }}
                      className="p-1 rounded bg-neutral-900 hover:text-rose-400"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                ) : (
                  <div>
                    <p className="text-xs text-neutral-300 font-medium">اضغط لرفع ملف نصي أو وثيقة</p>
                    <p className="text-[10px] text-neutral-500 mt-1">يدعم .txt, .md, .csv, .json</p>
                  </div>
                )}
              </div>

              {/* Paste Text Area */}
              <textarea
                value={pastedText}
                onChange={(e) => setPastedText(e.target.value)}
                placeholder="أو الصق نصوص الكتاب، المقال، أو الملف المراد تلخيصه هنا..."
                rows={6}
                className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-4 text-sm text-neutral-200 focus:outline-none focus:border-blue-500 placeholder:text-neutral-600 text-right dir-rtl"
              />

              {/* Options & Action */}
              <div className="flex items-center justify-between gap-4">
                <div className="flex items-center gap-2">
                  <span className="text-xs text-neutral-500">صيغة التلخيص:</span>
                  <select
                    value={summaryFormat}
                    onChange={(e) => setSummaryFormat(e.target.value)}
                    className="bg-neutral-950 border border-neutral-800 text-xs text-neutral-300 rounded px-2.5 py-1.5 focus:outline-none"
                  >
                    <option value="bullets">نقاط رئيسية مبوبة</option>
                    <option value="detailed">ملخص أكاديمي مفصل</option>
                    <option value="executive">إيجاز تنفيذي قصير</option>
                  </select>
                </div>

                <button
                  onClick={handleSummarize}
                  disabled={isLoading || !pastedText.trim()}
                  className="bg-blue-600 hover:bg-blue-500 disabled:bg-neutral-800 disabled:text-neutral-500 text-white font-medium text-xs px-5 py-2.5 rounded-xl transition shadow-md flex items-center gap-1.5"
                >
                  {isLoading ? "جاري التلخيص..." : "ابدأ التلخيص الذكي"}
                </button>
              </div>
            </div>

            {/* Output Side */}
            <div className="bg-neutral-950/50 border border-neutral-800/80 rounded-xl p-5 flex flex-col justify-between h-full min-h-[300px]">
              <div>
                <div className="flex items-center justify-between border-b border-neutral-900 pb-2 mb-3">
                  {summaryOutput && (
                    <button 
                      onClick={() => copyToClipboard(summaryOutput)}
                      className="text-xs text-blue-400 hover:underline"
                    >
                      نسخ الملخص
                    </button>
                  )}
                  <span className="text-xs font-mono text-neutral-500">التقرير والملخص الذكي</span>
                </div>

                {summaryOutput ? (
                  <div className="text-sm leading-relaxed text-neutral-200 whitespace-pre-wrap text-right dir-rtl max-h-[320px] overflow-y-auto pr-1">
                    {summaryOutput}
                  </div>
                ) : (
                  <div className="text-center text-neutral-600 text-xs py-20">
                    لم يتم إنشاء ملخص بعد. أدخل نصوصك واضغط على "ابدأ التلخيص".
                  </div>
                )}
              </div>
              
              <div className="border-t border-neutral-900 pt-3 mt-4 text-[10px] text-neutral-500 flex items-center justify-end gap-1">
                <span>تأمين حماية فكري كامل ضد المحتوى الضار</span>
                <Check className="h-3.5 w-3.5 text-emerald-500" />
              </div>
            </div>

          </div>
        )}

        {activeTab === "researcher" && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            {/* Researcher Setup */}
            <div className="space-y-4 text-right">
              <label className="text-xs font-mono text-neutral-400 tracking-wider block">
                تأليف الأبحاث الأكاديمية وصياغة المقالات:
              </label>

              <div className="space-y-2">
                <span className="text-xs text-neutral-400">عنوان أو موضوع البحث الرئيسي:</span>
                <input
                  type="text"
                  value={researchTopic}
                  onChange={(e) => setResearchTopic(e.target.value)}
                  placeholder="مثال: تأثير الذكاء الاصطناعي على مستقبل الطب البديل..."
                  className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-3 text-sm text-white focus:outline-none focus:border-emerald-500 text-right"
                />
              </div>

              <div className="space-y-2">
                <span className="text-xs text-neutral-400">المجال الأكاديمي أو التخصص العلمي:</span>
                <input
                  type="text"
                  value={academicField}
                  onChange={(e) => setAcademicField(e.target.value)}
                  placeholder="مثال: علوم الحاسب، الاقتصاد الرقمي، علم النفس الإيجابي..."
                  className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-3 text-sm text-white focus:outline-none focus:border-emerald-500 text-right"
                />
              </div>

              <button
                onClick={handleGenerateResearch}
                disabled={isLoading || !researchTopic.trim()}
                className="bg-emerald-600 hover:bg-emerald-500 disabled:bg-neutral-800 disabled:text-neutral-500 text-white font-medium text-xs px-5 py-2.5 rounded-xl transition shadow-md w-full"
              >
                {isLoading ? "جاري تخليق وتوثيق الدراسة..." : "توليد البحث العلمي والمراجع الموثقة"}
              </button>
            </div>

            {/* Researcher Output */}
            <div className="bg-neutral-950/50 border border-neutral-800/80 rounded-xl p-5 flex flex-col justify-between min-h-[300px]">
              <div>
                <div className="flex items-center justify-between border-b border-neutral-900 pb-2 mb-3">
                  {researchOutput && (
                    <button 
                      onClick={() => copyToClipboard(researchOutput)}
                      className="text-xs text-emerald-400 hover:underline"
                    >
                      نسخ البحث بالكامل
                    </button>
                  )}
                  <span className="text-xs font-mono text-neutral-500">مسودة البحث العلمي المعتمدة</span>
                </div>

                {researchOutput ? (
                  <div className="text-sm leading-relaxed text-neutral-200 whitespace-pre-wrap text-right dir-rtl max-h-[320px] overflow-y-auto pr-1">
                    {researchOutput}
                  </div>
                ) : (
                  <div className="text-center text-neutral-600 text-xs py-20">
                    أدخل موضوع البحث والتخصص، ثم انقر على "توليد البحث العلمي" لصياغة دراسة أكاديمية متكاملة.
                  </div>
                )}
              </div>
            </div>

          </div>
        )}

        {activeTab === "slides" && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            
            {/* Slide Configuration Box */}
            <div className="md:col-span-1 space-y-4 text-right border-r border-neutral-800/60 pr-2">
              <label className="text-xs font-mono text-neutral-400 tracking-wider block">
                تخليق العروض التقديمية (Slides Generator):
              </label>

              <div className="space-y-2">
                <span className="text-xs text-neutral-400">موضوع أو عنوان العرض المقترح:</span>
                <input
                  type="text"
                  value={slidesTopic}
                  onChange={(e) => setSlidesTopic(e.target.value)}
                  placeholder="مثال: ريادة الأعمال وصناعة الشركات الناشئة..."
                  className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-3 text-sm text-white focus:outline-none focus:border-fuchsia-500 text-right"
                />
              </div>

              <div className="space-y-2">
                <span className="text-xs text-neutral-400">عدد شرائح العرض المطلوبة:</span>
                <input
                  type="number"
                  min={3}
                  max={10}
                  value={slidesCount}
                  onChange={(e) => setSlidesCount(parseInt(e.target.value) || 5)}
                  className="w-full bg-neutral-950 border border-neutral-800 rounded-xl p-3 text-sm text-white focus:outline-none text-right"
                />
              </div>

              <button
                onClick={handleGenerateSlides}
                disabled={isLoading || !slidesTopic.trim()}
                className="bg-fuchsia-600 hover:bg-fuchsia-500 disabled:bg-neutral-800 disabled:text-neutral-500 text-white font-medium text-xs px-5 py-2.5 rounded-xl transition shadow-md w-full"
              >
                {isLoading ? "جاري تخطيط الشرائح..." : "تخليق شرائح العرض التفاعلية"}
              </button>
            </div>

            {/* Slides Presentation Player Deck */}
            <div className="md:col-span-2 bg-neutral-950/50 border border-neutral-800/80 rounded-2xl p-6 flex flex-col justify-between min-h-[340px] relative overflow-hidden">
              
              {generatedSlides.length > 0 ? (
                <>
                  {/* Decorative Slide Background Accent */}
                  <div className="absolute top-[-20%] right-[-10%] w-[40%] h-[40%] rounded-full bg-fuchsia-500/5 blur-[80px] pointer-events-none" />

                  {/* Active Slide Renderer */}
                  <div className="relative z-10 flex-1 flex flex-col justify-center text-right">
                    
                    {/* Header bar of slide */}
                    <div className="flex items-center justify-between border-b border-neutral-900 pb-2 mb-4">
                      <span className="text-xs font-mono text-fuchsia-400">
                        شريحة {currentSlideIndex + 1} من {generatedSlides.length}
                      </span>
                      <span className="text-xs text-neutral-500 font-medium">H&J Presentation System</span>
                    </div>

                    {/* Content */}
                    <h3 className="text-lg md:text-xl font-display font-semibold text-white mb-4 leading-tight">
                      {generatedSlides[currentSlideIndex].title}
                    </h3>

                    <ul className="space-y-2.5 pr-2">
                      {generatedSlides[currentSlideIndex].bullets.map((bullet: string, idx: number) => (
                        <li key={idx} className="text-sm text-neutral-300 flex items-start gap-2 justify-end text-right">
                          <span>{bullet}</span>
                          <span className="h-1.5 w-1.5 rounded-full bg-fuchsia-500 mt-2 flex-shrink-0" />
                        </li>
                      ))}
                    </ul>

                    {/* Design Tip accent info */}
                    <div className="mt-6 p-2 rounded bg-neutral-900 border border-neutral-800/60 text-[10px] font-mono text-neutral-400">
                      🎨 نصيحة تصميمية: {generatedSlides[currentSlideIndex].designTip}
                    </div>
                  </div>

                  {/* Slides Player Controls footer */}
                  <div className="relative z-10 border-t border-neutral-900 pt-4 mt-6 flex items-center justify-between">
                    <button
                      onClick={() => setCurrentSlideIndex(prev => Math.min(generatedSlides.length - 1, prev + 1))}
                      disabled={currentSlideIndex === generatedSlides.length - 1}
                      className="text-xs bg-neutral-900 border border-neutral-800 px-3 py-1.5 rounded text-neutral-300 hover:bg-neutral-800 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed flex items-center gap-1"
                    >
                      <ChevronLeft className="h-4 w-4" />
                      <span>السابق</span>
                    </button>

                    <button 
                      onClick={() => copyToClipboard(JSON.stringify(generatedSlides, null, 2))}
                      className="text-[10px] font-mono text-neutral-500 hover:text-neutral-300 flex items-center gap-1"
                    >
                      <Download className="h-3 w-3" />
                      <span>تصدير ملف العرض</span>
                    </button>

                    <button
                      onClick={() => setCurrentSlideIndex(prev => Math.max(0, prev - 1))}
                      disabled={currentSlideIndex === 0}
                      className="text-xs bg-neutral-900 border border-neutral-800 px-3 py-1.5 rounded text-neutral-300 hover:bg-neutral-800 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed flex items-center gap-1"
                    >
                      <span>التالي</span>
                      <ChevronRight className="h-4 w-4" />
                    </button>
                  </div>
                </>
              ) : (
                <div className="flex flex-col items-center justify-center py-16 text-center text-neutral-600">
                  <Presentation className="h-12 w-12 text-neutral-700 mb-2" />
                  <p className="text-xs">لم يتم توليد عرض تقديمي بعد.</p>
                  <p className="text-[10px] text-neutral-500 mt-1 max-w-[280px]">
                    اكتب عنوان ومحاور العرض الذي تريده في اللوحة اليمنى ليقوم الذكاء الاصطناعي بتخليق عرض تفاعلي جميل.
                  </p>
                </div>
              )}

            </div>

          </div>
        )}
      </div>

    </div>
  );
}
