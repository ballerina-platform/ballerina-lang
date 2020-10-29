package io.ballerina.projects.util;

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.Lists;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static io.ballerina.projects.util.FileUtils.getFileNameWithoutExtension;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Utilities related to ballerina projects.
 *
 * @since 2.0.0
 */
public class ProjectUtils {
    private static final HashSet<String> excludeExtensions = new HashSet<>(Lists.of("DSA", "SF"));
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

    public static Path getBallerinaRTJarPath() {
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "ballerina-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return getBalHomePath().resolve("bre").resolve("lib").resolve(runtimeJarName);
    }

    /**
     * Returns the testerina-runtime jar path.
     *
     * @return jar path
     */
    public static Path getTesterinaRuntimeJarPath() {
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "testerina-runtime-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return getBalHomePath().resolve("bre").resolve("lib").resolve(runtimeJarName);
    }

    public static void assembleExecutableJar(Manifest manifest,
                                             List<CompiledJarFile> compiledPackageJarList,
                                             Path targetPath) throws IOException {

        // Used to prevent adding duplicated entries during the final jar creation.
        HashSet<String> copiedEntries = new HashSet<>();

        try (ZipArchiveOutputStream outStream = new ZipArchiveOutputStream(
                new BufferedOutputStream(new FileOutputStream(targetPath.toString())))) {
            copyRuntimeJar(outStream, getBallerinaRTJarPath(), copiedEntries);

            JarArchiveEntry e = new JarArchiveEntry(JarFile.MANIFEST_NAME);
            outStream.putArchiveEntry(e);
            manifest.write(new BufferedOutputStream(outStream));
            outStream.closeArchiveEntry();

            for (CompiledJarFile compiledJarFile : compiledPackageJarList) {
                for (Map.Entry<String, byte[]> keyVal : compiledJarFile.getJarEntries().entrySet()) {
                    copyEntry(copiedEntries, outStream, keyVal);
                }
            }
        }
    }

    private static void copyEntry(HashSet<String> copiedEntries,
                                  ZipArchiveOutputStream outStream,
                                  Map.Entry<String, byte[]> keyVal) throws IOException {
        String entryName = keyVal.getKey();
        if (!isCopiedOrExcludedEntry(entryName, copiedEntries)) {
            byte[] entryContent = keyVal.getValue();
            JarArchiveEntry entry = new JarArchiveEntry(entryName);
            outStream.putArchiveEntry(entry);
            outStream.write(entryContent);
            outStream.closeArchiveEntry();
        }
    }

    /**
     * Copies a given jar file into the executable fat jar.
     *
     * @param ballerinaRTJarPath Ballerina runtime jar path.
     * @throws IOException If jar file copying is failed.
     */
    public static void copyRuntimeJar(ZipArchiveOutputStream outStream,
                                      Path ballerinaRTJarPath,
                                      HashSet<String> copiedEntries) throws IOException {
        // TODO This code is copied from the current executable jar creation logic. We may need to refactor this.
        HashMap<String, StringBuilder> services = new HashMap<>();
        ZipFile zipFile = new ZipFile(ballerinaRTJarPath.toString());
        ZipArchiveEntryPredicate predicate = entry -> {

            String entryName = entry.getName();
            if (entryName.equals("META-INF/MANIFEST.MF")) {
                return false;
            }

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

            // Skip already copied files or excluded extensions.
            if (isCopiedOrExcludedEntry(entryName, copiedEntries)) {
                return false;
            }
            // SPIs will be merged first and then put into jar separately.
            copiedEntries.add(entryName);
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

    private static boolean isCopiedOrExcludedEntry(String entryName, HashSet<String> copiedEntries) {
        return copiedEntries.contains(entryName) ||
                excludeExtensions.contains(entryName.substring(entryName.lastIndexOf(".") + 1));
    }

    /**
     * Construct and return the thin jar name of the provided module.
     *
     * @param module Module instance
     * @return the name of the thin jar
     */
    public static String getJarName(Module module) {
        String jarName;
        if (module.packageInstance().packageDescriptor().org().anonymous()) {
            DocumentId documentId = module.documentIds().iterator().next();
            String documentName = module.document(documentId).name();
            jarName = getFileNameWithoutExtension(documentName);
        } else {
            ModuleName moduleName = module.moduleName();
            if (moduleName.isDefaultModuleName()) {
                jarName = moduleName.packageName().toString();
            } else {
                jarName = moduleName.moduleNamePart();
            }
        }
        return jarName + ProjectConstants.BLANG_COMPILED_JAR_EXT;
    }

    /**
     * Return the testable thin jar name of the provided module.
     *
     * @param module Module instance
     * @return the name of the thin jar
     */
    public static String getTestableJarName(Module module) {
        String jarName;
        if (module.packageInstance().packageDescriptor().org().anonymous()) {
            DocumentId documentId = module.documentIds().iterator().next();
            String documentName = module.document(documentId).name();
            jarName = getFileNameWithoutExtension(documentName);
        } else {
            ModuleName moduleName = module.moduleName();
            if (moduleName.isDefaultModuleName()) {
                jarName = moduleName.packageName().toString();
            } else {
                jarName = moduleName.moduleNamePart();
            }
        }
        return jarName + "-testable" + ProjectConstants.BLANG_COMPILED_JAR_EXT;
    }

    /**
     * Provides Qualified Class Name.
     *
     * @param orgName     Org name
     * @param packageName Package name
     * @param version Package version
     * @param className   Class name
     * @return Qualified class name
     */
    public static String getQualifiedClassName(String orgName, String packageName, String version, String className) {

        if (!Names.DEFAULT_PACKAGE.value.equals(packageName)) {
            className = packageName.replace('.', '_') + "." + version.replace('.', '_') + "." + className;
        }
        if (!Names.ANON_ORG.value.equals(orgName)) {
            className = orgName.replace('.', '_') + "." +  className;
        }

        return className;
    }
}
