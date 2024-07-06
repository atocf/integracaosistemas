package br.com.atocf.pedido.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DefaultFileOperations implements FileOperations {
    @Override
    public void createDirectoryIfNotExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void writeFile(Path filePath, byte[] content) throws IOException {
        Files.write(filePath, content);
    }
}