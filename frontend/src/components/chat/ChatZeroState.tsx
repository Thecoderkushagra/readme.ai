import { Bot } from 'lucide-react';

interface Props {
  onSuggest: (prompt: string) => void;
}

export const ChatZeroState = ({ onSuggest }: Props) => {
  const suggestedPrompts = [
    "Explain the authentication flow",
    "Where are the database schemas?",
    "How do I run this locally?",
    "What are the main API endpoints?"
  ];

  return (
    <div className="flex flex-col items-center justify-center h-full text-center px-4 w-full">
      <div className="w-16 h-16 bg-[var(--accent-cyan)]/10 text-[var(--accent-cyan)] rounded-2xl flex items-center justify-center mb-6">
        <Bot size={32} />
      </div>
      <h2 className="text-2xl font-bold mb-3 text-[var(--text-primary)]">Repository ingested.</h2>
      <p className="text-[var(--text-secondary)] mb-10 max-w-md">
        What would you like to know? You can ask about architecture, specific files, or general concepts.
      </p>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full max-w-2xl">
        {suggestedPrompts.map((prompt, index) => (
          <button
            key={index}
            onClick={() => onSuggest(prompt)}
            className="px-4 py-3 bg-[var(--bg-surface)] hover:bg-[var(--bg-hover)] border border-[var(--border-color)] rounded-xl text-sm font-medium text-[var(--text-primary)] text-left transition-colors flex items-center"
          >
            {prompt}
          </button>
        ))}
      </div>
    </div>
  );
};
