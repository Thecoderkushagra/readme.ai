import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useRepositoryStore } from '../store/useRepositoryStore';
import { ImportForm } from '../components/dashboard/ImportForm';
import { Trash2, MessageSquare, RefreshCw } from 'lucide-react';

export const DashboardPage = () => {
  const { repositories, fetchRepositories, removeRepo, isLoading, stopPolling } = useRepositoryStore();
  const navigate = useNavigate();

  useEffect(() => {
    fetchRepositories();
    return () => stopPolling();
  }, [fetchRepositories, stopPolling]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20';
      case 'FAILED': return 'bg-red-500/10 text-red-500 border border-red-500/20';
      case 'CLONING':
      case 'PARSING':
      case 'VECTORIZING':
        return 'bg-amber-500/10 text-amber-500 border border-amber-500/20 animate-pulse';
      default: 
        return 'bg-gray-500/10 text-gray-500 border border-gray-500/20';
    }
  };

  return (
    <div className="animate-fade-in flex flex-col items-center w-full">
      <div className="flex flex-col items-center w-full mb-8">
        <h1 className="text-3xl font-bold mb-8 text-center text-gradient">Your Repositories</h1>
        <ImportForm />
      </div>
      
      {isLoading && repositories.length === 0 ? (
        <div className="flex justify-center mt-10">
           <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full">
          {repositories.map((repo) => (
            <div key={repo.id} className="surface-card flex flex-col p-5 gap-4">
              <div className="flex justify-between items-start gap-2">
                <h3 className="font-semibold text-lg truncate flex-1" title={repo.name}>{repo.name}</h3>
                <span className={`px-2 py-1 rounded text-xs font-medium whitespace-nowrap ${getStatusColor(repo.status)}`}>
                  {repo.status}
                </span>
              </div>
              <p className="text-sm text-[var(--text-secondary)] truncate" title={repo.gitUrl}>
                {repo.gitUrl}
              </p>
              
              <div className="mt-auto pt-4 flex gap-3 border-t border-[var(--border-color)]">
                <button 
                  onClick={() => navigate(`/chat/${repo.id}`)}
                  disabled={repo.status !== 'COMPLETED'}
                  className="flex-1 flex items-center justify-center gap-2 py-2 bg-[var(--bg-hover)] hover:bg-[var(--bg-active)] disabled:opacity-50 disabled:cursor-not-allowed rounded-md transition-colors text-sm font-medium"
                >
                  <MessageSquare size={16} />
                  Chat
                </button>
                <button 
                  onClick={() => removeRepo(repo.id)}
                  className="px-3 py-2 bg-red-500/10 hover:bg-red-500/20 text-red-500 rounded-md transition-colors"
                  aria-label="Delete repository"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
          {repositories.length === 0 && !isLoading && (
            <div className="col-span-full flex flex-col items-center justify-center py-16 text-[var(--text-secondary)] border border-dashed border-[var(--border-color)] rounded-xl">
              <RefreshCw size={32} className="mb-4 opacity-20" />
              <p className="text-lg">No repositories found.</p>
              <p className="text-sm opacity-60">Import a public repository above to get started.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
