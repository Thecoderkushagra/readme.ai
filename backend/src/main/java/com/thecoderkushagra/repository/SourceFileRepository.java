package com.thecoderkushagra.repository;

import com.thecoderkushagra.entity.SourceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SourceFileRepository extends JpaRepository<SourceFile, UUID> {
    List<SourceFile> findByRepositoryId(UUID repositoryId);
}
