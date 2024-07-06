package br.com.atocf.pedido.utils;

import java.io.IOException;
import java.nio.file.Path;

public interface FileOperations {
    void createDirectoryIfNotExists(String dirPath);
    void writeFile(Path filePath, byte[] content) throws IOException;
}