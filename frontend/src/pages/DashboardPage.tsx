import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import logo from '../assets/redmeiIcon.png';

export const DashboardPage = () => {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen flex flex-col bg-background text-primary">
      {/* Top Navbar */}
      <header className="border-b border-subtle bg-[#0F1011] px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <img src={logo} alt="readme.ai Logo" className="w-8 h-8 object-contain" />
          <span className="font-bold text-lg tracking-tight text-gradient">readme.ai</span>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm text-secondary">
            Logged in as <strong className="text-primary">{user?.name || user?.email}</strong>
          </span>
          <button
            onClick={handleLogout}
            className="btn-pill text-xs px-4 py-2 hover:bg-red-500/10 hover:border-red-500/20 hover:text-red-500"
          >
            Logout
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col items-center justify-center p-8 text-center">
        <div className="glow-wrapper max-w-lg w-full">
          <div className="surface-card p-10 flex flex-col items-center gap-4">
            <h2 className="text-2xl font-bold">Workspace Dashboard</h2>
            <p className="text-secondary text-sm">
              Your session is authenticated and active.
            </p>
            <div className="border border-subtle rounded-xl p-4 bg-[#18191B] w-full text-left font-mono text-xs text-cyan flex flex-col gap-2">
              <div>User ID: {user?.id || 'N/A'}</div>
              <div>Email: {user?.email}</div>
              <div>Name: {user?.name}</div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
