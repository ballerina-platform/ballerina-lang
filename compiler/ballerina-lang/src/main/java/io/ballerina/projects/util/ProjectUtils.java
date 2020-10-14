package io.ballerina.projects.util;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Utilities related to ballerina projects.
 *
 * @since 2.0.0
 */
public class ProjectUtils {

    public static Path getBalHomePath() {
        return Paths.get(System.getProperty(BALLERINA_HOME));
    }

    public static String getBallerinaPackVersion() {
        try (InputStream inputStream = ProjectUtils.class.getResourceAsStream(ProjectDirConstants.PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(ProjectDirConstants.BALLERINA_PACK_VERSION);
        } catch (Throwable ignore) {
        }
        return "unknown";
    }

    public static Path getRuntimeJar() {
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "ballerina-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return getBalHomePath().resolve("bre").resolve("lib").resolve(runtimeJarName);
    }

    /**
     * Copies a given jar file into the executable fat jar.
     *
     * @param targetPath     Output stream of the final uber jar.
     * @throws IOException If jar file copying is failed.
     */
    public static void copyRuntimeJar(Path targetPath) throws IOException {
        try (ZipArchiveOutputStream outStream = new ZipArchiveOutputStream(new BufferedOutputStream(
                new FileOutputStream(String.valueOf(targetPath))))) {

            HashMap<String, StringBuilder> services = new HashMap<>();
            ZipFile zipFile = new ZipFile(getRuntimeJar().toString());
            ZipArchiveEntryPredicate predicate = entry -> {

                String entryName = entry.getName();
                if (entryName.startsWith("META-INF/services")) {
                    StringBuilder s = services.get(entryName);
                    if (s == null) {
                        s = new StringBuilder();
                        services.put(entryName, s);
                    }
                    char c = '\n';

                    int len;
                    try (BufferedInputStream inStream = new BufferedInputStream(zipFile.getInputStream(entry))) {
                        while ((len = inStream.read()) != -1) {
                            c = (char) len;
                            s.append(c);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (c != '\n') {
                            s.append('\n');
                        }

                    // Its not required to copy SPI entries in here as we'll be adding merged SPI related entries
                    // separately. Therefore the predicate should be set as false.
                    return false;
                }
                return true;
            };

            // Transfers selected entries from this zip file to the output stream, while preserving its compression and
            // all the other original attributes.
            zipFile.copyRawEntries(outStream, predicate);
            zipFile.close();

            for (Map.Entry<String, StringBuilder> entry : services.entrySet()) {
                String s = entry.getKey();
                StringBuilder service = entry.getValue();
                JarArchiveEntry e = new JarArchiveEntry(s);
                outStream.putArchiveEntry(e);
                outStream.write(service.toString().getBytes(StandardCharsets.UTF_8));
                outStream.closeArchiveEntry();
            }
        }
    }
}
