package com.thecoderkushagra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "source_files", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"repository_id", "file_path"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceFile {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
