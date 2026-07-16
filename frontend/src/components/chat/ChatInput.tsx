import React, { useState } from 'react';
import TextareaAutosize from 'react-textarea-autosize';
import { Send } from 'lucide-react';
import { useChatStore } from '../../store/useChatStore';

interface Props {
  onSendMessage: (query: string) => void;
}

export const ChatInput = ({ onSendMessage }: Props) => {
  const [query, setQuery] = useState('');
  const isStreaming = useChatStore(state => state.isStreaming);

  const handleSubmit = (e?: React.FormEvent) => {
    e?.preventDefault();
    if (query.trim() && !isStreaming) {
      onSendMessage(query.trim());
      setQuery('');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <form onSubmit={handleSubmit} className="w-full relative bg-[var(--bg-card)] border border-[var(--border-color)] rounded-xl flex items-end p-2 focus-within:border-[var(--accent-cyan)]/50 transition-colors">
      <TextareaAutosize
        minRows={1}
        maxRows={6}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Ask a question about this repository..."
        className="w-full bg-transparent border-none focus:ring-0 resize-none py-2 px-3 text-[var(--text-primary)] placeholder-[var(--text-secondary)] outline-none"
        disabled={isStreaming}
      />
      <button
        type="submit"
        disabled={!query.trim() || isStreaming}
        className="flex-shrink-0 mb-1 ml-2 p-2 rounded-lg bg-[var(--accent-cyan)]/10 text-[var(--accent-cyan)] hover:bg-[var(--accent-cyan)]/20 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        <Send size={18} />
      </button>
    </form>
  );
};
