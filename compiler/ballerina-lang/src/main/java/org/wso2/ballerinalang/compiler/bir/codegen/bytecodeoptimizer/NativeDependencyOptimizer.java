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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Optimizes a given JAR on class file level.
 *
 * @since 2201.10.0
 */
public class NativeDependencyOptimizer {

    private static final String CLASS = ".class";
    private static final String SERVICE_PROVIDER_DIRECTORY = "META-INF/services/";
    private static final String NATIVE_IMAGE_DIRECTORY = "META-INF/native-image";
    private static final String REFLECT_CONFIG_JSON = "reflect-config.json";
    private static final String JNI_CONFIG_JSON = "jni-config.json";
    private static final String MODULE_INFO = "module-info";

    // These directories are whitelisted due to usages of "sun.misc.Unsafe" class
    // TODO Find a way to whitelist only the necessary class files
    // TODO Check whether we need "com/mysql" for sure
    private static final Set<String> WHITELISTED_DIRECTORIES = new LinkedHashSet<>(Arrays.asList("io/netty/util"));

    /**
     * These classes are used by GraalVM when building the native-image. Since they are not connected to the root class,
     * they will be removed by the NativeDependencyOptimizer if they are not whitelisted.
     */
    private static final Set<String> GRAALVM_FEATURE_CLASSES =
            new LinkedHashSet<>(Arrays.asList("io/ballerina/stdlib/crypto/svm/BouncyCastleFeature"));

    /**
     * key = implementation class name, value = interface class name.
     * Since one interface can be implemented by more than one child class, it is possible to have duplicate values.
     * <p>
     * TODO modify the service provider files and delete the lines containing the UNUSED implementations of interfaces
     */
    private static final Map<String, String> implementationWiseAllServiceProviders = new LinkedHashMap<>();

    /**
     * key = used interface, value = used implementation.
     */
    private static final Map<String, Set<String>> interfaceWiseAllServiceProviders = new LinkedHashMap<>();
    private static final Set<String> usedSpInterfaces = new LinkedHashSet<>();
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

    private static boolean isWhiteListedEntryName(String entryName) {
        if (entryName.equals(MODULE_INFO + CLASS)) {
            return true;
        }

        for (String whiteListedDirectory : WHITELISTED_DIRECTORIES) {
            if (entryName.startsWith(whiteListedDirectory)) {
                return true;
            }
        }
        return false;
    }

    private static LinkedHashSet<String> getServiceProviderImplementations(ZipFile originalJarFile,
                                                                           ZipArchiveEntry entry) throws IOException {
        String allImplString = IOUtils.toString(originalJarFile.getInputStream(entry), StandardCharsets.UTF_8);
        String[] serviceImplClassesArr = allImplString.split("\n");
        LinkedHashSet<String> serviceProviderDependencies = new LinkedHashSet<>();

        for (String serviceClass : serviceImplClassesArr) {
            // Skipping the licencing comments
            if (serviceClass.startsWith("#") || serviceClass.isBlank()) {
                continue;
            }
            serviceProviderDependencies.add(getServiceProviderClassName(serviceClass));
        }

        return serviceProviderDependencies;
    }

    private static boolean isServiceProvider(String entryName) {
        return entryName.startsWith(SERVICE_PROVIDER_DIRECTORY);
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
            if (visitedClasses.contains(usedClassName + CLASS)) {
                continue;
            }

            visitedClasses.add(usedClassName + CLASS);
            ZipArchiveEntry usedJarEntry = originalJarFile.getEntry(usedClassName + CLASS);

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

            if (isServiceProvider(currentEntry.getName())) {
                String spInterfaceClassName = getServiceProviderClassName(currentEntry.getName());
                for (String spImplementationClassName : getServiceProviderImplementations(originalJarFile,
                        currentEntry)) {
                    implementationWiseAllServiceProviders.put(spImplementationClassName, spInterfaceClassName);
                    interfaceWiseAllServiceProviders.putIfAbsent(spInterfaceClassName, new LinkedHashSet<>());
                    interfaceWiseAllServiceProviders.get(spInterfaceClassName).add(spImplementationClassName);
                }
            }

            if (isReflectionConfig(currentEntry.getName())) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(originalJarFile.getInputStream(currentEntry), StandardCharsets.UTF_8))) {
                    whitelistReflectionClasses(reader);
                }
            }
        }

        startPointClasses.addAll(GRAALVM_FEATURE_CLASSES);
    }

    private boolean isReflectionConfig(String entryName) {
        return (entryName.endsWith(REFLECT_CONFIG_JSON) || entryName.endsWith(JNI_CONFIG_JSON)) &&
                entryName.startsWith(NATIVE_IMAGE_DIRECTORY);
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
            if (!entryName.endsWith(CLASS)) {
                if (isServiceProvider(entryName)) {
                    return usedSpInterfaces.contains(getServiceProviderClassName(entryName));
                }
                return true;
            }
            return visitedClasses.contains(entryName) || isWhiteListedEntryName(entryName);
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
            if (!entry.isDirectory() && className.endsWith(CLASS) && !visitedClasses.contains(className) &&
                    !className.equals(MODULE_INFO + CLASS)) {
                unusedClasses.add(className);
            }
        }
        return unusedClasses;
    }

    /*
    This function can be used when modularizing the native dependency optimization.
     */
    public boolean jarContainsStartPoints() {
        for (String startPoint : startPointClasses) {
            ZipArchiveEntry jarEntry = originalJarFile.getEntry(startPoint + CLASS);
            if (jarEntry != null) {
                return true;
            }
        }
        return false;
    }
}
