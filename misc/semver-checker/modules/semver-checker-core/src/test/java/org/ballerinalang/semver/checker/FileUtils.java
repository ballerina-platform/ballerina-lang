package org.ballerinalang.semver.checker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileUtils {
    public static void writeToFile(File file, String content) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file, Charset.defaultCharset())) {
            fileWriter.write(content);
        } catch (Exception e) {
            throw e;
        }
    }

}
