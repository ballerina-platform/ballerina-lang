/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Lists;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for creating the executable jar file.
 */
public class CreateExecutableTask implements Task {

    private static HashSet<String> excludeExtensions = new HashSet<>(Lists.of("DSA", "SF"));

    @Override
    public void execute(BuildContext buildContext) {
        Optional<BLangPackage> modulesWithEntryPoints = buildContext.getModules().stream()
                .filter(m -> m.symbol.entryPointExists)
                .findAny();

        if (modulesWithEntryPoints.isPresent()) {
            buildContext.out().println();
            buildContext.out().println("Generating executables");
            for (BLangPackage module : buildContext.getModules()) {
                if (module.symbol.entryPointExists) {
                    Path executablePath = buildContext.getExecutablePathFromTarget(module.packageID);
                    Path jarFromCachePath = buildContext.getJarPathFromTargetCache(module.packageID);
                    try (ZipArchiveOutputStream outStream = new ZipArchiveOutputStream(new BufferedOutputStream(
                            new FileOutputStream(String.valueOf(executablePath))))) {
                        assembleExecutable(jarFromCachePath,
                                buildContext.moduleDependencyPathMap.get(module.packageID).platformLibs, outStream);
                    } catch (IOException e) {
                        throw createLauncherException("unable to extract the uber jar :" + e.getMessage());
                    }
                }
            }
        } else {
            switch (buildContext.getSourceType()) {
                case SINGLE_BAL_FILE:
                    SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    throw createLauncherException("no entry points found in '" + singleFileContext.getBalFile() + "'.");
                case SINGLE_MODULE:
                    SingleModuleContext singleModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                    throw createLauncherException("no entry points found in '" + singleModuleContext.getModuleName() +
                            "'.\n" +
                            "Use `ballerina build -c` to compile the module without building executables.");
                case ALL_MODULES:
                    throw createLauncherException("no entry points found in any of the modules.\n" +
                            "Use `ballerina build -c` to compile the modules without building executables.");
                default:
                    throw createLauncherException("unknown source type found when creating executable.");
            }
        }
    }

    private void assembleExecutable(Path jarFromCachePath, HashSet<Path> dependencySet,
                                    ZipArchiveOutputStream outStream) {
        try {
            // Used to prevent adding duplicated entries during the final jar creation.
            HashSet<String> entries = new HashSet<>();
            // Used to process SPI related metadata entries separately. The reason is unlike the other entry types,
            // service loader related information should be merged together in the final executable jar creation.
            HashMap<String, StringBuilder> serviceEntries = new HashMap<>();
            // Copy executable thin jar and the dependency jars.
            // Executable is created at given location.
            // If no entry point is found, we do nothing.
            copyJarToJar(outStream, jarFromCachePath.toString(), entries, serviceEntries);
            for (Path path : dependencySet) {
                copyJarToJar(outStream, path.toString(), entries, serviceEntries);
            }
            // Copy merged spi services.
            for (Map.Entry<String, StringBuilder> entry : serviceEntries.entrySet()) {
                String s = entry.getKey();
                StringBuilder service = entry.getValue();
                JarArchiveEntry e = new JarArchiveEntry(s);
                outStream.putArchiveEntry(e);
                outStream.write(service.toString().getBytes(StandardCharsets.UTF_8));
                outStream.closeArchiveEntry();
            }
        } catch (IOException | NullPointerException e) {
            throw createLauncherException("unable to create the executable: " + e.getMessage());
        }
    }

    /**
     * Copies a given jar file into the executable fat jar.
     *
     * @param outStream     Output stream of the final uber jar.
     * @param sourceJarFile Path of the source jar file.
     * @param entries       Entries set will be used to ignore duplicate files.
     * @param services      Services will be used to temporary hold merged spi files.
     * @throws IOException If jar file copying is failed.
     */
    private void copyJarToJar(ZipArchiveOutputStream outStream, String sourceJarFile, HashSet<String> entries,
                              HashMap<String, StringBuilder> services) throws IOException {

        ZipFile zipFile = new ZipFile(sourceJarFile);
        ZipArchiveEntryPredicate predicate = entry -> {

            String entryName = entry.getName();
            if (entryName.startsWith("META-INF/services")) {
                StringBuilder s = services.get(entryName);
                if (s == null) {
                    s = new StringBuilder();
                    services.put(entryName, s);
                }
                char c = '\n';
                BufferedInputStream inStream = null;
                try {
                    int len;
                    inStream = new BufferedInputStream(zipFile.getInputStream(entry));
                    while ((len = inStream.read()) != -1) {
                        c = (char) len;
                        s.append(c);
                    }
                    if (c != '\n') {
                        s.append('\n');
                    }
                } catch (IOException e) {
                    throw createLauncherException(
                            "Error occurred while creating final executable jar due to: " + e.getMessage());
                } finally {
                    if (inStream != null) {
                        closeStream(inStream);
                    }
                }
                // Its not required to copy SPI entries in here as we'll be adding merged SPI related entries
                // separately. Therefore the predicate should be set as false.
                return false;
            }
            // Skip already copied files or excluded extensions.
            if (entries.contains(entryName) ||
                    excludeExtensions.contains(entryName.substring(entryName.lastIndexOf(".") + 1))) {
                return false;
            }
            // SPIs will be merged first and then put into jar separately.
            entries.add(entryName);
            return true;
        };

        // Transfers selected entries from this zip file to the output stream, while preserving its compression and
        // all the other original attributes.
        zipFile.copyRawEntries(outStream, predicate);
        zipFile.close();
    }

    private void closeStream(BufferedInputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            throw createLauncherException("error: Failed to close input stream while creating the final " +
                    "executable jar.\n" + e.getMessage());
        }
    }
}
