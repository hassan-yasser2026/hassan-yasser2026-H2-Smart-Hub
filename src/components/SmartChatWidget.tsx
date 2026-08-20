import React, { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "motion/react";
import { 
  Sparkles, Send, Trash2, Mic, MicOff, Volume2, VolumeX, RefreshCw, 
  CloudLightning, CloudOff, CloudCheck, User, Users, ChevronLeft, ChevronRight,
  ShieldAlert, Bot, HelpCircle, CheckCircle, Play, Square, Settings, RefreshCwIcon,
  Paperclip, X
} from "lucide-react";
import { ChatSession, ChatMessage, UserAccount, AIPersona } from "../types";
import { AI_PERSONAS } from "../data/personas";
// @ts-ignore
import hassanAvatar from "../assets/images/hassan_avatar_1783868972463.jpg";

interface SmartChatWidgetProps {
  currentUser: UserAccount;
  onUserChange: (user: UserAccount) => void;
  onSyncTrigger: () => void;
  syncStatus: "idle" | "syncing" | "success" | "error";
  syncTime: string;
}

export default function SmartChatWidget({ 
  currentUser, 
  onUserChange, 
  onSyncTrigger,
  syncStatus,
  syncTime 
}: SmartChatWidgetProps) {
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<string>("");
  const [inputText, setInputText] = useState("");
  const [selectedPersonaId, setSelectedPersonaId] = useState("default");
  const [isLoading, setIsLoading] = useState(false);

  // File Upload & Drag/Drop State
  const [attachedFile, setAttachedFile] = useState<{
    mimeType: string;
    data: string; // Base64
    name: string;
  } | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  
  // TTS State
  const [playingMessageId, setPlayingMessageId] = useState<string | null>(null);
  const currentAudioRef = useRef<HTMLAudioElement | null>(null);
  const [ttsFeedback, setTtsFeedback] = useState<string | null>(null);

  // Dictation / Speech recognition State
  const [isListening, setIsListening] = useState(false);
  const recognitionRef = useRef<any>(null);

  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  // Load chat sessions from local storage and try to sync with cloud on mount
  useEffect(() => {
    const saved = localStorage.getItem(`hj_sessions_${currentUser.username}`);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        setSessions(parsed);
        if (parsed.length > 0) {
          setActiveSessionId(parsed[0].id);
        }
      } catch (e) {
        console.error("Failed to parse sessions", e);
      }
    } else {
      // Create default session
      const defaultSess: ChatSession = {
        id: "default-session",
        title: "محادثة جديدة",
        messages: [
          {
            id: "welcome",
            role: "model",
            text: `أهلاً بك يا ${currentUser.username === "Guest" ? "ضيفنا العزيز" : currentUser.username}! أنا مساعد H&J الذكي ومستشارك الرقمي الموثوق. كيف يمكنني مساعدتك اليوم؟ يرجى اختيار الشخصية المناسبة لسؤالك من الشريط العلوي.`,
            timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
          }
        ],
        personaId: "default",
        createdAt: new Date().toISOString()
      };
      setSessions([defaultSess]);
      setActiveSessionId(defaultSess.id);
    }
  }, [currentUser]);

  // Save sessions to localStorage whenever they change
  useEffect(() => {
    if (sessions.length > 0) {
      localStorage.setItem(`hj_sessions_${currentUser.username}`, JSON.stringify(sessions));
    }
  }, [sessions, currentUser]);

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [sessions, activeSessionId, isLoading]);

  const fallbackSession: ChatSession = {
    id: "fallback-session",
    title: "محادثة جديدة",
    messages: [],
    personaId: "default",
    createdAt: new Date().toISOString()
  };

  const activeSession = sessions.find(s => s.id === activeSessionId) || sessions[0] || fallbackSession;

  // Start speech recognition
  const toggleListening = () => {
    if (isListening) {
      recognitionRef.current?.stop();
      setIsListening(false);
      return;
    }

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert("متصفحك الحالي لا يدعم ميزة الإدخال الصوتي بالذكاء الاصطناعي.");
      return;
    }

    const rec = new SpeechRecognition();
    rec.continuous = false;
    rec.lang = "ar-EG"; // Arabic speech recognition
    rec.interimResults = false;

    rec.onstart = () => {
      setIsListening(true);
    };

    rec.onresult = (e: any) => {
      const transcript = e.results[0][0].transcript;
      if (transcript) {
        setInputText(prev => prev + " " + transcript);
      }
    };

    rec.onerror = (err: any) => {
      console.error("Speech recognition error", err);
      setIsListening(false);
    };

    rec.onend = () => {
      setIsListening(false);
    };

    recognitionRef.current = rec;
    rec.start();
  };

  // Speak message text using Gemini TTS API with browser fallback
  const handleTTS = async (messageId: string, text: string) => {
    // If already playing this message, stop it
    if (playingMessageId === messageId) {
      if (currentAudioRef.current) {
        currentAudioRef.current.pause();
      }
      setPlayingMessageId(null);
      return;
    }

    // Stop any current playing audio
    if (currentAudioRef.current) {
      currentAudioRef.current.pause();
    }

    setPlayingMessageId(messageId);
    setTtsFeedback("جاري تخليق الصوت الفني بالذكاء الاصطناعي...");

    try {
      const res = await fetch("/api/tts", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ 
          text: text.slice(0, 300), // Limiting length to prevent excessively large payload
          voice: selectedPersonaId === "teacher" ? "Puck" : "Zephyr" 
        })
      });

      const data = await res.json();
      if (res.ok && data.audio) {
        const audioUrl = `data:audio/wav;base64,${data.audio}`;
        const audio = new Audio(audioUrl);
        currentAudioRef.current = audio;
        
        audio.onended = () => {
          setPlayingMessageId(null);
          setTtsFeedback(null);
        };

        await audio.play();
        setTtsFeedback("مستمر بالقراءة...");
      } else {
        throw new Error(data.error || "Failed to generate TTS audio.");
      }
    } catch (err: any) {
      console.warn("Gemini TTS API failed, using browser native speech synthesis fallback:", err.message);
      
      // Browser SpeechSynthesis Fallback
      if ("speechSynthesis" in window) {
        const utterance = new SpeechSynthesisUtterance(text.slice(0, 400));
        utterance.lang = "ar-SA"; // Arabic voice
        utterance.rate = 1.0;
        
        utterance.onend = () => {
          setPlayingMessageId(null);
          setTtsFeedback(null);
        };

        window.speechSynthesis.speak(utterance);
        setTtsFeedback("مستمر بالقراءة الصوتية المحلية...");
      } else {
        setTtsFeedback("الخدمة الصوتية غير متوفرة في متصفحك حالياً.");
        setTimeout(() => setTtsFeedback(null), 3000);
        setPlayingMessageId(null);
      }
    }
  };

  // Send message
  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if ((!inputText.trim() && !attachedFile) || isLoading) return;

    const userMsgText = inputText.trim() || `تحليل الملف المرفوع: ${attachedFile ? attachedFile.name : ""}`;
    const fileToSend = attachedFile;

    setInputText("");
    setAttachedFile(null);
    setIsLoading(true);

    // Create unique ID for message
    const userMsgId = "msg-" + Date.now();
    const newUserMessage: ChatMessage = {
      id: userMsgId,
      role: "user",
      text: userMsgText,
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
      attachedFile: fileToSend ? { ...fileToSend } : undefined
    };

    // Update active session with user message
    let updatedSessions = sessions.map(s => {
      if (s.id === activeSessionId) {
        return {
          ...s,
          personaId: selectedPersonaId,
          messages: [...s.messages, newUserMessage]
        };
      }
      return s;
    });
    setSessions(updatedSessions);

    try {
      // API call to custom Express endpoint
      const res = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          message: userMsgText,
          history: activeSession.messages,
          username: currentUser.username,
          persona: selectedPersonaId,
          attachedFile: fileToSend ? {
            mimeType: fileToSend.mimeType,
            data: fileToSend.data,
            name: fileToSend.name
          } : undefined
        })
      });

      const data = await res.json();

      if (!res.ok) {
        // Show safety error or any other server message directly
        throw new Error(data.error || "Server responded with an error");
      }

      const modelMsg: ChatMessage = {
        id: "msg-reply-" + Date.now(),
        role: "model",
        text: data.reply,
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
      };

      setSessions(prev => prev.map(s => {
        if (s.id === activeSessionId) {
          // If the title is "محادثة جديدة" or simple, rename it based on the first query
          const currentTitle = s.title === "محادثة جديدة" 
            ? (userMsgText.length > 20 ? userMsgText.slice(0, 20) + "..." : userMsgText)
            : s.title;

          return {
            ...s,
            title: currentTitle,
            messages: [...s.messages, modelMsg]
          };
        }
        return s;
      }));

    } catch (error: any) {
      console.error("Chat error:", error);
      const errorMsg: ChatMessage = {
        id: "msg-err-" + Date.now(),
        role: "model",
        text: `⚠️ تنبيه حماية وسلامة:\n${error.message || "حدث خطأ غير متوقع أثناء المعالجة."}`,
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
      };

      setSessions(prev => prev.map(s => {
        if (s.id === activeSessionId) {
          return {
            ...s,
            messages: [...s.messages, errorMsg]
          };
        }
        return s;
      }));
    } finally {
      setIsLoading(false);
    }
  };

  // Helper file loaders and processing functions
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    processFile(file);
  };

  const processFile = (file: File) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      const base64Data = reader.result as string;
      const commaIdx = base64Data.indexOf(",");
      const dataOnly = commaIdx !== -1 ? base64Data.substring(commaIdx + 1) : base64Data;
      
      setAttachedFile({
        mimeType: file.type,
        data: dataOnly,
        name: file.name
      });
    };
    reader.readAsDataURL(file);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (file) {
      processFile(file);
    }
  };

  // Create new session
  const handleCreateNewSession = () => {
    const newSessId = "session-" + Date.now();
    const newSess: ChatSession = {
      id: newSessId,
      title: "محادثة جديدة",
      messages: [
        {
          id: "welcome-" + Date.now(),
          role: "model",
          text: `أهلاً بك في جلسة ذكية جديدة. أنا مستعد لمساعدتك بالكامل تحت إشراف الشخصية المختارة. اطرح تساؤلك وسأجيبك بأمان ومسؤولية.`,
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
        }
      ],
      personaId: selectedPersonaId,
      createdAt: new Date().toISOString()
    };
    setSessions([newSess, ...sessions]);
    setActiveSessionId(newSessId);
  };

  // Clear current history
  const handleClearHistory = () => {
    if (confirm("هل تريد بالتأكيد مسح كافة المحادثات من السجل؟")) {
      const cleared: ChatSession = {
        id: "session-cleared-" + Date.now(),
        title: "محادثة جديدة",
        messages: [
          {
            id: "welcome-cleared",
            role: "model",
            text: "تم مسح السجل بنجاح. كيف يمكنني إرشادك الآن؟",
            timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
          }
        ],
        personaId: "default",
        createdAt: new Date().toISOString()
      };
      setSessions([cleared]);
      setActiveSessionId(cleared.id);
    }
  };

  const selectedPersona = AI_PERSONAS.find(p => p.id === selectedPersonaId) || AI_PERSONAS[0];

  return (
    <div className="bg-neutral-900/60 rounded-2xl border border-neutral-800 backdrop-blur-md overflow-hidden grid grid-cols-1 lg:grid-cols-4 min-h-[620px] max-h-[700px] shadow-2xl relative">
      
      {/* Collapsible Left Pane: Chat history list + account profiles switcher */}
      <div className="lg:col-span-1 border-r border-neutral-800 bg-neutral-950/80 flex flex-col h-full overflow-hidden">
        
        {/* User Account Switcher Header */}
        <div className="p-4 border-b border-neutral-800 bg-neutral-900/40">
          <label className="text-[10px] font-mono tracking-widest text-neutral-500 uppercase block mb-2 text-right">
            الملف الشخصي النشط (المزامنة السحابية)
          </label>
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              {currentUser.avatar.includes("/") || currentUser.avatar.startsWith("data:") ? (
                <img 
                  src={currentUser.avatar} 
                  alt={currentUser.username} 
                  className="h-8 w-8 rounded-full object-cover shadow-lg shadow-blue-500/10"
                  referrerPolicy="no-referrer"
                />
              ) : (
                <span className={`h-8 w-8 rounded-full flex items-center justify-center text-white font-bold bg-gradient-to-tr ${currentUser.color} shadow-lg shadow-blue-500/10`}>
                  {currentUser.username[0]}
                </span>
              )}
              <div className="text-right">
                <div className="flex items-center gap-1 justify-end">
                  {currentUser.isVIP && (
                    <span className="text-[8px] bg-amber-500/20 text-amber-400 px-1 rounded font-bold">VIP</span>
                  )}
                  <h4 className="text-sm font-semibold text-white leading-tight">{currentUser.username}</h4>
                </div>
                <p className="text-[10px] text-neutral-400">{currentUser.tagline}</p>
              </div>
            </div>
            
            {/* Switcher Button with Guest Support */}
            <div className="flex gap-1 items-center">
              <button 
                onClick={() => onUserChange({ username: "Hassan", avatar: hassanAvatar, color: "from-blue-600 to-indigo-700", tagline: "المطور الرئيسي ومؤسس المنصة", isVIP: true })}
                className={`h-7 w-7 rounded-full border transition-all overflow-hidden flex items-center justify-center ${currentUser.username === "Hassan" ? "border-blue-500 ring-1 ring-blue-500" : "border-neutral-800 hover:border-neutral-700"}`}
                title="حساب Hassan (VIP)"
              >
                <img src={hassanAvatar} alt="Hassan" className="h-full w-full object-cover" />
              </button>
              <button 
                onClick={() => onUserChange({ username: "Jana", avatar: "J", color: "from-fuchsia-600 to-pink-700", tagline: "محللة ومصممة المنصة", isVIP: true })}
                className={`h-7 w-7 rounded-full border transition-all text-[11px] flex items-center justify-center font-bold ${currentUser.username === "Jana" ? "bg-fuchsia-600 border-fuchsia-500 text-white shadow-md" : "border-neutral-800 text-neutral-400 hover:bg-neutral-800"}`}
                title="حساب Jana (VIP)"
              >
                J
              </button>
              <button 
                onClick={() => onUserChange({ username: "Guest", avatar: "G", color: "from-amber-600 to-yellow-500", tagline: "عضو زائر (لتجربة القيود)", isVIP: false })}
                className={`h-7 w-7 rounded-full border transition-all text-[11px] flex items-center justify-center font-bold ${currentUser.username === "Guest" ? "bg-amber-600 border-amber-500 text-white shadow-md" : "border-neutral-800 text-neutral-400 hover:bg-neutral-800"}`}
                title="حساب زائر (محدود)"
              >
                G
              </button>
            </div>
          </div>

          {/* Real Cloud Sync Status Indicator */}
          <div className="mt-3 pt-2 border-t border-neutral-900 flex items-center justify-between text-[11px] font-mono text-neutral-400">
            <div className="flex items-center gap-1.5">
              {syncStatus === "syncing" && <RefreshCw className="h-3 w-3 text-cyan-400 animate-spin" />}
              {syncStatus === "success" && <CloudCheck className="h-3 w-3 text-emerald-400" />}
              {syncStatus === "error" && <CloudOff className="h-3 w-3 text-rose-400" />}
              {syncStatus === "idle" && <CloudCheck className="h-3 w-3 text-blue-400" />}
              <span>{syncStatus === "syncing" ? "جاري الحفظ..." : "مزامنة سحابية نشطة"}</span>
            </div>
            <button 
              onClick={onSyncTrigger}
              className="hover:text-white hover:underline flex items-center gap-1"
              title="تحديث المزامنة الآن"
            >
              <RefreshCw className="h-2.5 w-2.5" />
              <span>{syncTime ? syncTime : "مزامنة"}</span>
            </button>
          </div>
        </div>

        {/* List of Chat Sessions */}
        <div className="flex-1 overflow-y-auto p-3 space-y-1">
          <div className="flex items-center justify-between px-2 mb-2">
            <span className="text-xs font-mono text-neutral-500">الجلسات التاريخية</span>
            <button 
              onClick={handleCreateNewSession}
              className="text-xs bg-blue-600/10 text-blue-400 hover:bg-blue-600/25 border border-blue-500/20 px-2 py-0.5 rounded font-medium transition"
            >
              + جلسة جديدة
            </button>
          </div>

          <AnimatePresence>
            {sessions.map(s => {
              const isActive = s.id === activeSessionId;
              const hasErrors = s.messages.some(m => m.text.includes("⚠️"));
              return (
                <motion.button
                  key={s.id}
                  onClick={() => setActiveSessionId(s.id)}
                  className={`w-full text-right p-3 rounded-xl border transition-all flex items-center justify-between gap-2 ${
                    isActive 
                      ? "bg-neutral-900 border-neutral-700/80 text-white shadow-lg" 
                      : "border-transparent text-neutral-400 hover:bg-neutral-900/30 hover:text-neutral-200"
                  }`}
                  whileHover={{ scale: 1.01 }}
                  whileTap={{ scale: 0.99 }}
                >
                  <div className="flex items-center gap-2 overflow-hidden">
                    <span className={`h-2 w-2 rounded-full ${hasErrors ? "bg-rose-500" : isActive ? "bg-cyan-400" : "bg-neutral-700"}`} />
                    <span className="text-xs font-medium truncate block">{s.title}</span>
                  </div>
                  <span className="text-[10px] font-mono text-neutral-600 block">
                    {s.messages.length} رسالة
                  </span>
                </motion.button>
              );
            })}
          </AnimatePresence>
        </div>

        {/* Clear Data Footer */}
        <div className="p-3 border-t border-neutral-800 bg-neutral-900/20">
          <button 
            onClick={handleClearHistory}
            className="w-full py-1.5 px-3 rounded-lg border border-neutral-800 text-xs font-mono text-neutral-500 hover:border-rose-500/30 hover:text-rose-400 transition flex items-center justify-center gap-1.5"
          >
            <Trash2 className="h-3 w-3" />
            <span>تفريغ كافة المحادثات</span>
          </button>
        </div>
      </div>

      {/* Main Chat Box Pane */}
      <div 
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className="lg:col-span-3 flex flex-col h-full bg-neutral-900/40 relative"
      >
        {/* Drag Overlay */}
        {isDragging && (
          <div className="absolute inset-0 bg-neutral-950/95 backdrop-blur-md border-2 border-dashed border-cyan-500 m-3 rounded-2xl z-50 flex flex-col items-center justify-center gap-3 text-white pointer-events-none text-center p-6">
            <Paperclip className="h-12 w-12 text-cyan-400 animate-bounce" />
            <p className="text-lg font-bold">أفلت صورتك أو ملفك الصوتي هنا</p>
            <p className="text-xs text-neutral-400">سيتم رفع الملف فورياً وبأمان للتحليل والتقييم الصريح والذكي بواسطة الذكاء الاصطناعي</p>
          </div>
        )}
        
        {/* Persona Selectors Top Bar */}
        <div className="p-3 border-b border-neutral-800 bg-neutral-950/40 flex flex-col gap-2 relative z-20">
          <div className="flex items-center justify-between">
            <span className="text-xs font-mono text-neutral-400 flex items-center gap-1.5 text-right">
              <Bot className="h-4 w-4 text-cyan-400 animate-pulse" />
              أختر شخصية الذكاء الاصطناعي (AI Persona) المتخصصة:
            </span>
            <div className="bg-emerald-500/10 border border-emerald-500/20 rounded px-2 py-0.5 text-[10px] font-mono text-emerald-400 flex items-center gap-1">
              <CheckCircle className="h-2.5 w-2.5" />
              <span>جدار الحماية الفعال نشط</span>
            </div>
          </div>
          
          <div className="flex gap-1.5 overflow-x-auto pb-1 mt-1 scrollbar-thin">
            {AI_PERSONAS.map(p => {
              const isSelected = p.id === selectedPersonaId;
              return (
                <button
                  key={p.id}
                  onClick={() => {
                    setSelectedPersonaId(p.id);
                    // Update current session persona if clean
                    if (activeSession.messages.length <= 1) {
                      setSessions(prev => prev.map(s => {
                        if (s.id === activeSessionId) {
                          return { ...s, personaId: p.id };
                        }
                        return s;
                      }));
                    }
                  }}
                  className={`flex-shrink-0 px-3 py-1.5 rounded-xl border text-xs font-medium transition-all flex items-center gap-1.5 ${
                    isSelected 
                      ? `bg-gradient-to-r ${p.color} border-transparent text-white shadow-lg` 
                      : "bg-neutral-900 border-neutral-800 text-neutral-400 hover:border-neutral-700 hover:text-neutral-200"
                  }`}
                >
                  <span className="text-sm">
                    {p.id === "default" && "✨"}
                    {p.id === "teacher" && "🎓"}
                    {p.id === "coder" && "💻"}
                    {p.id === "doctor" && "❤️"}
                    {p.id === "advisor" && "💼"}
                    {p.id === "designer" && "🎨"}
                  </span>
                  <span>{p.nameAr.split(" ")[0]}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Current Active Persona Info banner */}
        <div className={`px-4 py-2 bg-gradient-to-r ${selectedPersona.color} bg-opacity-5 border-b border-neutral-800/60 flex items-center justify-between`}>
          <div className="text-right">
            <span className="text-[10px] font-mono uppercase tracking-wider text-neutral-400">الشخصية الحالية: {selectedPersona.name}</span>
            <p className="text-[11px] text-neutral-200">{selectedPersona.description}</p>
          </div>
        </div>

        {/* Dynamic TTS Floating Notification */}
        {ttsFeedback && (
          <div className="absolute top-28 left-4 right-4 z-30 bg-neutral-950/90 border border-cyan-500/30 p-2 rounded-xl flex items-center justify-between text-xs font-mono text-cyan-400 shadow-xl">
            <div className="flex items-center gap-2">
              <span className="h-2 w-2 rounded-full bg-cyan-500 animate-ping" />
              <span>{ttsFeedback}</span>
            </div>
            <button 
              onClick={() => {
                if (currentAudioRef.current) currentAudioRef.current.pause();
                setPlayingMessageId(null);
                setTtsFeedback(null);
              }}
              className="hover:text-white hover:underline bg-neutral-900 px-2 py-0.5 rounded border border-neutral-800"
            >
              إيقاف
            </button>
          </div>
        )}

        {/* Chat History Pane */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {activeSession.messages.map((msg, i) => {
            const isUser = msg.role === "user";
            const isError = msg.text.includes("⚠️");
            const isPlaying = playingMessageId === msg.id;

            return (
              <motion.div
                key={msg.id || i}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3 }}
                className={`flex ${isUser ? "justify-end" : "justify-start"}`}
              >
                <div className={`max-w-[85%] rounded-2xl p-4 shadow-md border ${
                  isUser 
                    ? "bg-blue-600/20 border-blue-500/20 text-white rounded-br-none" 
                    : isError 
                      ? "bg-rose-950/40 border-rose-500/30 text-rose-200 rounded-bl-none"
                      : "bg-neutral-950/40 border-neutral-800 text-neutral-200 rounded-bl-none"
                }`}>
                  {/* Message Sender Title */}
                  <div className="flex items-center justify-between gap-4 mb-1.5 pb-1 border-b border-neutral-800/40 text-[10px] font-mono text-neutral-500">
                    <span className="font-semibold text-neutral-400">
                      {isUser ? currentUser.username : selectedPersona.name}
                    </span>
                    <div className="flex items-center gap-2">
                      <span>{msg.timestamp}</span>
                      {/* Speech reading capability for model responses */}
                      {!isUser && !isError && (
                        <button
                          onClick={() => handleTTS(msg.id, msg.text)}
                          className={`p-1 rounded transition-colors ${isPlaying ? "text-cyan-400 bg-cyan-500/10" : "text-neutral-500 hover:text-neutral-300 hover:bg-neutral-800"}`}
                          title="استمع للمقالة بصوت AI"
                        >
                          {isPlaying ? <Square className="h-3 w-3 fill-cyan-400" /> : <Volume2 className="h-3 w-3" />}
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Message body */}
                  <div className="text-sm leading-relaxed whitespace-pre-wrap text-right" dir="rtl">
                    {msg.text}
                  </div>

                  {/* Attached File Previews inside bubble */}
                  {msg.attachedFile && msg.attachedFile.mimeType.startsWith("image/") && (
                    <div className="mt-3 rounded-xl overflow-hidden border border-neutral-800 bg-neutral-900/60 max-w-sm mr-auto">
                      <img 
                        src={`data:${msg.attachedFile.mimeType};base64,${msg.attachedFile.data}`} 
                        alt={msg.attachedFile.name} 
                        className="w-full h-auto object-cover max-h-60"
                        referrerPolicy="no-referrer"
                      />
                      <div className="p-2 text-[10px] text-neutral-400 font-mono truncate bg-neutral-950/40 text-right" dir="rtl">
                        📷 {msg.attachedFile.name}
                      </div>
                    </div>
                  )}

                  {msg.attachedFile && msg.attachedFile.mimeType.startsWith("audio/") && (
                    <div className="mt-3 p-2 rounded-xl border border-neutral-800 bg-neutral-950/60 flex flex-col gap-1 max-w-sm mr-auto text-right" dir="rtl">
                      <div className="flex items-center gap-2 text-xs text-neutral-300">
                        <Volume2 className="h-4 w-4 text-cyan-400" />
                        <span className="truncate">{msg.attachedFile.name}</span>
                      </div>
                      <audio 
                        controls 
                        src={`data:${msg.attachedFile.mimeType};base64,${msg.attachedFile.data}`} 
                        className="w-full h-8 mt-1 accent-cyan-500 text-xs" 
                      />
                    </div>
                  )}

                  {msg.attachedFile && !msg.attachedFile.mimeType.startsWith("image/") && !msg.attachedFile.mimeType.startsWith("audio/") && (
                    <div className="mt-3 p-2 rounded-xl border border-neutral-800 bg-neutral-950/60 flex items-center gap-2 text-xs text-neutral-300 max-w-sm mr-auto text-right" dir="rtl">
                      <Paperclip className="h-4 w-4 text-indigo-400" />
                      <span className="truncate">{msg.attachedFile.name}</span>
                    </div>
                  )}

                  {/* Persona-specific Tutor Suggestion banner inside Chat messages */}
                  {!isUser && selectedPersonaId === "teacher" && msg.text.includes("خطوة") && (
                    <div className="mt-2 pt-2 border-t border-emerald-500/20 text-[10px] text-emerald-400 flex items-center gap-1.5">
                      <GraduationCap className="h-3.5 w-3.5" />
                      <span>مساعد التدريس النشط: تواصل معي لحل التمرين خطوة بخطوة.</span>
                    </div>
                  )}
                </div>
              </motion.div>
            );
          })}

          {/* Loading bubble */}
          {isLoading && (
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="flex justify-start"
            >
              <div className="bg-neutral-950/40 border border-neutral-800 rounded-2xl rounded-bl-none p-4 max-w-[80%] flex items-center gap-3">
                <div className="flex gap-1.5">
                  <span className="h-2.5 w-2.5 bg-blue-500 rounded-full animate-bounce [animation-delay:-0.3s]" />
                  <span className="h-2.5 w-2.5 bg-cyan-500 rounded-full animate-bounce [animation-delay:-0.15s]" />
                  <span className="h-2.5 w-2.5 bg-indigo-500 rounded-full animate-bounce" />
                </div>
                <span className="text-xs font-mono text-neutral-400">جاري تفكير وصياغة إجابة آمنة...</span>
              </div>
            </motion.div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Safety & Academic Integrity Disclaimer banner above Input */}
        <div className="px-4 py-1.5 bg-neutral-950/60 border-t border-neutral-800 text-[11px] text-neutral-400 flex items-center justify-between gap-4">
          <div className="flex items-center gap-1">
            <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="font-mono text-emerald-400">Strict Safety Guard Active</span>
          </div>
          <span className="text-right">يُحظر تماماً توليد نصوص غش مباشر أو صور غير أخلاقية.</span>
        </div>

        {/* Selected file attachment preview bar */}
        {attachedFile && (
          <div className="px-4 py-2 border-t border-neutral-800 bg-neutral-950/60 flex items-center justify-between gap-4 animate-fade-in">
            <div className="flex items-center gap-2.5 overflow-hidden">
              {attachedFile.mimeType.startsWith("image/") ? (
                <img 
                  src={`data:${attachedFile.mimeType};base64,${attachedFile.data}`} 
                  alt="preview" 
                  className="h-10 w-10 object-cover rounded-lg border border-neutral-700"
                  referrerPolicy="no-referrer"
                />
              ) : attachedFile.mimeType.startsWith("audio/") ? (
                <div className="h-10 w-10 rounded-lg border border-neutral-700 bg-cyan-950/40 flex items-center justify-center">
                  <Volume2 className="h-5 w-5 text-cyan-400" />
                </div>
              ) : (
                <div className="h-10 w-10 rounded-lg border border-neutral-700 bg-neutral-800 flex items-center justify-center">
                  <Paperclip className="h-5 w-5 text-neutral-400" />
                </div>
              )}
              <div className="text-right overflow-hidden">
                <span className="text-xs font-semibold text-neutral-200 block truncate">{attachedFile.name}</span>
                <span className="text-[10px] text-neutral-500 uppercase block font-mono">
                  {attachedFile.mimeType.split("/")[0]} • {(attachedFile.data.length * 0.75 / 1024).toFixed(1)} KB
                </span>
              </div>
            </div>
            
            <button 
              type="button"
              onClick={() => setAttachedFile(null)}
              className="p-1.5 rounded-lg bg-neutral-900 border border-neutral-800 text-neutral-400 hover:text-rose-400 hover:border-rose-500/20 transition-colors"
              title="إزالة المرفق"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        )}

        {/* Input Text Form */}
        <form onSubmit={handleSendMessage} className="p-4 border-t border-neutral-800 bg-neutral-950/40 flex gap-2 items-center">
          
          {/* Hidden File Input */}
          <input 
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="image/*,audio/*"
            className="hidden"
          />

          {/* Upload Button */}
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="p-3 rounded-xl border bg-neutral-900 border-neutral-800 text-neutral-400 hover:border-neutral-700 hover:text-white transition-all flex items-center justify-center"
            title="رفع صورة أو مقطع صوتي للتحليل"
          >
            <Paperclip className="h-4 w-4" />
          </button>

          {/* Micro Button for Speech Dictation */}
          <button
            type="button"
            onClick={toggleListening}
            className={`p-3 rounded-xl border transition-all ${
              isListening 
                ? "bg-rose-600/20 border-rose-500 text-rose-400 animate-pulse shadow-lg" 
                : "bg-neutral-900 border-neutral-800 text-neutral-400 hover:border-neutral-700 hover:text-white"
            }`}
            title="إملاء صوتي بالذكاء الاصطناعي"
          >
            {isListening ? <MicOff className="h-4 w-4" /> : <Mic className="h-4 w-4" />}
          </button>
 
          {/* Text Input Box */}
          <input
            type="text"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            placeholder={isListening ? "جاري الاستماع لصوتك..." : "اسأل H&J Smart Hub أو ارفع صورة/ملف صوتي..."}
            disabled={isLoading}
            className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-cyan-500 transition-colors placeholder:text-neutral-500 text-right"
          />
 
          {/* Submit Button */}
          <button
            type="submit"
            disabled={(!inputText.trim() && !attachedFile) || isLoading}
            className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 disabled:from-neutral-800 disabled:to-neutral-800 disabled:text-neutral-500 disabled:cursor-not-allowed text-white p-3 rounded-xl font-medium transition-all shadow-md flex items-center justify-center"
          >
            <Send className="h-4 w-4" />
          </button>
        </form>
      </div>

    </div>
  );
}

// Minimal icons wrapper for component
function GraduationCap(props: any) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      {...props}
    >
      <path d="M21.42 10.922a1 1 0 0 0-.019-1.838L12.83 5.18a2 2 0 0 0-1.66 0L2.6 9.08a1 1 0 0 0 0 1.832l8.57 3.908a2 2 0 0 0 1.66 0z" />
      <path d="M6 12v5c0 2 2 3 6 3s6-1 6-3v-5" />
    </svg>
  );
}
