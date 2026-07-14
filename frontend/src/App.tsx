import logo from './assets/redmeiIcon.png';

export const App = () => {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-background text-primary p-4">
      <div className="glow-wrapper max-w-md w-full">
        <div className="surface-card p-8 text-center flex flex-col items-center justify-center">
          <img src={logo} alt="readme.ai Logo" className="w-16 h-16 mb-4 object-contain" />
          <h1 className="text-3xl font-extrabold tracking-tight mb-2 text-gradient">
            readme.ai
          </h1>
          <p className="text-secondary text-sm font-semibold tracking-wide uppercase">
            Frontend client initialized
          </p>
        </div>
      </div>
    </div>
  );
};
