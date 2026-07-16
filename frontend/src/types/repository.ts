export interface Repository {
    id: string;
    name: string;
    gitUrl: string;
    status: 'CLONING' | 'PARSING' | 'VECTORIZING' | 'COMPLETED' | 'FAILED';
}

export interface SourceFile {
    id: string;
    repositoryId: string;
    filePath: string;
    content: string;
}
