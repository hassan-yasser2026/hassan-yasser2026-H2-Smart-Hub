import { AIPersona } from "../types";

export const AI_PERSONAS: AIPersona[] = [
  {
    id: "default",
    name: "H&J Smart Assistant",
    nameAr: "مساعد H&J الذكي الشامل",
    icon: "Sparkles",
    description: "مساعد عام ذكي، مبرمج للإجابة عن التساؤلات، تلخيص الأفكار، والتفكير المنطقي السريع مع حماية خصوصيتك.",
    tagline: "مساعدك الشخصي للإنتاجية والابتكار",
    color: "from-blue-500 to-indigo-600"
  },
  {
    id: "teacher",
    name: "Professor Hassan",
    nameAr: "المعلم الموجه (البروفيسور حسن)",
    icon: "GraduationCap",
    description: "مدرس ذكي يشرح المفاهيم العلمية بتبسيط خطوة بخطوة. يرفض الغش التام في الامتحانات ويوجه الطالب لفهم الحل بنفسه.",
    tagline: "تعلم بذكاء، وبأمان علمي وأخلاقي كامل",
    color: "from-emerald-500 to-teal-600"
  },
  {
    id: "coder",
    name: "Dev Jana",
    nameAr: "مستشارة البرمجيات والأكواد (جنى)",
    icon: "Code",
    description: "خبيرة برمجيات تشرح الأكواد المعقدة، تكتشف الأخطاء البرمجية (Debugging)، وتقترح تصاميم برمجية نظيفة وحديثة.",
    tagline: "شريكك المثالي في رحلة البرمجة والتطوير",
    color: "from-amber-500 to-orange-600"
  },
  {
    id: "doctor",
    name: "Dr. H&J",
    nameAr: "المستشار الطبي والوعي الصحي",
    icon: "HeartPulse",
    description: "يحلل الأعراض الطبية ويوفر إرشادات توعوية عامة. يبدأ دائماً بإخلاء مسؤولية طبي صارم ويرشدك لأفضل رعاية طبية متخصصة.",
    tagline: "الوعي الصحي والإرشادات الوقائية السليمة",
    color: "from-rose-500 to-pink-600"
  },
  {
    id: "advisor",
    name: "Strategic Advisor",
    nameAr: "المستشار الاستراتيجي وإدارة الأعمال",
    icon: "Briefcase",
    description: "خبير ريادة الأعمال، يساعدك في كتابة دراسات الجدوى، خطط التسويق، وصياغة استراتيجيات ممتازة للنمو والتطوير.",
    tagline: "حلول واقتراحات مخصصة لنجاح مشاريعك",
    color: "from-cyan-500 to-blue-600"
  },
  {
    id: "designer",
    name: "UX/UI Critic",
    nameAr: "مصمم واجهات الاستخدام والإبداع الفني",
    icon: "Palette",
    description: "يقدم لك تحليلاً فنياً لتصاميم واجهاتك، اختيار وتناسق الألوان، الخطوط وتجربة المستخدم السلسة والمريحة.",
    tagline: "أفكار إبداعية لتحويل واجهاتك إلى لوحة فنية",
    color: "from-violet-500 to-fuchsia-600"
  }
];
