import { create } from 'zustand';
import type { ChatMessage } from '../types/chat';
import type { SourceFile } from '../types/repository';
import { getRepositoryFiles } from '../services/repositoryService';

interface ChatState {
    messages: ChatMessage[];
    isStreaming: boolean;
    sourceFiles: SourceFile[];
    activeViewerItem: { title: string; content: string; language: string } | null;
    
    addMessage: (msg: ChatMessage) => void;
    appendTokenToLastMessage: (token: string) => void;
    setStreaming: (isStreaming: boolean) => void;
    clearChat: () => void;
    
    loadSourceFiles: (repositoryId: string) => Promise<void>;
    openViewer: (title: string, content: string, language: string) => void;
    closeViewer: () => void;
}

export const useChatStore = create<ChatState>((set) => ({
    messages: [],
    sourceFiles: [],
    activeViewerItem: null,
    isStreaming: false,
    addMessage: (msg) => set((state) => ({ messages: [...state.messages, msg] })),
    appendTokenToLastMessage: (token) => set((state) => {
        const newMessages = [...state.messages];
        if (newMessages.length > 0) {
            const lastIndex = newMessages.length - 1;
            newMessages[lastIndex] = {
                ...newMessages[lastIndex],
                content: newMessages[lastIndex].content + token
            };
        }
        return { messages: newMessages };
    }),
    setStreaming: (isStreaming) => set({ isStreaming }),
    clearChat: () => set({ messages: [], activeViewerItem: null }),
    
    loadSourceFiles: async (repositoryId) => {
        try {
            const res = await getRepositoryFiles(repositoryId);
            if (res.success) {
                set({ sourceFiles: res.data });
            }
        } catch (error) {
            console.error('Failed to load source files:', error);
        }
    },
    
    openViewer: (title, content, language) => set({ activeViewerItem: { title, content, language } }),
    closeViewer: () => set({ activeViewerItem: null })
}));
