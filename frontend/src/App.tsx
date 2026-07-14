export const App = () => {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-background text-foreground p-4">
      <div className="rounded-2xl border border-border bg-muted p-8 text-center shadow-xl max-w-md w-full">
        <h1 className="text-3xl font-extrabold tracking-tight mb-2 text-primary">
          readme.ai
        </h1>
        <p className="text-muted-foreground text-sm font-semibold tracking-wide uppercase">
          Frontend client initialized
        </p>
      </div>
    </div>
  );
};
