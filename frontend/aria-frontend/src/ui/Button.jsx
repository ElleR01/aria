export function Button({ children, onClick, className = "", variant = "primary", ...props }) {
  const base =
    "inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition active:scale-[0.99]";

  const variants = {
    primary: "bg-white text-slate-900 hover:bg-white/90",
    ghost: "bg-white/0 text-white hover:bg-white/10 border border-white/10",
    danger: "bg-red-500 text-white hover:bg-red-500/90",
  };

  return (
    <button 
    onClick={onClick}
    className={`${base} ${variants[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
}