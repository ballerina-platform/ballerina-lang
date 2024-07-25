/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.bytecodeoptimizer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.ballerina.projects.util.CodegenOptimizationUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.CodegenOptimizationUtils.getWhiteListedClasses;

/**
 * Optimizes a given JAR on class file level.
 *
 * @since 2201.10.0
 */
public final class NativeDependencyOptimizer {

    /**
     * key = used interface, value = used implementation.
     */
    private static final Map<String, Set<String>> interfaceWiseAllServiceProviders = new HashMap<>();
    private static final Collection<String> usedSpInterfaces = new LinkedHashSet<>();
    private static final Gson gson = new Gson();
    private final Set<String> startPointClasses;
    private final Stack<String> usedClassesStack;
    // TODO use externalClasses field when modularizing the NativeDependencyOptimizer
    private final Set<String> externalClasses = new LinkedHashSet<>();
    private final Set<String> visitedClasses = new LinkedHashSet<>();
    private final ZipFile originalJarFile;
    private final ZipArchiveOutputStream optimizedJarStream;

    public NativeDependencyOptimizer(Set<String> startPointClasses, ZipFile originalJarFile,
                                     ZipArchiveOutputStream optimizedJarStream) {
        this.startPointClasses = startPointClasses;
        this.originalJarFile = originalJarFile;
        this.optimizedJarStream = optimizedJarStream;
        this.usedClassesStack = new Stack<>();
    }

    private static Iterable<String> getServiceProviderImplementations(ZipFile originalJarFile,
                                                                      ZipArchiveEntry entry) throws IOException {
        String allImplString = IOUtils.toString(originalJarFile.getInputStream(entry), StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile("\n");
        return pattern.splitAsStream(allImplString)
                // Skipping the licensing comments
                .filter(serviceClass -> !serviceClass.startsWith("#") && !serviceClass.isBlank())
                .map(NativeDependencyOptimizer::getServiceProviderClassName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String getServiceProviderClassName(String providerFileName) {
        int i = providerFileName.lastIndexOf('/');
        return providerFileName.substring(i + 1).replace(".", "/");
    }

    public void analyzeUsedClasses() throws IOException {
        if (!jarContainsStartPoints()) {
            return;
        }
        usedClassesStack.addAll(startPointClasses);

        while (!usedClassesStack.empty()) {
            String usedClassName = usedClassesStack.pop();
            if (visitedClasses.contains(usedClassName + CodegenOptimizationUtils.CLASS)) {
                continue;
            }

            visitedClasses.add(usedClassName + CodegenOptimizationUtils.CLASS);
            ZipArchiveEntry usedJarEntry = originalJarFile.getEntry(usedClassName + CodegenOptimizationUtils.CLASS);

            if (usedJarEntry == null) {
                externalClasses.add(usedClassName);
                continue;
            }

            if (interfaceWiseAllServiceProviders.containsKey(usedClassName)) {
                usedSpInterfaces.add(usedClassName);
                usedClassesStack.addAll(interfaceWiseAllServiceProviders.get(usedClassName));
            }

            InputStream usedClassStream = originalJarFile.getInputStream(usedJarEntry);
            byte[] usedClassByteArr = IOUtils.toByteArray(usedClassStream);

            ClassNodeVisitor classNodeVisitor = new ClassNodeVisitor(usedClassByteArr);
            classNodeVisitor.analyzeClass();
            usedClassesStack.addAll(classNodeVisitor.getDependentClasses());
        }
    }

    public void analyzeWhiteListedClasses() throws IOException {
        Enumeration<ZipArchiveEntry> jarEntries = originalJarFile.getEntries();
        while (jarEntries.hasMoreElements()) {
            ZipArchiveEntry currentEntry = jarEntries.nextElement();

            if (CodegenOptimizationUtils.isServiceProvider(currentEntry.getName())) {
                analyzeServiceProviders(currentEntry);
            }

            if (CodegenOptimizationUtils.isReflectionConfig(currentEntry.getName())) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(originalJarFile.getInputStream(currentEntry), StandardCharsets.UTF_8))) {
                    whitelistReflectionClasses(reader);
                }
            }
        }
        startPointClasses.addAll(getWhiteListedClasses());
    }

    private void analyzeServiceProviders(ZipArchiveEntry currentEntry) throws IOException {
        String spInterfaceClassName = getServiceProviderClassName(currentEntry.getName());
        for (String spImplementationClassName : getServiceProviderImplementations(originalJarFile, currentEntry)) {
            interfaceWiseAllServiceProviders.putIfAbsent(spInterfaceClassName, new LinkedHashSet<>());
            interfaceWiseAllServiceProviders.get(spInterfaceClassName).add(spImplementationClassName);
        }
    }

    private void whitelistReflectionClasses(Reader reader) {
        JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);

        jsonElement.getAsJsonArray().forEach(entry -> {
            String className = entry.getAsJsonObject().get("name").getAsString();
            if (className.contains("$")) {
                startPointClasses.add(className.replace(".", "/").split("\\$")[0]);
            }
            startPointClasses.add(className.replace(".", "/"));
        });
    }

    public void copyUsedEntries() throws IOException {
        ZipArchiveEntryPredicate usedClassPredicate = entry -> {
            String entryName = entry.getName();

            if (entry.isDirectory()) {
                return true;
            }
            if (!entryName.endsWith(CodegenOptimizationUtils.CLASS)) {
                if (CodegenOptimizationUtils.isServiceProvider(entryName)) {
                    return usedSpInterfaces.contains(getServiceProviderClassName(entryName));
                }
                return true;
            }
            return visitedClasses.contains(entryName) || CodegenOptimizationUtils.isWhiteListedEntryName(entryName);
        };

        originalJarFile.copyRawEntries(optimizedJarStream, usedClassPredicate);
    }

    public NativeDependencyOptimizationReport getNativeDependencyOptimizationReport() {
        return new NativeDependencyOptimizationReport(this.startPointClasses, this.externalClasses, this.visitedClasses,
                getUnusedClasses());
    }

    private LinkedHashSet<String> getUnusedClasses() {
        LinkedHashSet<String> unusedClasses = new LinkedHashSet<>();
        Enumeration<ZipArchiveEntry> entries = this.originalJarFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();
            String className = entry.getName();
            if (!entry.isDirectory() && className.endsWith(CodegenOptimizationUtils.CLASS) &&
                    !visitedClasses.contains(className) &&
                    !className.equals(CodegenOptimizationUtils.MODULE_INFO + CodegenOptimizationUtils.CLASS)) {
                unusedClasses.add(className);
            }
        }
        return unusedClasses;
    }

    /*
    This function can be used when modularizing the native dependency optimization.
     */
    private boolean jarContainsStartPoints() {
        for (String startPoint : startPointClasses) {
            ZipArchiveEntry jarEntry = originalJarFile.getEntry(startPoint + CodegenOptimizationUtils.CLASS);
            if (jarEntry != null) {
                return true;
            }
        }
        return false;
    }
}
