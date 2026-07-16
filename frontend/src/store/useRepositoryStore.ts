import { create } from 'zustand';
import type { Repository } from '../types/repository';
import { getRepositories, importRepository, deleteRepository } from '../services/repositoryService';
import toast from 'react-hot-toast';

const TERMINAL_STATUSES = ['COMPLETED', 'FAILED'];
const POLL_INTERVAL_MS = 5000;

interface RepositoryState {
    repositories: Repository[];
    isLoading: boolean;
    _pollingIntervalId: ReturnType<typeof setInterval> | null;
    fetchRepositories: () => Promise<void>;
    importRepo: (gitUrl: string) => Promise<void>;
    removeRepo: (id: string) => Promise<void>;
    startPolling: () => void;
    stopPolling: () => void;
}

export const useRepositoryStore = create<RepositoryState>((set, get) => ({
    repositories: [],
    isLoading: false,
    _pollingIntervalId: null,
    fetchRepositories: async () => {
        set({ isLoading: true });
        try {
            const res = await getRepositories();
            if (res.success) {
                set({ repositories: res.data });
                // Auto-manage polling based on current statuses
                const hasInProgress = res.data.some(
                    (r: Repository) => !TERMINAL_STATUSES.includes(r.status)
                );
                if (hasInProgress) {
                    get().startPolling();
                } else {
                    get().stopPolling();
                }
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || 'Failed to fetch repositories');
        } finally {
            set({ isLoading: false });
        }
    },
    importRepo: async (gitUrl: string) => {
        try {
            const res = await importRepository(gitUrl);
            if (res.success) {
                toast.success('Repository imported successfully');
                await get().fetchRepositories();
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || 'Failed to import repository');
            throw error;
        }
    },
    removeRepo: async (id: string) => {
        try {
            const res = await deleteRepository(id);
            if (res.success) {
                toast.success('Repository deleted successfully');
                await get().fetchRepositories();
            }
        } catch (error: any) {
            toast.error(error.response?.data?.message || 'Failed to delete repository');
        }
    },
    startPolling: () => {
        // Don't start a second interval if one is already running
        if (get()._pollingIntervalId !== null) return;
        const intervalId = setInterval(() => {
            get().fetchRepositories();
        }, POLL_INTERVAL_MS);
        set({ _pollingIntervalId: intervalId });
    },
    stopPolling: () => {
        const intervalId = get()._pollingIntervalId;
        if (intervalId !== null) {
            clearInterval(intervalId);
            set({ _pollingIntervalId: null });
        }
    },
}));
