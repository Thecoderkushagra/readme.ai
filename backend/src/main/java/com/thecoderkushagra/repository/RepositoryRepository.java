package com.thecoderkushagra.repository;

import com.thecoderkushagra.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<Repository, UUID> {
    List<Repository> findByUsersId(UUID userId);
    Optional<Repository> findByIdAndUsersId(UUID id, UUID userId);
    Optional<Repository> findByGitUrl(String gitUrl);
}
