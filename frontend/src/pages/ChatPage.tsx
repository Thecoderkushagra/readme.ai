import { useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useChatStore } from '../store/useChatStore';
import { useRepositoryStore } from '../store/useRepositoryStore';
import { streamChat } from '../services/chat.service';
import { ChatMessageBubble } from '../components/chat/ChatMessageBubble';
import { ChatInput } from '../components/chat/ChatInput';
import { ChatZeroState } from '../components/chat/ChatZeroState';
import { CodeViewerPane } from '../components/chat/CodeViewerPane';
import toast from 'react-hot-toast';

export const ChatPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  
  const { repositories, fetchRepositories } = useRepositoryStore();
  const { messages, isStreaming, addMessage, appendTokenToLastMessage, setStreaming, clearChat, loadSourceFiles, activeViewerItem } = useChatStore();

  const repository = repositories.find(r => r.id === id);

  useEffect(() => {
    if (repositories.length === 0) {
      fetchRepositories();
    }
    if (id) {
      loadSourceFiles(id);
    }
    // Clear chat on unmount or when changing repos
    return () => clearChat();
  }, [id, fetchRepositories, clearChat, loadSourceFiles, repositories.length]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isStreaming]);

  const handleSendMessage = async (query: string) => {
    if (!id) return;
    
    // Capture history up to this point
    const history = [...messages];
    
    addMessage({ role: 'user', content: query });
    addMessage({ role: 'assistant', content: '' });
    setStreaming(true);

    await streamChat(
      id,
      query,
      history,
      (token) => {
        appendTokenToLastMessage(token);
      },
      () => {
        setStreaming(false);
      },
      (error) => {
        setStreaming(false);
        toast.error('Failed to generate response');
        console.error('Streaming error:', error);
      }
    );
  };

  if (!repository && repositories.length > 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-[var(--text-secondary)] h-full">
        <p>Repository not found.</p>
        <button onClick={() => navigate('/dashboard')} className="mt-4 text-[var(--accent-cyan)] hover:underline">
          Return to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="flex h-[calc(100vh-100px)] w-full overflow-hidden">
      {/* Left Pane (Chat Area) */}
      <div className={`flex flex-col h-full transition-all duration-300 ease-in-out ${activeViewerItem ? 'w-full md:w-[45%]' : 'max-w-4xl mx-auto w-full'}`}>
        {/* Header */}
        <div className="flex items-center gap-4 mb-4 border-b border-[var(--border-color)] pb-4 shrink-0 mx-2 mt-2">
          <button 
            onClick={() => navigate('/dashboard')}
            className="p-2 hover:bg-[var(--bg-hover)] rounded-md transition-colors text-[var(--text-secondary)]"
          >
            <ArrowLeft size={20} />
          </button>
          <div>
            <h2 className="text-xl font-bold">{repository?.name || 'Loading...'}</h2>
            <p className="text-sm text-[var(--text-secondary)]">Ask anything about this codebase</p>
          </div>
        </div>

        {/* Chat Area */}
        <div className="flex-1 overflow-y-auto mb-4 pr-2 custom-scrollbar">
          {messages.length === 0 ? (
            <ChatZeroState onSuggest={handleSendMessage} />
          ) : (
            <div className="flex flex-col w-full pb-4 px-2">
              {messages.map((msg, index) => (
                <ChatMessageBubble key={index} message={msg} />
              ))}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Input Area */}
        <div className="w-full shrink-0 mt-auto px-2 mb-2">
          <ChatInput onSendMessage={handleSendMessage} />
        </div>
      </div>

      {/* Right Pane (Code Viewer) */}
      {activeViewerItem && (
        <div className="hidden md:block w-[55%] border-l border-[var(--border-color)] h-full">
          <CodeViewerPane />
        </div>
      )}
    </div>
  );
};
