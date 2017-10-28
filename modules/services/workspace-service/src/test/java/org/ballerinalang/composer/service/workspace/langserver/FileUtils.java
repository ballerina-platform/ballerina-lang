package org.ballerinalang.composer.service.workspace.langserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File utils for reading the file content
 */
public class FileUtils {
    private static final ClassLoader CLASS_LOADER = FileUtils.class.getClassLoader();
    private static final String SAMPLES_DIR = "samples/langserver/";

    /**
     * Get the file content
     * @param filePath path to the file
     * @return file content as a string
     * @throws IOException IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static String fileContent(String filePath) throws IOException, URISyntaxException {
        String location = SAMPLES_DIR + filePath;
        URI fileLocation = CLASS_LOADER.getResource(location).toURI();
        return new String(Files.readAllBytes(Paths.get(fileLocation)));
    }
}
