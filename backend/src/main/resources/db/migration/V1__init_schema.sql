CREATE EXTENSION IF NOT EXISTS vector;

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL
);

-- Create index for users email
CREATE INDEX idx_users_email ON users(email);

-- Create repositories table
CREATE TABLE repositories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    git_url VARCHAR(1024) NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_repositories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for repositories user_id
CREATE INDEX idx_repositories_user_id ON repositories(user_id);

-- Create ast_chunks table
CREATE TABLE ast_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repository_id UUID NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    node_type VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    embedding VECTOR(768),
    CONSTRAINT fk_ast_chunks_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE
);

-- Create index for ast_chunks repository_id
CREATE INDEX idx_ast_chunks_repository_id ON ast_chunks(repository_id);

-- Create HNSW spatial index on ast_chunks embedding
CREATE INDEX idx_ast_chunks_embedding ON ast_chunks USING hnsw (embedding vector_cosine_ops);

-- Create semantic_cache table
CREATE TABLE semantic_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repository_id UUID NOT NULL,
    query_text TEXT NOT NULL,
    query_embedding VECTOR(768) NOT NULL,
    response_text TEXT NOT NULL,
    CONSTRAINT fk_semantic_cache_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE
);

-- Create index for semantic_cache repository_id
CREATE INDEX idx_semantic_cache_repository_id ON semantic_cache(repository_id);

-- Create HNSW spatial index on semantic_cache query_embedding
CREATE INDEX idx_semantic_cache_query_embedding ON semantic_cache USING hnsw (query_embedding vector_cosine_ops);
