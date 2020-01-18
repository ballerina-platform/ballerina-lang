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

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Lists;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for creating the executable jar file.
 */
public class CreateExecutableTask implements Task {
    
    private static HashSet<String> excludeExtensions =  new HashSet<>(Lists.of("DSA", "SF"));
    private static Field namesField;

    static {
        try {
            // We need to access the already inserted names set to override the default behavior of throwing exception.
            namesField = ZipOutputStream.class.getDeclaredField("names");
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                namesField.setAccessible(true);
                return null;
            });
        } catch (NoSuchFieldException e) {
            throw createLauncherException("unable to retrive the entry names field :" + e.getMessage());
        }
    }

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
                    /*
                    TODO: We earlier used ZipFileSystem but we cannot explicitly set the compression method in
                     java8 with ZipFileSystem. Once we moved to java11 we can revert back to ZipFileSystem then we can
                      get rid of with accessing names field as well.
                      */
                    try (ZipOutputStream outStream =
                                 new ZipOutputStream(new FileOutputStream(String.valueOf(executablePath)))) {
                        assembleExecutable(jarFromCachePath,
                                           buildContext.moduleDependencyPathMap.get(module.packageID).platformLibs,
                                           outStream);
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

    private void assembleExecutable(Path jarFromCachePath, HashSet<Path> dependencySet, ZipOutputStream outStream) {
        try {
            HashSet<String> entries = (HashSet<String>) namesField.get(outStream);
            HashMap<String, StringBuilder> services = new HashMap<>();
            copyJarToJar(outStream, jarFromCachePath.toString(), entries, services);
            for (Path path : dependencySet) {
                copyJarToJar(outStream, path.toString(), entries, services);
            }
            // Copy merged spi services
            for (Map.Entry<String, StringBuilder> entry : services.entrySet()) {
                String s = entry.getKey();
                StringBuilder service = entry.getValue();
                ZipEntry e = new ZipEntry(s);
                outStream.putNextEntry(e);
                outStream.write(service.toString().getBytes(StandardCharsets.UTF_8));
                outStream.closeEntry();
            }
            // Copy dependency jar
            // Copy dependency libraries
            // Executable is created at give location.
            // If no entry point is found we do nothing.
        } catch (IOException | NullPointerException | IllegalAccessException e) {
            throw createLauncherException("unable to create the executable: " + e.getMessage());
        }
    }

    /**
     * Copy jar file to executable fat jar.
     *
     * @param outStream     Executable jar out stream
     * @param sourceJarFile Source file
     * @param entries       Entries set wiil be used ignore duplicate files
     * @param services      Services will be used to temporary hold merged spi files.
     * @throws IOException If file copy failed ioexception will be thrown
     */
    private void copyJarToJar(ZipOutputStream outStream, String sourceJarFile, HashSet<String> entries,
                              HashMap<String, StringBuilder> services) throws IOException {

        byte[] buffer = new byte[1024];
        int len;
        try (ZipInputStream inStream = new ZipInputStream(new FileInputStream(sourceJarFile))) {
            for (ZipEntry e; (e = inStream.getNextEntry()) != null; ) {
                String entryName = e.getName();
                // Skip already copied files or excluded extensions.
                if (e.isDirectory() || entries.contains(entryName) ||
                        excludeExtensions.contains(entryName.substring(entryName.lastIndexOf(".") + 1))) {
                    continue;
                }
                // SPIs will be merged first and then put into jar separately.
                if (entryName.startsWith("META-INF/services")) {
                    StringBuilder s = services.get(entryName);
                    if (s == null) {
                        s = new StringBuilder();
                        services.put(entryName, s);
                    }
                    char c = '\n';
                    while ((len = inStream.read()) != -1) {
                        c = (char) len;
                        s.append(c);
                    }
                    if (c != '\n') {
                        s.append('\n');
                    }
                    continue;
                }
                outStream.putNextEntry(e);
                while ((len = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, len);
                }
                outStream.closeEntry();
            }
        }
    }
}
