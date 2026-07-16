import { apiClient } from './apiClient';
import type { Repository, SourceFile } from '../types/repository';
import type { ApiResponse } from '../types/auth';

export const getRepositories = async (): Promise<ApiResponse<Repository[]>> => {
    const response = await apiClient.get('/api/repositories');
    return response.data as ApiResponse<Repository[]>;
};

export const importRepository = async (gitUrl: string): Promise<ApiResponse<Repository>> => {
    const response = await apiClient.post('/api/repositories/import', { gitUrl });
    return response.data as ApiResponse<Repository>;
};

export const deleteRepository = async (id: string): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete(`/api/repositories/${id}`);
    return response.data as ApiResponse<void>;
};

export const getRepositoryFiles = async (repositoryId: string): Promise<ApiResponse<SourceFile[]>> => {
    const response = await apiClient.get(`/api/repositories/${repositoryId}/files`);
    return response.data as ApiResponse<SourceFile[]>;
};
