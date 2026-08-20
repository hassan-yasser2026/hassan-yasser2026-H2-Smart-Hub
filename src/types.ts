export interface UserAccount {
  username: "Hassan" | "Jana" | "Guest";
  avatar: string;
  color: string;
  tagline: string;
  isVIP: boolean;
}

export interface ChatMessage {
  id: string;
  role: "user" | "model";
  text: string;
  timestamp: string;
  attachedFile?: {
    mimeType: string;
    data: string; // Base64 or local URL
    name: string;
  };
}

export interface ChatSession {
  id: string;
  title: string;
  messages: ChatMessage[];
  personaId: string;
  createdAt: string;
}

export interface AIPersona {
  id: string;
  name: string;
  nameAr: string;
  icon: string;
  description: string;
  tagline: string;
  color: string;
}

export interface TaskItem {
  id: string;
  text: string;
  completed: boolean;
  category: string;
  dueDate?: string;
}

export interface PlannerEvent {
  time: string;
  title: string;
  duration: string;
  description: string;
  category: "study" | "health" | "leisure" | "coding" | "business" | string;
}

export interface SyncState {
  chatSessions: ChatSession[];
  tasks: TaskItem[];
  plannerEvents: PlannerEvent[];
  lastSynced: string;
}
