import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import type { ChatMessage } from '../../types/chat';
import { User, Bot, FileText, Maximize2 } from 'lucide-react';
import { useChatStore } from '../../store/useChatStore';

interface Props {
  message: ChatMessage;
}

export const ChatMessageBubble = ({ message }: Props) => {
  const isUser = message.role === 'user';
  const { sourceFiles, openViewer } = useChatStore();

  const determineLanguage = (filePath: string) => {
    const ext = filePath.split('.').pop()?.toLowerCase();
    switch (ext) {
      case 'ts': case 'tsx': return 'typescript';
      case 'js': case 'jsx': return 'javascript';
      case 'py': return 'python';
      case 'java': return 'java';
      case 'cpp': case 'cc': case 'h': return 'cpp';
      case 'go': return 'go';
      case 'rs': return 'rust';
      default: return 'text';
    }
  };

  return (
    <div className={`flex w-full mb-6 ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div className={`flex max-w-[85%] sm:max-w-[75%] ${isUser ? 'flex-row-reverse' : 'flex-row'} gap-3`}>
        <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center ${isUser ? 'bg-[var(--accent-cyan)]/20 text-[var(--accent-cyan)]' : 'bg-[var(--border-color)] text-[var(--text-secondary)]'}`}>
          {isUser ? <User size={16} /> : <Bot size={16} />}
        </div>
        
        <div className={`flex flex-col ${isUser ? 'items-end' : 'items-start'} max-w-full overflow-hidden`}>
          <div className={`px-4 py-3 rounded-2xl w-full max-w-full overflow-x-auto ${isUser ? 'bg-[var(--accent-cyan)]/10 border border-[var(--accent-cyan)]/20 text-[var(--text-primary)] rounded-tr-sm' : 'surface-card rounded-tl-sm'}`}>
            <div className={`prose prose-invert max-w-none prose-p:leading-relaxed ${isUser ? 'prose-p:text-[var(--text-primary)]' : 'prose-p:text-[var(--text-secondary)]'}`}>
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                components={{
                  code({ node, inline, className, children, ...props }: any) {
                    const match = /language-(\w+)/.exec(className || '');
                    
                    if (!inline && match) {
                      const codeString = String(children).replace(/\n$/, '');
                      return (
                        <div className="relative group my-4">
                          <div className="absolute top-0 right-0 z-10 flex items-center bg-[var(--bg-card)]/80 rounded-bl-md rounded-tr-md border-b border-l border-[var(--border-color)] opacity-0 group-hover:opacity-100 transition-opacity">
                            <button
                              onClick={() => openViewer("Snippet", codeString, match[1])}
                              className="p-1.5 text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
                              title="Expand in Viewer"
                            >
                              <Maximize2 size={14} />
                            </button>
                          </div>
                          <SyntaxHighlighter
                            style={vscDarkPlus as any}
                            language={match[1]}
                            PreTag="div"
                            className="rounded-md text-sm m-0"
                            {...props}
                          >
                            {codeString}
                          </SyntaxHighlighter>
                        </div>
                      );
                    }
                    
                    if (inline) {
                      const codeText = String(children);
                      // Check for precise match or trailing match (e.g. `App.java` matches `src/main/.../App.java`)
                      const matchedFile = sourceFiles?.find(sf => sf.filePath === codeText || sf.filePath.endsWith('/' + codeText));
                      
                      if (matchedFile) {
                        return (
                          <button
                            onClick={() => openViewer(matchedFile.filePath, matchedFile.content, determineLanguage(matchedFile.filePath))}
                            className="inline-flex items-center gap-1 bg-[var(--bg-hover)] hover:bg-[var(--accent-cyan)]/20 text-[var(--accent-cyan)] border border-[var(--accent-cyan)]/30 px-1.5 py-0.5 rounded-md font-mono text-sm transition-colors align-baseline mx-1"
                          >
                            <FileText size={12} />
                            {codeText}
                          </button>
                        );
                      }
                      
                      return (
                        <code className="bg-[var(--bg-hover)] text-[var(--text-primary)] px-1.5 py-0.5 rounded-md font-mono text-sm" {...props}>
                          {children}
                        </code>
                      );
                    }

                    return <code {...props}>{children}</code>;
                  }
                }}
              >
                {message.content}
              </ReactMarkdown>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
