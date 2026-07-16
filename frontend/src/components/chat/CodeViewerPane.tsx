import { X } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { useChatStore } from '../../store/useChatStore';

export const CodeViewerPane = () => {
  const { activeViewerItem, closeViewer } = useChatStore();

  if (!activeViewerItem) return null;

  return (
    <div className="flex flex-col h-full w-full bg-[var(--bg-base)] animate-fade-in">
      <div className="flex items-center justify-between px-4 py-3 border-b border-[var(--border-color)] bg-[var(--bg-card)] shrink-0">
        <h3 className="font-semibold text-sm text-[var(--text-primary)] truncate max-w-[80%]" title={activeViewerItem.title}>
          {activeViewerItem.title}
        </h3>
        <button
          onClick={closeViewer}
          className="p-1 hover:bg-[var(--bg-hover)] rounded-md transition-colors text-[var(--text-secondary)]"
        >
          <X size={18} />
        </button>
      </div>
      <div className="flex-1 overflow-y-auto custom-scrollbar">
        <SyntaxHighlighter
          language={activeViewerItem.language}
          style={vscDarkPlus as any}
          showLineNumbers={true}
          customStyle={{
            margin: 0,
            padding: '1rem',
            background: 'transparent',
            fontSize: '13px',
            lineHeight: '1.5'
          }}
          lineNumberStyle={{
            minWidth: '2.5em',
            paddingRight: '1em',
            color: '#858585',
            textAlign: 'right'
          }}
        >
          {activeViewerItem.content}
        </SyntaxHighlighter>
      </div>
    </div>
  );
};
