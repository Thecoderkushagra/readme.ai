package com.thecoderkushagra.service;

import com.thecoderkushagra.dto.ParsedChunkDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.treesitter.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
public class AstParsingService {

    public List<ParsedChunkDto> parseRepository(Path repositoryPath) {
        List<ParsedChunkDto> allChunks = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(repositoryPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isValidFile)
                 .forEach(path -> {
                     String relativePath = repositoryPath.relativize(path).toString();
                     allChunks.addAll(processFile(path, relativePath));
                 });
        } catch (IOException e) {
            log.error("Failed to walk repository path: {}", repositoryPath, e);
        }

        return allChunks;
    }

    private boolean isValidFile(Path path) {
        String pathStr = path.toString().replace('\\', '/').toLowerCase();

        // Skip ignored directories
        if (pathStr.contains("/.git/") || pathStr.contains(".git/") ||
            pathStr.contains("/node_modules/") || pathStr.contains("node_modules/") ||
            pathStr.contains("/target/") || pathStr.contains("target/") ||
            pathStr.contains("/build/") || pathStr.contains("build/") ||
            pathStr.contains("/dist/") || pathStr.contains("dist/") ||
            pathStr.contains("/.idea/") || pathStr.contains(".idea/") ||
            pathStr.contains("/.vscode/") || pathStr.contains(".vscode/") ||
            pathStr.contains("/venv/") || pathStr.contains("venv/")) {
            return false;
        }

        // Skip binary extensions
        if (pathStr.endsWith(".png") || pathStr.endsWith(".jar") ||
            pathStr.endsWith(".exe") || pathStr.endsWith(".pdf")) {
            return false;
        }
        return true;
    }

    private List<ParsedChunkDto> processFile(Path filePath, String relativePath) {
        List<ParsedChunkDto> chunks = new ArrayList<>();
        try {
            String fileName = filePath.getFileName().toString().toLowerCase();
            String extension = getExtension(fileName);

            if (isTier1Language(extension)) {
                try {
                    List<ParsedChunkDto> astChunks = createAstChunks(filePath, relativePath, extension);
                    if (astChunks.isEmpty()) {
                        throw new RuntimeException("AST parsing returned 0 chunks");
                    }
                    chunks.addAll(astChunks);
                } catch (Exception e) {
                    log.warn("Tree-sitter parsing failed for {}, falling back to Universal Chunks. Error: {}", relativePath, e.getMessage());
                    chunks.addAll(createUniversalChunks(filePath, relativePath));
                }
            } else {
                chunks.addAll(createUniversalChunks(filePath, relativePath));
            }
        } catch (Exception e) {
            log.error("Error processing file {}", relativePath, e);
        }
        return chunks;
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex > 0 && lastIndex < fileName.length() - 1) {
            return fileName.substring(lastIndex + 1);
        }
        return "";
    }

    private boolean isTier1Language(String extension) {
        return switch (extension) {
            case "java", "py", "js", "ts", "tsx", "jsx", "cpp", "cc", "h", "go", "rs" -> true;
            default -> false;
        };
    }

    private List<ParsedChunkDto> createUniversalChunks(Path filePath, String relativePath) throws IOException {
        List<ParsedChunkDto> chunks = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath);

        int chunkSize = 100;
        int overlap = 20;
        int step = chunkSize - overlap;

        for (int i = 0; i < lines.size(); i += step) {
            int end = Math.min(i + chunkSize, lines.size());
            List<String> chunkLines = lines.subList(i, end);
            String content = String.join("\n", chunkLines);
            chunks.add(new ParsedChunkDto(relativePath, "TextChunk", content));
            if (end == lines.size()) {
                break;
            }
        }
        return chunks;
    }

    private List<ParsedChunkDto> createAstChunks(Path filePath, String relativePath, String extension) throws IOException {
        String content = Files.readString(filePath);
        TSLanguage language = getLanguageForExtension(extension);
        Set<String> targetNodes = getTargetNodesForExtension(extension);

        TSParser parser = new TSParser();
        parser.setLanguage(language);

        TSTree tree = parser.parseString(null, content);
        TSNode rootNode = tree.getRootNode();

        List<ParsedChunkDto> chunks = new ArrayList<>();
        byte[] utf8Bytes = content.getBytes(StandardCharsets.UTF_8);

        traverseAndExtract(rootNode, utf8Bytes, targetNodes, chunks, relativePath);

        return chunks;
    }

    private TSLanguage getLanguageForExtension(String extension) {
        return switch (extension) {
            case "java" -> new TreeSitterJava();
            case "py" -> new TreeSitterPython();
            case "js", "jsx" -> new TreeSitterJavascript();
            case "ts", "tsx" -> new TreeSitterTypescript();
            case "cpp", "cc", "h" -> new TreeSitterCpp();
            case "go" -> new TreeSitterGo();
            case "rs" -> new TreeSitterRust();
            default -> throw new IllegalArgumentException("Unsupported language extension: " + extension);
        };
    }

    private Set<String> getTargetNodesForExtension(String extension) {
        return switch (extension) {
            case "java" -> Set.of("class_declaration", "method_declaration");
            case "py" -> Set.of("class_definition", "function_definition");
            case "js", "ts", "tsx", "jsx" -> Set.of("class_declaration", "function_declaration", "method_definition");
            case "cpp", "cc", "h" -> Set.of("class_specifier", "function_definition");
            case "go" -> Set.of("type_declaration", "function_declaration", "method_declaration");
            case "rs" -> Set.of("struct_item", "function_item", "impl_item");
            default -> Set.of();
        };
    }

    private void traverseAndExtract(TSNode node, byte[] contentBytes, Set<String> targetNodes, List<ParsedChunkDto> chunks, String relativePath) {
        if (targetNodes.contains(node.getType())) {
            int start = node.getStartByte();
            int end = node.getEndByte();

            // Extract using UTF-8 byte bounds
            if (start >= 0 && end <= contentBytes.length && start <= end) {
                String chunkContent = new String(contentBytes, start, end - start, StandardCharsets.UTF_8);
                chunks.add(new ParsedChunkDto(relativePath, node.getType(), chunkContent));
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverseAndExtract(node.getChild(i), contentBytes, targetNodes, chunks, relativePath);
        }
    }
}
