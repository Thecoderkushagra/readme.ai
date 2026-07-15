-- ============================================================
-- V2: Refactor Repository Relations for Global Cache Deduplication
-- Converts User <-> Repository from Many-to-One to Many-to-Many
-- ============================================================

-- Drop the existing foreign key index on user_id
DROP INDEX IF EXISTS idx_repositories_user_id;

-- Drop the user_id column (removes the FK constraint implicitly)
ALTER TABLE repositories DROP COLUMN user_id;

-- Add UNIQUE constraint on git_url to enforce global deduplication
ALTER TABLE repositories ADD CONSTRAINT uk_repositories_git_url UNIQUE (git_url);

-- Create the Many-to-Many join table
CREATE TABLE user_repositories (
    user_id       UUID NOT NULL,
    repository_id UUID NOT NULL,
    PRIMARY KEY (user_id, repository_id),
    CONSTRAINT fk_user_repositories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_repositories_repository FOREIGN KEY (repository_id) REFERENCES repositories(id) ON DELETE CASCADE
);
