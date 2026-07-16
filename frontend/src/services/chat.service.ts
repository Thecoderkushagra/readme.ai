import type { ChatMessage } from '../types/chat';
import { useAuthStore } from '../store/useAuthStore';

export const streamChat = async (
    repositoryId: string,
    query: string,
    history: ChatMessage[],
    onToken: (token: string) => void,
    onComplete: () => void,
    onError: (error: any) => void
) => {
    try {
        const token = useAuthStore.getState().accessToken;
        const baseUrl = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080';
        
        const response = await fetch(`${baseUrl}/api/chat/${repositoryId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ query, history })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        if (!response.body) {
            throw new Error('Response body is null');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';

        while (true) {
            const { value, done } = await reader.read();
            if (done) break;
            
            buffer += decoder.decode(value, { stream: true });
            
            // SSE chunks usually end with \n\n
            const chunks = buffer.split('\n\n');
            buffer = chunks.pop() || ''; // keep the last incomplete chunk in the buffer
            
            for (const chunk of chunks) {
                const lines = chunk.split('\n');
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        // Extract text after 'data:'. Spring might include a leading space like 'data: text'
                        let data = line.substring(5);
                        // Spring text event stream does not typically prepend space, but if it does:
                        // Actually, raw token-by-token doesn't need trim because we want spaces.
                        onToken(data);
                    }
                }
            }
        }
        
        onComplete();
    } catch (error) {
        onError(error);
    }
};
