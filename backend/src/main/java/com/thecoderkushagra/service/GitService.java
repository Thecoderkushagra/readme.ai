package com.thecoderkushagra.service;

import com.thecoderkushagra.exception.GitOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class GitService {

    public Path cloneRepository(String gitUrl) {
        try {
            Path tempDirectory = Files.createTempDirectory("readme-ai-");
            log.info("Cloning repository {} into {}", gitUrl, tempDirectory);

            ProcessBuilder pb = new ProcessBuilder(
                    "git", "clone", "--depth", "1", gitUrl, tempDirectory.toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.error("Git clone failed with exit code {}. Output: {}", exitCode, output);
                throw new GitOperationException("Git clone failed: " + output);
            }
            
            return tempDirectory;
        } catch (GitOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during git clone", e);
            throw new GitOperationException("Error during git clone", e);
        }
    }

    public void cleanupDirectory(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try {
            log.info("Cleaning up directory {}", directory);
            FileSystemUtils.deleteRecursively(directory.toFile());
        } catch (Exception e) {
            log.warn("Failed to cleanup directory {}: {}", directory, e.getMessage());
        }
    }
}
