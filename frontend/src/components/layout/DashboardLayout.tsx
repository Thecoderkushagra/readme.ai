import { Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';
import { LogOut } from 'lucide-react';
import redmeiIcon from '../../assets/redmeiIcon.png';

export const DashboardLayout = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-[var(--bg-base)] text-[var(--text-primary)] flex flex-col">
      <nav className="border-b border-[var(--border-color)] p-4 flex justify-between items-center bg-[var(--bg-card)]">
        <div className="flex items-center gap-3">
          <img src={redmeiIcon} alt="readme.ai" className="h-8 w-auto object-contain" />
          <span className="text-xl font-bold text-gradient">readme.ai</span>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm font-medium hidden sm:block">{user?.name}</span>
          <button 
            onClick={handleLogout} 
            className="p-2 hover:bg-[var(--bg-hover)] rounded-md transition-colors text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
            title="Logout"
          >
            <LogOut size={20} />
          </button>
        </div>
      </nav>
      <main className="flex-1 w-full max-w-7xl mx-auto p-4 sm:p-6 md:p-8">
        <Outlet />
      </main>
    </div>
  );
};
