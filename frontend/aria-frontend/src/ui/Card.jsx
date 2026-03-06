export function Card({ children, className = "" }) {
  return (
    <div className={`rounded-2xl border border-white/10 bg-white/5 p-4 ${className}`}>
      {children}
    </div>
  );
}

export function CardTitle({ children }) {
  return <div className="text-sm text-white/60">{children}</div>;
}

export function CardValue({ children }) {
  return <div className="mt-1 text-2xl font-semibold tracking-tight">{children}</div>;
}