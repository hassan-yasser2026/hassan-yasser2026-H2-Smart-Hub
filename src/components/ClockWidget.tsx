import { useState, useEffect } from "react";
import { motion } from "motion/react";
import { Sun, Moon, Sunset, Sunrise, Clock } from "lucide-react";

export default function ClockWidget({ username = "Hassan" }: { username?: string }) {
  const [time, setTime] = useState<Date>(new Date());

  useEffect(() => {
    const timer = setInterval(() => {
      setTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const hours = time.getHours();
  const minutes = time.getMinutes();
  const seconds = time.getSeconds();

  // Determine dynamic greeting and icon
  let greeting = `أهلاً بك يا ${username}`;
  let GreetingIcon = Sun;
  let accentColor = "text-amber-400";
  let bgGradient = "from-amber-500/10 to-transparent";

  if (hours >= 5 && hours < 12) {
    greeting = `صباح الخير والإنتاجية يا ${username}`;
    GreetingIcon = Sunrise;
    accentColor = "text-amber-400";
    bgGradient = "from-amber-400/10 to-transparent";
  } else if (hours >= 12 && hours < 18) {
    greeting = `طاب يومك السعيد يا ${username}`;
    GreetingIcon = Sun;
    accentColor = "text-orange-400";
    bgGradient = "from-orange-400/10 to-transparent";
  } else if (hours >= 18 && hours < 22) {
    greeting = `مساء النور والابتكار يا ${username}`;
    GreetingIcon = Sunset;
    accentColor = "text-rose-400";
    bgGradient = "from-rose-400/10 to-transparent";
  } else {
    greeting = `وقت التركيز الهادئ يا ${username}`;
    GreetingIcon = Moon;
    accentColor = "text-indigo-400";
    bgGradient = "from-indigo-400/10 to-transparent";
  }

  // Calculate day completion percentage
  const totalSecondsInDay = 24 * 60 * 60;
  const currentSecondsInDay = hours * 3600 + minutes * 60 + seconds;
  const dayProgress = (currentSecondsInDay / totalSecondsInDay) * 100;

  // Format digital time
  const formattedTime = time.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: true,
  });

  // Format date
  const formattedDate = time.toLocaleDateString([], {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6 }}
      className={`relative overflow-hidden rounded-2xl border border-neutral-800 bg-neutral-900/60 p-6 backdrop-blur-md`}
    >
      {/* Decorative gradient overlay */}
      <div className={`absolute top-0 left-0 h-32 w-full bg-gradient-to-b ${bgGradient} pointer-events-none`} />

      <div className="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        {/* Left: Dynamic Greeting */}
        <div className="space-y-2">
          <div className="flex items-center gap-3">
            <div className={`rounded-xl bg-neutral-800/80 p-2.5 border border-neutral-700/50 ${accentColor}`}>
              <GreetingIcon className="h-6 w-6" />
            </div>
            <h1 className="text-2xl md:text-3xl font-display font-medium text-neutral-100 tracking-tight">
              {greeting}
            </h1>
          </div>
          <p className="text-sm text-neutral-400 pl-1">
            {formattedDate}
          </p>
        </div>

        {/* Right: Digital Clock & Metrics */}
        <div className="flex flex-col md:items-end justify-center min-w-[200px]">
          <div className="flex items-center gap-2.5 font-mono text-3xl font-semibold text-white tracking-wider">
            <Clock className="h-5 w-5 text-neutral-500 animate-pulse" />
            <span>{formattedTime}</span>
          </div>

          {/* Day Progress bar */}
          <div className="mt-4 w-full md:w-56 space-y-1">
            <div className="flex justify-between text-[11px] font-mono text-neutral-400">
              <span>Day Progress</span>
              <span>{dayProgress.toFixed(1)}%</span>
            </div>
            <div className="h-1.5 w-full bg-neutral-800 rounded-full overflow-hidden">
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${dayProgress}%` }}
                transition={{ duration: 0.5 }}
                className="h-full bg-amber-500 rounded-full"
              />
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}
