/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bindgen.command;

import org.ballerinalang.bindgen.components.JClass;
import org.ballerinalang.bindgen.exceptions.BindgenException;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenConstants.ARRAY_UTILS_FILE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ARRAY_UTILS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BAL_FILE_EXTENSION;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BBGEN_CLASS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CONSTANTS_FILE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CONSTANTS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DEFAULT_TEMPLATE_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DEPENDENCIES_DIR_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA_UTILS_MODULE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JOBJECT_FILE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JOBJECT_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.UTILS_DIR;
import static org.ballerinalang.bindgen.utils.BindgenUtils.createDirectory;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getClassNamesInJar;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getModuleName;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicClass;
import static org.ballerinalang.bindgen.utils.BindgenUtils.writeOutputFile;

/**
 * Class for generating Ballerina bindings for Java APIs.
 */
public class BindingsGenerator {

    private String jarPathString;
    private String outputPath;
    private Set<String> stdClasses;
    private Path modulePath;
    private Path stdJavaModulePath;
    public static boolean directJavaClass = true;
    private Set<String> dependentJars = new HashSet<>();
    private Set<String> packageNames = new HashSet<>();
    private Set<String> classNames = new HashSet<>();
    private static final PrintStream errStream = System.err;
    private static final PrintStream outStream = System.out;
    private static Path userDir = Paths.get(System.getProperty(USER_DIR));

    public static Set<String> allClasses = new HashSet<>();
    public static Set<String> classListForLooping = new HashSet<>();
    public static Set<String> allJavaClasses = new HashSet<>();

    void bindingsFromJar(String jarPath) {

        this.jarPathString = jarPath;
        URLClassLoader classLoader = null;
        try {
            this.dependentJars.add(this.jarPathString);
            classLoader = getClassLoader(this.dependentJars);
        } catch (BindgenException e) {
            errStream.println(e);
        }
        if (classLoader != null) {
            String moduleName = getModuleName(jarPathString);
            if (this.outputPath == null) {
                modulePath = Paths.get(userDir.toString(), moduleName);
                stdJavaModulePath = Paths.get(userDir.toString(), moduleName, DEPENDENCIES_DIR_NAME);
            } else {
                this.modulePath = Paths.get(outputPath, moduleName);
                this.stdJavaModulePath = Paths.get(outputPath, moduleName, DEPENDENCIES_DIR_NAME);
            }
            try {
                Set<String> classes = getClassNamesInJar(jarPathString);
                Set<String> classList = new HashSet<>(classes);
                if (packageNames != null) {
                    filterClasses(packageNames, classList);
                }
                outStream.println("Generating bindings for: ");
                generateBindings(classList, classLoader, modulePath);
                createDirectory(Paths.get(modulePath.toString(), UTILS_DIR).toString());
                writeOutputFile(null, DEFAULT_TEMPLATE_DIR, JOBJECT_TEMPLATE_NAME,
                        Paths.get(modulePath.toString() + UTILS_DIR, JOBJECT_FILE_NAME).toString());
                writeOutputFile(null, DEFAULT_TEMPLATE_DIR, ARRAY_UTILS_TEMPLATE_NAME,
                        Paths.get(modulePath.toString() + UTILS_DIR, ARRAY_UTILS_FILE_NAME).toString());
                outStream.println("\nGenerating dependency bindings for: ");
                while (!classListForLooping.isEmpty()) {
                    Set<String> newSet = new HashSet<>(classListForLooping);
                    allJavaClasses.addAll(newSet);
                    classListForLooping.clear();
                    generateBindings(newSet, classLoader, stdJavaModulePath);
                    directJavaClass = false;
                }
                Set<String> names = new HashSet<>(allClasses);
                writeOutputFile(names, DEFAULT_TEMPLATE_DIR, CONSTANTS_TEMPLATE_NAME,
                        Paths.get(modulePath.toString() + UTILS_DIR, CONSTANTS_FILE_NAME).toString());
                outStream.println("Completed generating bindings for: " + moduleName);

            } catch (IOException | BindgenException e) {
                errStream.println(e);
            } finally {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    errStream.println(e);
                }
            }
        }
    }

    private void filterClasses(Set<String> packages, Set<String> classes) {

        Set<String> classesToRemove = new HashSet<>();
        for (String className : classes) {
            for (String packageName : packages) {
                if (className.startsWith(packageName)) {
                    classesToRemove.add(className);
                    break;
                }
            }
        }
        classes.removeAll(classesToRemove);
    }

    void stdJavaBindings(Set<String> stdClasses) throws BindgenException {

        this.stdClasses = stdClasses;
        if (outputPath == null) {
            stdJavaModulePath = Paths.get(userDir.toString(), JAVA_UTILS_MODULE);
        } else {
            stdJavaModulePath = Paths.get(outputPath, JAVA_UTILS_MODULE);
        }
        if (this.stdClasses != null) {
            allJavaClasses.addAll(this.stdClasses);
            outStream.println("Generating bindings for: ");
            generateBindings(this.stdClasses, this.getClass().getClassLoader(), stdJavaModulePath);
            outStream.println("\nGenerating dependency bindings for: ");
            Path dependenciesPath = Paths.get(stdJavaModulePath.toString(), DEPENDENCIES_DIR_NAME);
            while (!classListForLooping.isEmpty()) {
                Set<String> newSet = new HashSet<>(classListForLooping);
                allJavaClasses.addAll(newSet);
                classListForLooping.clear();
                generateBindings(newSet, this.getClass().getClassLoader(), dependenciesPath);
                directJavaClass = false;
            }
            Set<String> names = new HashSet<>(allClasses);
            createDirectory(Paths.get(stdJavaModulePath.toString(), UTILS_DIR).toString());
            writeOutputFile(null, DEFAULT_TEMPLATE_DIR, JOBJECT_TEMPLATE_NAME,
                    Paths.get(stdJavaModulePath.toString() + UTILS_DIR, JOBJECT_FILE_NAME).toString());
            writeOutputFile(null, DEFAULT_TEMPLATE_DIR, ARRAY_UTILS_TEMPLATE_NAME,
                    Paths.get(stdJavaModulePath.toString() + UTILS_DIR, ARRAY_UTILS_FILE_NAME).toString());
            writeOutputFile(names, DEFAULT_TEMPLATE_DIR, CONSTANTS_TEMPLATE_NAME,
                    Paths.get(stdJavaModulePath.toString() + UTILS_DIR, CONSTANTS_FILE_NAME).toString());
        }
    }

    void setOutputPath(String outputPath) {

        this.outputPath = outputPath;
    }

    private URLClassLoader getClassLoader(Set<String> jarPaths) throws BindgenException {

        URLClassLoader classLoader;
        List<URL> urls = new ArrayList<>();
        try {
            for (String path : jarPaths) {
                urls.add(FileSystems.getDefault().getPath(path).toFile().toURI().toURL());
            }
            classLoader = (URLClassLoader) AccessController.doPrivileged((PrivilegedAction) ()
                    -> new URLClassLoader(urls.toArray(new URL[urls.size()])));
        } catch (Exception e) {
            throw new BindgenException("Error while processing the jar path: ", e);
        }
        return classLoader;
    }

    void setDependentJars(String[] jarPaths) {

        Collections.addAll(this.dependentJars, jarPaths);
    }

    void setPackageNames(String[] packageNames) {

        Collections.addAll(this.packageNames, packageNames);
    }

    void setClassNames(String[] classNames) {

        Collections.addAll(this.classNames, classNames);
    }

    private void generateBindings(Set<String> classList, ClassLoader classLoader, Path modulePath)
            throws BindgenException {

        createDirectory(modulePath.toString());
        for (String c : classList) {
            try {
                if (classLoader != null) {
                    Class classInstance = classLoader.loadClass(c);
                    if (classInstance != null && isPublicClass(classInstance) && !classInstance.isEnum()) {
                        JClass jClass = new JClass(classInstance);
                        String outputFile = Paths.get(modulePath.toString(), jClass.packageName).toString();
                        createDirectory(outputFile);
                        String filePath = Paths.get(outputFile, jClass.shortClassName + BAL_FILE_EXTENSION).toString();
                        writeOutputFile(jClass, DEFAULT_TEMPLATE_DIR, BBGEN_CLASS_TEMPLATE_NAME, filePath);
                        outStream.println("\t" + c);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                errStream.println("Bindings for class " + c + " could not be created.");
            } catch (BindgenException e) {
                throw new BindgenException("Error while generating Ballerina bridge code: " + e);
            }
        }
    }
}
