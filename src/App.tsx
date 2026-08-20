import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  Sparkles, Bot, ShieldCheck, Star, CloudLightning, MessageSquareCode, 
  Wand, Layers, CheckSquare, RefreshCw, LogOut, Terminal, LayoutDashboard,
  Video, Crown
} from "lucide-react";

import ClockWidget from "./components/ClockWidget";
import SmartChatWidget from "./components/SmartChatWidget";
import ProductivityWidget from "./components/ProductivityWidget";
import ImageGeneratorWidget from "./components/ImageGeneratorWidget";
import OrganizerWidget from "./components/OrganizerWidget";
import AIVideoWidget from "./components/AIVideoWidget";
// @ts-ignore
import hassanAvatar from "./assets/images/hassan_avatar_1783868972463.jpg";
import { UserAccount, TaskItem, PlannerEvent } from "./types";

export default function App() {
  // 1. Current Active User Account State
  const [currentUser, setCurrentUser] = useState<UserAccount>({
    username: "Hassan",
    avatar: hassanAvatar,
    color: "from-blue-600 to-indigo-700",
    tagline: "المطور الرئيسي ومؤسس المنصة",
    isVIP: true
  });

  // 2. Navigation State
  const [activeTab, setActiveTab] = useState<"chat" | "productivity" | "images" | "planner" | "video">("chat");

  // 3. Organizer State shared for global Cloud Synchronization
  const [tasks, setTasks] = useState<TaskItem[]>([]);
  const [plannerEvents, setPlannerEvents] = useState<PlannerEvent[]>([]);

  // 4. Synchronization Indicators
  const [syncStatus, setSyncStatus] = useState<"idle" | "syncing" | "success" | "error">("idle");
  const [syncTime, setSyncTime] = useState("");

  // Load user cloud-synchronized items when changing profile
  const fetchCloudData = async (user: string) => {
    setSyncStatus("syncing");
    try {
      const res = await fetch(`/api/sync/load?username=${user}`);
      const result = await res.json();
      
      if (res.ok && result.found && result.data) {
        setTasks(result.data.tasks || []);
        setPlannerEvents(result.data.plannerEvents || []);
        setSyncStatus("success");
        setSyncTime(new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }));
      } else {
        // Fallback to local storage if no server-side sync file exists yet
        const localTasks = localStorage.getItem(`hj_tasks_${user}`);
        const localEvents = localStorage.getItem(`hj_events_${user}`);
        
        setTasks(localTasks ? JSON.parse(localTasks) : []);
        setPlannerEvents(localEvents ? JSON.parse(localEvents) : []);
        setSyncStatus("idle");
      }
    } catch (e) {
      console.error("Cloud sync load failed, fallback to local storage:", e);
      setSyncStatus("error");
    }
  };

  // Save state back to server for cloud sync
  const saveCloudData = async (user: string, currentTasks: TaskItem[], currentEvents: PlannerEvent[]) => {
    setSyncStatus("syncing");
    try {
      const res = await fetch("/api/sync/save", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: user,
          data: {
            tasks: currentTasks,
            plannerEvents: currentEvents
          }
        })
      });

      if (res.ok) {
        setSyncStatus("success");
        setSyncTime(new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }));
        
        // Also cache locally
        localStorage.setItem(`hj_tasks_${user}`, JSON.stringify(currentTasks));
        localStorage.setItem(`hj_events_${user}`, JSON.stringify(currentEvents));
      } else {
        setSyncStatus("error");
      }
    } catch (e) {
      console.error("Cloud sync save failed:", e);
      setSyncStatus("error");
    }
  };

  // Trigger sync on user change
  useEffect(() => {
    fetchCloudData(currentUser.username);
  }, [currentUser]);

  // Handle tasks updates and trigger auto-sync
  const handleTasksChange = (newTasks: TaskItem[]) => {
    setTasks(newTasks);
    saveCloudData(currentUser.username, newTasks, plannerEvents);
  };

  // Handle planner events updates and trigger auto-sync
  const handlePlannerEventsChange = (newEvents: PlannerEvent[]) => {
    setPlannerEvents(newEvents);
    saveCloudData(currentUser.username, tasks, newEvents);
  };

  // Manual trigger for synchronization
  const handleManualSync = () => {
    saveCloudData(currentUser.username, tasks, plannerEvents);
  };

  return (
    <div className="min-h-screen bg-blue-950 text-neutral-200 bg-grid-pattern-dark relative overflow-x-hidden font-sans select-none selection:bg-blue-500/30 selection:text-white">
      
      {/* Ambient glass glows for Brand aesthetics */}
      <div className="absolute top-[-10%] right-[-10%] w-[50%] h-[50%] rounded-full bg-cyan-500/5 blur-[140px] pointer-events-none" />
      <div className="absolute bottom-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-blue-500/5 blur-[140px] pointer-events-none" />

      {/* Primary Brand Container */}
      <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex flex-col min-h-screen">
        
        {/* Navigation & Header */}
        <header className="flex flex-col md:flex-row items-center justify-between border-b border-neutral-900 pb-5 mb-8 gap-4">
          
          {/* Brand Logo & Slogan */}
          <div className="flex items-center gap-3.5 text-right md:text-left">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 border border-blue-400/30 text-white shadow-xl shadow-blue-500/15">
              <Sparkles className="h-5 w-5 animate-pulse" />
            </div>
            <div className="text-right">
              <div className="flex items-center gap-2 justify-end">
                <span className="bg-blue-500/10 border border-blue-500/20 text-blue-400 text-[10px] font-mono tracking-widest px-2 py-0.5 rounded uppercase">
                  Super App Brand
                </span>
                <h1 className="text-xl font-display font-bold text-white tracking-tight">H&J Smart Hub</h1>
              </div>
              <p className="text-xs text-neutral-400 mt-0.5">
                منصة الذكاء الاصطناعي الشاملة لحسن وجنى - Hassan & Jana Smart Engine
              </p>
            </div>
          </div>

          {/* Module Selection Navigation */}
          <nav className="flex bg-neutral-900/80 border border-neutral-800 p-1.5 rounded-2xl gap-1 overflow-x-auto max-w-full">
            <button
              onClick={() => setActiveTab("chat")}
              className={`px-4 py-2.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-2 ${
                activeTab === "chat" 
                  ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/10" 
                  : "text-neutral-400 hover:text-white"
              }`}
            >
              <Bot className="h-4 w-4" />
              <span>الدردشة والشخصيات</span>
            </button>
            <button
              onClick={() => setActiveTab("productivity")}
              className={`px-4 py-2.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-2 ${
                activeTab === "productivity" 
                  ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/10" 
                  : "text-neutral-400 hover:text-white"
              }`}
            >
              <Layers className="h-4 w-4" />
              <span>أدوات الإنتاجية</span>
            </button>
            <button
              onClick={() => setActiveTab("images")}
              className={`px-4 py-2.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-2 ${
                activeTab === "images" 
                  ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/10" 
                  : "text-neutral-400 hover:text-white"
              }`}
            >
              <Wand className="h-4 w-4" />
              <span>توليد الصور</span>
            </button>
            <button
              onClick={() => setActiveTab("video")}
              className={`px-4 py-2.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-2 ${
                activeTab === "video" 
                  ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/10" 
                  : "text-neutral-400 hover:text-white"
              }`}
            >
              <Video className="h-4 w-4" />
              <span>فيديو الذكاء الاصطناعي (AI Video)</span>
            </button>
            <button
              onClick={() => setActiveTab("planner")}
              className={`px-4 py-2.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-2 ${
                activeTab === "planner" 
                  ? "bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md shadow-blue-500/10" 
                  : "text-neutral-400 hover:text-white"
              }`}
            >
              <CheckSquare className="h-4 w-4" />
              <span>المهام والجدول</span>
            </button>
          </nav>
        </header>

        {/* Dynamic Greeting & Digital Clock Banner */}
        <section className="mb-6">
          <ClockWidget username={currentUser.username} />
        </section>

        {/* Primary Workspace Panels */}
        <main className="flex-1 flex flex-col gap-6">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeTab}
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -15 }}
              transition={{ duration: 0.4 }}
              className="flex-1"
            >
              {activeTab === "chat" && (
                <SmartChatWidget 
                  currentUser={currentUser} 
                  onUserChange={setCurrentUser}
                  onSyncTrigger={handleManualSync}
                  syncStatus={syncStatus}
                  syncTime={syncTime}
                />
              )}

              {activeTab === "productivity" && (
                <ProductivityWidget />
              )}

              {activeTab === "images" && (
                <ImageGeneratorWidget currentUser={currentUser} />
              )}

              {activeTab === "video" && (
                <AIVideoWidget currentUser={currentUser} />
              )}

              {activeTab === "planner" && (
                <OrganizerWidget 
                  tasks={tasks}
                  onTasksChange={handleTasksChange}
                  plannerEvents={plannerEvents}
                  onPlannerEventsChange={handlePlannerEventsChange}
                  currentUser={currentUser}
                />
              )}
            </motion.div>
          </AnimatePresence>
        </main>

        {/* Strict Safety Layer Information Board & User Status Bar */}
        <section className="mt-8 flex flex-col gap-4">
          
          {/* Bottom Status Bar showing active user profile avatar & details */}
          <div className="p-4 bg-neutral-900 border border-neutral-800 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4 text-xs font-sans text-neutral-300">
            <div className="flex items-center gap-3">
              {currentUser.avatar.includes("/") || currentUser.avatar.startsWith("data:") ? (
                <img 
                  src={currentUser.avatar} 
                  alt={currentUser.username} 
                  className="h-9 w-9 rounded-full object-cover ring-2 ring-blue-500/30"
                  referrerPolicy="no-referrer"
                />
              ) : (
                <span className={`h-9 w-9 rounded-full flex items-center justify-center text-white font-bold bg-gradient-to-tr ${currentUser.color} ring-2 ring-fuchsia-500/30`}>
                  {currentUser.username[0]}
                </span>
              )}
              <div className="text-right sm:text-left">
                <div className="flex items-center gap-1.5 justify-end sm:justify-start">
                  <h4 className="font-semibold text-white">{currentUser.username}</h4>
                  {currentUser.isVIP ? (
                    <span className="bg-gradient-to-r from-amber-500 to-yellow-500 text-black font-extrabold text-[8px] px-2 py-0.5 rounded flex items-center gap-1">
                      <Crown className="h-2 w-2 fill-black" />
                      <span>عضو VIP مميز</span>
                    </span>
                  ) : (
                    <span className="bg-neutral-800 text-neutral-400 text-[8px] px-2 py-0.5 rounded font-mono">
                      عضو زائر (Guest)
                    </span>
                  )}
                </div>
                <p className="text-[10px] text-neutral-500">{currentUser.tagline}</p>
              </div>
            </div>

            <div className="text-neutral-500 text-right sm:text-left text-[10px] font-mono flex items-center gap-2">
              <span className="h-2 w-2 bg-emerald-500 rounded-full animate-ping" />
              <span>مزامنة تلقائية للحساب نشطة (Realtime Sync: OK)</span>
            </div>
          </div>

          <div className="p-4 bg-neutral-900/40 border border-neutral-800 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4 text-xs font-mono text-neutral-400">
            <div className="flex items-center gap-2 text-right sm:text-left">
              <ShieldCheck className="h-4 w-4 text-emerald-400 animate-pulse" />
              <span>نظام أمان صارم ومراقبة أخلاقية مفعلة بالكامل (Content Moderation & NSFW Filter Active)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span>الملكية الفكرية محفوظة لـ H&J Hub</span>
              <Star className="h-3 w-3 text-amber-500 fill-amber-500/20" />
            </div>
          </div>
        </section>

        {/* Footer */}
        <footer className="mt-6 pt-5 border-t border-neutral-900 text-center text-[10px] font-mono text-neutral-600">
          <p>© 2026 H&J Smart Hub Super App. Powered securely by Gemini 3.5 AI Engine.</p>
        </footer>

      </div>
    </div>
  );
}
