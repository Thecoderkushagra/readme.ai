package com.thecoderkushagra.repository;

import com.thecoderkushagra.entity.AstChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AstChunkRepository extends JpaRepository<AstChunk, UUID> {
}
