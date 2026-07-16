package com.thecoderkushagra.entity;


import org.hibernate.annotations.ColumnTransformer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ast_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AstChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "node_type", nullable = false, length = 100)
    private String nodeType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ColumnTransformer(write = "?::vector")
    @Column(columnDefinition = "vector(768)")
    private String embedding;
}
