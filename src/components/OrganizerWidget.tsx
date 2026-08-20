import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  CheckCircle, Plus, Trash2, Calendar, Clock, Sparkles, RefreshCw, 
  Tag, Compass, Smile, Flame, Coffee, HeartPulse, GraduationCap, Code, Briefcase
} from "lucide-react";
import { TaskItem, PlannerEvent } from "../types";

interface OrganizerWidgetProps {
  tasks: TaskItem[];
  onTasksChange: (tasks: TaskItem[]) => void;
  plannerEvents: PlannerEvent[];
  onPlannerEventsChange: (events: PlannerEvent[]) => void;
  currentUser: any;
}

export default function OrganizerWidget({
  tasks,
  onTasksChange,
  plannerEvents,
  onPlannerEventsChange,
  currentUser
}: OrganizerWidgetProps) {
  const [newTaskText, setNewTaskText] = useState("");
  const [taskCategory, setTaskCategory] = useState("study");
  
  // AI Scheduler prompt
  const [plannerPrompt, setPlannerPrompt] = useState("");
  const [isGeneratingPlanner, setIsGeneratingPlanner] = useState(false);

  // Manual event adding
  const [manualEventTime, setManualEventTime] = useState("09:00 AM");
  const [manualEventTitle, setManualEventTitle] = useState("");
  const [manualEventDuration, setManualEventDuration] = useState("1 hour");
  const [manualEventDesc, setManualEventDesc] = useState("");
  const [manualEventCat, setManualEventCat] = useState("study");

  const categories = [
    { id: "study", nameAr: "مذاكرة وتعليم", color: "bg-emerald-500/10 border-emerald-500/30 text-emerald-400" },
    { id: "coding", nameAr: "برمجة وتطوير", color: "bg-blue-500/10 border-blue-500/30 text-blue-400" },
    { id: "health", nameAr: "رياضة وصحة", color: "bg-rose-500/10 border-rose-500/30 text-rose-400" },
    { id: "business", nameAr: "عمل وإدارة", color: "bg-amber-500/10 border-amber-500/30 text-amber-400" },
    { id: "leisure", nameAr: "ترفيه وراحة", color: "bg-violet-500/10 border-violet-500/30 text-violet-400" }
  ];

  // Helper to render Category Badge Icons
  const getCategoryIcon = (cat: string) => {
    switch (cat.toLowerCase()) {
      case "study": return <GraduationCap className="h-3.5 w-3.5" />;
      case "coding": return <Code className="h-3.5 w-3.5" />;
      case "health": return <HeartPulse className="h-3.5 w-3.5" />;
      case "business": return <Briefcase className="h-3.5 w-3.5" />;
      default: return <Coffee className="h-3.5 w-3.5" />;
    }
  };

  // 1. Add Task manually
  const handleAddTask = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTaskText.trim()) return;

    const newTask: TaskItem = {
      id: "task-" + Date.now(),
      text: newTaskText,
      completed: false,
      category: taskCategory
    };

    onTasksChange([...tasks, newTask]);
    setNewTaskText("");
  };

  // 2. Toggle Task state
  const handleToggleTask = (taskId: string) => {
    const updated = tasks.map(t => {
      if (t.id === taskId) {
        return { ...t, completed: !t.completed };
      }
      return t;
    });
    onTasksChange(updated);
  };

  // 3. Delete Task
  const handleDeleteTask = (taskId: string) => {
    onTasksChange(tasks.filter(t => t.id !== taskId));
  };

  // 4. Generate AI daily schedule
  const handleGenerateAISchedule = async () => {
    if (!plannerPrompt.trim() || isGeneratingPlanner) return;
    setIsGeneratingPlanner(true);

    try {
      const res = await fetch("/api/planner/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          prompt: plannerPrompt,
          wakeTime: "08:00",
          sleepTime: "23:00"
        })
      });

      const data = await res.json();
      if (res.ok && data.timeline) {
        onPlannerEventsChange(data.timeline);
        setPlannerPrompt("");
      } else {
        alert(data.error || "فشلت جدولة اليوم بالذكاء الاصطناعي.");
      }
    } catch (e) {
      console.error(e);
      alert("حدث خطأ أثناء جدولة اليوم.");
    } finally {
      setIsGeneratingPlanner(false);
    }
  };

  // 5. Add Manual Planner Event
  const handleAddManualEvent = (e: React.FormEvent) => {
    e.preventDefault();
    if (!manualEventTitle.trim()) return;

    const newEvent: PlannerEvent = {
      time: manualEventTime,
      title: manualEventTitle,
      duration: manualEventDuration,
      description: manualEventDesc || "نشاط مبرمج يدوياً",
      category: manualEventCat
    };

    onPlannerEventsChange([...plannerEvents, newEvent]);
    setManualEventTitle("");
    setManualEventDesc("");
  };

  // 6. Clear Planner Events
  const handleClearPlanner = () => {
    if (confirm("هل تريد بالتأكيد تصفير جدول المهام واليوم الحالي؟")) {
      onPlannerEventsChange([]);
    }
  };

  return (
    <div className="bg-neutral-900/60 rounded-2xl border border-neutral-800 p-6 backdrop-blur-md shadow-xl grid grid-cols-1 lg:grid-cols-5 gap-6">
      
      {/* 1. Daily Task List column */}
      <div className="lg:col-span-2 text-right border-l border-neutral-800/60 pl-0 lg:pl-6 space-y-4">
        
        {/* Task List Header */}
        <div>
          <h3 className="text-lg font-display font-medium text-white flex items-center justify-end gap-2">
            <CheckCircle className="h-5 w-5 text-emerald-400" />
            منظم المهام اليومي (Daily Backlog)
          </h3>
          <p className="text-xs text-neutral-400 mt-0.5">خطط لمهماتك الصغيرة وقسمها بحسب الأولوية والتصنيف.</p>
        </div>

        {/* Task Input Form */}
        <form onSubmit={handleAddTask} className="space-y-3 p-3 rounded-xl bg-neutral-950/40 border border-neutral-800/60">
          <div className="flex gap-2">
            <input
              type="text"
              value={newTaskText}
              onChange={(e) => setNewTaskText(e.target.value)}
              placeholder="مثال: مراجعة الوحدة الثالثة من الكود..."
              className="flex-1 bg-neutral-900 border border-neutral-800 rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-emerald-500 text-right"
            />
            <button
              type="submit"
              className="bg-emerald-600 hover:bg-emerald-500 text-white p-2 rounded-lg text-xs"
              title="إضافة المهمة"
            >
              <Plus className="h-4 w-4" />
            </button>
          </div>

          {/* Category badges picker */}
          <div className="flex flex-wrap gap-1.5 justify-end">
            {categories.map(c => (
              <button
                key={c.id}
                type="button"
                onClick={() => setTaskCategory(c.id)}
                className={`px-2 py-0.5 rounded text-[10px] font-medium border transition-colors ${
                  taskCategory === c.id ? c.color : "bg-transparent border-neutral-800 text-neutral-500 hover:text-neutral-300"
                }`}
              >
                {c.nameAr.split(" ")[0]}
              </button>
            ))}
          </div>
        </form>

        {/* List render */}
        <div className="space-y-2 max-h-[300px] overflow-y-auto pr-1">
          <AnimatePresence>
            {tasks.length > 0 ? (
              tasks.map(t => {
                const catObj = categories.find(c => c.id === t.category) || categories[0];
                return (
                  <motion.div
                    key={t.id}
                    initial={{ opacity: 0, x: 10 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -10 }}
                    className="p-3 bg-neutral-950/20 border border-neutral-900/80 rounded-xl flex items-center justify-between gap-3 group"
                  >
                    {/* Delete button */}
                    <button
                      onClick={() => handleDeleteTask(t.id)}
                      className="opacity-0 group-hover:opacity-100 p-1 text-neutral-500 hover:text-rose-400 transition"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>

                    {/* Task details */}
                    <div className="flex items-center gap-3">
                      <div className="text-right">
                        <span 
                          onClick={() => handleToggleTask(t.id)}
                          className={`text-xs cursor-pointer select-none font-medium ${t.completed ? "line-through text-neutral-500" : "text-neutral-200 hover:text-white"}`}
                        >
                          {t.text}
                        </span>
                        
                        {/* Task Category Tag badge */}
                        <div className="flex justify-end mt-1">
                          <span className={`px-1.5 py-0.5 rounded-[4px] text-[8px] font-mono border ${catObj.color} flex items-center gap-1`}>
                            {getCategoryIcon(t.category)}
                            <span>{catObj.nameAr}</span>
                          </span>
                        </div>
                      </div>

                      {/* Checkbox trigger */}
                      <button 
                        onClick={() => handleToggleTask(t.id)}
                        className={`h-4.5 w-4.5 rounded-full border flex items-center justify-center transition ${
                          t.completed 
                            ? "bg-emerald-500 border-emerald-400 text-neutral-950" 
                            : "border-neutral-700 hover:border-emerald-500"
                        }`}
                      >
                        {t.completed && <CheckCircle className="h-3.5 w-3.5 fill-current" />}
                      </button>
                    </div>
                  </motion.div>
                );
              })
            ) : (
              <div className="text-center py-10 text-xs text-neutral-600">
                لا توجد مهام نشطة حالياً. خطط لمهمتك الأولى بالأعلى!
              </div>
            )}
          </AnimatePresence>
        </div>

      </div>

      {/* 2. Interactive Scheduler Column (Chronological Planner) */}
      <div className="lg:col-span-3 text-right space-y-4">
        
        {/* Planner Header */}
        <div className="flex items-center justify-between border-b border-neutral-800 pb-2">
          <button
            onClick={handleClearPlanner}
            className="text-[10px] font-mono text-neutral-500 hover:text-rose-400"
          >
            تفريغ الجدول
          </button>
          <h3 className="text-lg font-display font-medium text-white flex items-center justify-end gap-2">
            <Calendar className="h-5 w-5 text-indigo-400" />
            جدول اليوم الذكي (AI Daily Schedule Planner)
          </h3>
        </div>

        {/* AI Scheduler Generator Form */}
        <div className="p-3.5 bg-indigo-950/10 border border-indigo-900/30 rounded-2xl space-y-2.5">
          <span className="text-xs text-neutral-300 font-medium flex items-center justify-end gap-1.5">
            <span>التخطيط التلقائي لليوم بالذكاء الاصطناعي (AI Schedule Auto-Planner):</span>
            <Sparkles className="h-4 w-4 text-indigo-400 animate-pulse" />
          </span>

          <div className="flex gap-2">
            <input
              type="text"
              value={plannerPrompt}
              onChange={(e) => setPlannerPrompt(e.target.value)}
              placeholder="مثال: نظم لي يوم المذاكرة لثلاث تخصصات مع فترات راحة ورياضة..."
              className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-2.5 text-xs text-white focus:outline-none focus:border-indigo-500 text-right"
              disabled={isGeneratingPlanner}
            />
            <button
              onClick={handleGenerateAISchedule}
              disabled={isGeneratingPlanner || !plannerPrompt.trim()}
              className="bg-indigo-600 hover:bg-indigo-500 disabled:bg-neutral-800 text-white text-xs px-4 py-2.5 rounded-xl font-medium transition flex items-center gap-1"
            >
              {isGeneratingPlanner ? "جاري الجدولة..." : "جدولة ذكية"}
            </button>
          </div>
        </div>

        {/* Chronological Planner Timeline list */}
        <div className="space-y-3 max-h-[340px] overflow-y-auto pr-1">
          {plannerEvents.length > 0 ? (
            plannerEvents.map((evt, idx) => {
              const catObj = categories.find(c => c.id === evt.category) || categories[0];
              return (
                <motion.div
                  key={idx}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="p-4 bg-neutral-950/40 border border-neutral-900 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-3 text-right relative"
                >
                  {/* Left Column (Meta tags) */}
                  <div className="flex items-center gap-2 justify-end md:justify-start">
                    <span className="text-[10px] font-mono text-neutral-500 bg-neutral-900 px-2 py-0.5 rounded border border-neutral-800">
                      ⏱️ {evt.duration}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-[10px] border font-mono flex items-center gap-1 ${catObj.color}`}>
                      {getCategoryIcon(evt.category)}
                      <span>{catObj.nameAr}</span>
                    </span>
                  </div>

                  {/* Right Column (Event title + desc + time) */}
                  <div className="flex items-start gap-3 justify-end text-right">
                    <div>
                      <h4 className="text-sm font-semibold text-white leading-snug">{evt.title}</h4>
                      <p className="text-xs text-neutral-400 mt-1 leading-normal">{evt.description}</p>
                    </div>

                    {/* Clock element */}
                    <div className="bg-neutral-900 border border-neutral-800/80 rounded-xl px-2.5 py-1.5 flex flex-col items-center justify-center min-w-[70px] flex-shrink-0">
                      <Clock className="h-3.5 w-3.5 text-neutral-500 mb-1" />
                      <span className="text-[10px] font-mono text-neutral-300 font-semibold">{evt.time}</span>
                    </div>
                  </div>

                </motion.div>
              );
            })
          ) : (
            <div className="flex flex-col items-center justify-center py-16 text-neutral-600 text-center">
              <Calendar className="h-10 w-10 text-neutral-700 mb-2" />
              <p className="text-xs">لا توجد فعاليات مجدولة لليوم.</p>
              <p className="text-[10px] text-neutral-500 mt-1 max-w-[260px]">
                اكتب طلباً للأوتو-بلانر بالأعلى أو اضف مهام backlog ليرسم الذكاء الاصطناعي لك جدولاً رائعاً لليوم.
              </p>
            </div>
          )}
        </div>

      </div>

    </div>
  );
}
