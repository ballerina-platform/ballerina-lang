package org.wso2.ballerinalang.compiler.bir.codegen.bytecodeOptimizer;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class NativeDependencyOptimizer {

    private final Set<String> startPointClasses;
    private final Stack<String> usedClassesStack;
    private final Set<String> externalClasses = new HashSet<>();
    private final Set<String> visitedClasses = new HashSet<>();
    private final ZipFile originalJarFile;
    private final ZipArchiveOutputStream optimizedJarStream;
    private static final String CLASS = ".class";
    private static final String SERVICE_PROVIDER_DIRECTORY = "META-INF/services/";

    /**
     * key = implementation class name
     * value = interface class name
     * Since one interface can be implemented by more than one child class, it is possible to have duplicate values
     */
    private static final Map<String, String> implementationWiseAllServiceProviders = new HashMap<>();
    /**
     * key = used interface
     * value = used implementation
     */
    private static final Map<String, HashSet<String>> interfaceWiseAllServiceProviders = new HashMap<>();
    private static final Set<String> usedSpInterfaces = new HashSet<>();

    public NativeDependencyOptimizer(HashSet<String> startPointClasses, ZipFile originalJarFile,
                                     ZipArchiveOutputStream optimizedJarStream) {

        this.startPointClasses = startPointClasses;
        this.originalJarFile = originalJarFile;
        this.optimizedJarStream = optimizedJarStream;
        this.usedClassesStack = new Stack<>();
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

    public void analyzeServiceProviders() throws IOException {
        Enumeration<ZipArchiveEntry> jarEntries = originalJarFile.getEntries();
        while (jarEntries.hasMoreElements()) {
            ZipArchiveEntry currentEntry = jarEntries.nextElement();
            if (isServiceProvider(currentEntry.getName())) {

                String spInterfaceClassName = getServiceProviderClassName(currentEntry.getName());
                for (String spImplementationClassName : getServiceProviderImplementations(originalJarFile,
                        currentEntry)) {
                    implementationWiseAllServiceProviders.put(spImplementationClassName, spInterfaceClassName);
                    interfaceWiseAllServiceProviders.putIfAbsent(spInterfaceClassName, new HashSet<>());
                    interfaceWiseAllServiceProviders.get(spInterfaceClassName).add(spImplementationClassName);
                }
            }
        }
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
            return visitedClasses.contains(entryName) || entryName.equals("module-info.class");
        };

        originalJarFile.copyRawEntries(optimizedJarStream, usedClassPredicate);
    }

    private static HashSet<String> getServiceProviderImplementations(ZipFile originalJarFile, ZipArchiveEntry entry) throws IOException {
        String allImplString = IOUtils.toString(originalJarFile.getInputStream(entry), StandardCharsets.UTF_8);
        String[] serviceImplClassesArr = allImplString.split("\n");
        HashSet<String> serviceProviderDependencies = new HashSet<>();

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

    private static String getServiceProviderFileName(String implementationClassName) {
        return implementationClassName.replace("/", ".");
    }

    public boolean jarContainsStartPoints() {
        for (String startPoint : startPointClasses) {
            ZipArchiveEntry jarEntry = originalJarFile.getEntry(startPoint + CLASS);
            if (jarEntry != null) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getExternalClasses() {
        return externalClasses;
    }
}
