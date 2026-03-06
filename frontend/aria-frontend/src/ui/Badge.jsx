export function Badge({ children, tone = "default" }) {
  const tones = {
    default: "bg-white/10 text-white/80",
    draft: "bg-yellow-500/15 text-yellow-200 border border-yellow-500/20",
    submitted: "bg-blue-500/15 text-blue-200 border border-blue-500/20",
    published: "bg-green-500/15 text-green-200 border border-green-500/20",
  };

  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs ${tones[tone]}`}>
      {children}
    </span>
  );
}