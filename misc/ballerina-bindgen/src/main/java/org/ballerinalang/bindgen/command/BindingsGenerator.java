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

import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.JClass;
import org.ballerinalang.bindgen.model.JError;
import org.ballerinalang.bindgen.utils.BindgenMvnResolver;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenConstants.ARRAY_UTILS_FILE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ARRAY_UTILS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_BINDINGS_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BAL_EXTENSION;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BBGEN_CLASS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BINDINGS_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CONSTANTS_FILE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CONSTANTS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DEFAULT_TEMPLATE_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DEPENDENCIES_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ERROR_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ERROR_TYPES_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JOBJECT_FILE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JOBJECT_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.UTILS_DIR;
import static org.ballerinalang.bindgen.utils.BindgenUtils.createDirectory;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getClassLoader;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getExistingBindings;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getUpdatedConstantsList;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicClass;
import static org.ballerinalang.bindgen.utils.BindgenUtils.notifyExistingDependencies;
import static org.ballerinalang.bindgen.utils.BindgenUtils.writeOutputFile;

/**
 * Class for generating Ballerina bindings for Java APIs.
 *
 * @since 1.2.0
 */
public class BindingsGenerator {

    private Path modulePath;
    private Path projectRoot;
    private Path dependenciesPath;
    private Path utilsDirPath;
    private String mvnGroupId;
    private String mvnArtifactId;
    private String mvnVersion;
    private PrintStream errStream;
    private PrintStream outStream;
    private Set<String> classNames = new HashSet<>();

    private static String outputPath;
    private static boolean directJavaClass = true;
    private static Set<String> classPaths = new HashSet<>();
    private static Path userDir = Paths.get(System.getProperty(USER_DIR));

    private static Set<String> allClasses = new HashSet<>();
    private static Set<String> classListForLooping = new HashSet<>();
    private static Set<String> allJavaClasses = new HashSet<>();
    private static Set<JError> exceptionList = new HashSet<>();
    private static Map<String, String> failedClassGens = new HashMap<>();

    BindingsGenerator(PrintStream out, PrintStream err) {
        this.outStream = out;
        this.errStream = err;
    }

    void generateJavaBindings() throws BindgenException {
        // Resolve existing platform.libraries specified in the Ballerina.toml
        resolvePlatformLibraries();

        // Resolve the maven dependency received through the tool and update the Ballerina.toml file
        // with the direct and transitive platform.libraries
        if ((mvnGroupId != null) && (mvnArtifactId != null) && (mvnVersion != null)) {
            new BindgenMvnResolver(outStream).mavenResolver(mvnGroupId, mvnArtifactId, mvnVersion, projectRoot, true);
        }

        ClassLoader classLoader = setClassLoader();
        if (classLoader != null) {
            setDirectoryPaths();

            // Generate bindings for directly specified Java classes.
            outStream.println("\nGenerating bindings for: ");
            generateBindings(classNames, classLoader, modulePath);

            // Generate bindings for dependent Java classes.
            if (!classListForLooping.isEmpty()) {
                outStream.println("\nGenerating dependency bindings for: ");
                setDependentJavaClass();
            }
            while (!classListForLooping.isEmpty()) {
                Set<String> newSet = new HashSet<>(classListForLooping);
                newSet.removeAll(classNames);
                List<String> existingBindings = getExistingBindings(newSet, modulePath.toFile());
                newSet.removeAll(existingBindings);
                allJavaClasses.addAll(newSet);
                classListForLooping.clear();
                generateBindings(newSet, classLoader, dependenciesPath);
            }

            // Generate the required util files.
            generateUtilFiles();

            // Handle failed binding generations.
            if (failedClassGens != null) {
                handleFailedClassGens();
            }
        }
    }

    private void resolvePlatformLibraries() throws BindgenException {
        if (projectRoot != null) {
            Manifest manifest = TomlParserUtils.getManifest(projectRoot);
            if (manifest != null) {
                List<Library> platformLibraries = manifest.getPlatform().getLibraries();
                if (platformLibraries != null) {
                    for (Library library : platformLibraries) {
                        if (library.path != null) {
                            classPaths.add(Paths.get(projectRoot.toString(), library.path).toString());
                        } else if (library.groupId != null && library.artifactId != null && library.version != null) {
                            new BindgenMvnResolver(outStream).mavenResolver(library.groupId, library.artifactId,
                                    library.version, projectRoot, false);
                        }
                    }
                }
            }
        }
    }

    private ClassLoader setClassLoader() throws BindgenException {
        ClassLoader classLoader;
        try {
            if (!classPaths.isEmpty()) {
                classLoader = getClassLoader(classPaths, this.getClass().getClassLoader());
            } else {
                outStream.println("\nNo classpaths were detected.");
                classLoader = this.getClass().getClassLoader();
            }
        } catch (BindgenException e) {
            throw new BindgenException("Error while loading the classpaths.", e);
        }
        return classLoader;
    }

    private void setDirectoryPaths() throws BindgenException {
        String userPath = userDir.toString();
        if (outputPath != null) {
            if (!Paths.get(outputPath).toFile().exists()) {
                throw new BindgenException("Output path provided [" + outputPath + "] could not be found.");
            }
            userPath = outputPath;
        }
        modulePath = Paths.get(userPath, BALLERINA_BINDINGS_DIR, BINDINGS_DIR);
        dependenciesPath = Paths.get(userPath, BALLERINA_BINDINGS_DIR, DEPENDENCIES_DIR);
        utilsDirPath = Paths.get(userPath, BALLERINA_BINDINGS_DIR, UTILS_DIR);
    }

    private void handleFailedClassGens() {
        errStream.print("\n");
        for (Map.Entry<String, String> entry : failedClassGens.entrySet()) {
            if (classNames.contains(entry.getKey())) {
                errStream.println("Bindings for '" + entry.getKey() + "' class could not be generated.\n\t" +
                        entry.getValue() + "\n");
            }
        }
    }

    private void generateUtilFiles() throws BindgenException {
        String utilsDirStrPath = utilsDirPath.toString();
        createDirectory(utilsDirStrPath);
        createDirectory(Paths.get(utilsDirStrPath, ERROR_TYPES_DIR).toString());

        // Create the JObject.bal file.
        writeOutputFile(null, DEFAULT_TEMPLATE_DIR, JOBJECT_TEMPLATE_NAME,
                Paths.get(utilsDirStrPath, JOBJECT_FILE_NAME).toString(), false);

        // Create the ArrayUtils.bal file.
        writeOutputFile(null, DEFAULT_TEMPLATE_DIR, ARRAY_UTILS_TEMPLATE_NAME,
                Paths.get(utilsDirStrPath, ARRAY_UTILS_FILE_NAME).toString(), false);

        // Create the Constants.bal file.
        Path constantsPath = Paths.get(utilsDirPath.toString(), CONSTANTS_FILE_NAME);
        Set<String> names = new HashSet<>(allClasses);
        if (constantsPath.toFile().exists()) {
            getUpdatedConstantsList(constantsPath, names);
            notifyExistingDependencies(classNames, dependenciesPath.toFile());
        }
        if (!names.isEmpty()) {
            writeOutputFile(names, DEFAULT_TEMPLATE_DIR, CONSTANTS_TEMPLATE_NAME, constantsPath.toString(), true);
        }

        // Create the .bal files for Ballerina error types.
        for (JError jError : exceptionList) {
            String fileName = jError.getShortExceptionName() + BAL_EXTENSION;
            writeOutputFile(jError, DEFAULT_TEMPLATE_DIR, ERROR_TEMPLATE_NAME,
                    Paths.get(utilsDirStrPath, ERROR_TYPES_DIR, fileName).toString(), false);
        }
    }

    static void setOutputPath(String output) {
        outputPath = output;
    }

    void setDependentJars(String[] jarPaths) {
        Collections.addAll(classPaths, jarPaths);
    }

    void setClassNames(List<String> classNames) {
        this.classNames = new HashSet<>(classNames);
    }

    private void generateBindings(Set<String> classList, ClassLoader classLoader, Path modulePath)
            throws BindgenException {
        createDirectory(modulePath.toString());
        for (String c : classList) {
            try {
                if (classLoader != null) {
                    Class classInstance = classLoader.loadClass(c);
                    if (classInstance != null && isPublicClass(classInstance)) {
                        JClass jClass = new JClass(classInstance);
                        String outputFile = Paths.get(modulePath.toString(), jClass.getPackageName()).toString();
                        createDirectory(outputFile);
                        String filePath = Paths.get(outputFile, jClass.getShortClassName() + BAL_EXTENSION).toString();
                        writeOutputFile(jClass, DEFAULT_TEMPLATE_DIR, BBGEN_CLASS_TEMPLATE_NAME, filePath, false);
                        outStream.println("\t" + c);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                failedClassGens.put(c, e.toString());
            } catch (BindgenException e) {
                throw new BindgenException("Error while generating Ballerina bridge code: " + e);
            }
        }
    }

    public static boolean isDirectJavaClass() {
        return directJavaClass;
    }

    private static void setDependentJavaClass() {
        BindingsGenerator.directJavaClass = false;
    }

    public static Set<String> getAllJavaClasses() {
        return allJavaClasses;
    }

    public static void setClassListForLooping(String classListForLooping) {
        BindingsGenerator.classListForLooping.add(classListForLooping);
    }

    public static void setAllClasses(String allClasses) {
        BindingsGenerator.allClasses.add(allClasses);
    }

    public static void setExceptionList(JError exception) {
        BindingsGenerator.exceptionList.add(exception);
    }

    void setProjectRoot(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    void setMvnGroupId(String mvnGroupId) {
        this.mvnGroupId = mvnGroupId;
    }

    void setMvnArtifactId(String mvnArtifactId) {
        this.mvnArtifactId = mvnArtifactId;
    }

    void setMvnVersion(String mvnVersion) {
        this.mvnVersion = mvnVersion;
    }

    public static void setClassPaths(String classPath) {
        BindingsGenerator.classPaths.add(classPath);
    }

    public static String getOutputPath() {
        return outputPath;
    }
}
