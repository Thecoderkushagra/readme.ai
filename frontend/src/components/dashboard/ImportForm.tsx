import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Loader2 } from 'lucide-react';
import { useRepositoryStore } from '../../store/useRepositoryStore';

const importSchema = z.object({
  gitUrl: z.string().url('Must be a valid URL')
});

type ImportFormData = z.infer<typeof importSchema>;

export const ImportForm = () => {
  const importRepo = useRepositoryStore((state) => state.importRepo);
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<ImportFormData>({
    resolver: zodResolver(importSchema)
  });

  const onSubmit = async (data: ImportFormData) => {
    try {
      await importRepo(data.gitUrl);
      reset();
    } catch (error) {
      // Error handled globally via toast in store
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="w-full max-w-2xl mb-10 mt-2">
      <div className="flex flex-col sm:flex-row gap-4 w-full">
        <div className="flex-1 w-full relative">
          <input
            type="text"
            placeholder="Enter public GitHub repository URL..."
            className="input-base w-full pr-4 py-3"
            disabled={isSubmitting}
            {...register('gitUrl')}
          />
          {errors.gitUrl && (
            <p className="text-red-500 text-sm mt-1.5 ml-1 absolute">{errors.gitUrl.message}</p>
          )}
        </div>
        <button 
          type="submit" 
          disabled={isSubmitting} 
          className="btn-primary whitespace-nowrap min-w-[140px] flex items-center justify-center h-12 py-0"
        >
          {isSubmitting ? <Loader2 size={18} className="animate-spin" /> : 'Import'}
        </button>
      </div>
    </form>
  );
};
