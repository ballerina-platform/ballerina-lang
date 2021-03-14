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

import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.ManifestBuilder;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.JClass;
import org.ballerinalang.bindgen.model.JError;
import org.ballerinalang.bindgen.utils.BindgenEnv;
import org.ballerinalang.bindgen.utils.BindgenMvnResolver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenConstants.BAL_EXTENSION;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BBGEN_CLASS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CONSTANTS_FILE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CONSTANTS_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DEFAULT_TEMPLATE_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ERROR_TEMPLATE_NAME;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MODULES_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;
import static org.ballerinalang.bindgen.utils.BindgenUtils.createDirectory;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getClassLoader;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getUpdatedConstantsList;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicClass;
import static org.ballerinalang.bindgen.utils.BindgenUtils.writeOutputFile;

/**
 * Class for generating Ballerina bindings for Java APIs.
 *
 * @since 1.2.0
 */
public class BindingsGenerator {

    private final BindgenEnv env;

    private Path modulePath;
    private Path dependenciesPath;
    private Path utilsDirPath;
    private String mvnGroupId;
    private String mvnArtifactId;
    private String mvnVersion;
    private String accessModifier;
    private PrintStream errStream;
    private PrintStream outStream;
    private Set<String> classNames = new HashSet<>();
    private Path userDir = Paths.get(System.getProperty(USER_DIR));

    private static Set<String> allClasses = new HashSet<>();
    private static Set<String> allPackages = new HashSet<>();
    private static Set<String> classListForLooping = new HashSet<>();
    private static Set<String> allJavaClasses = new HashSet<>();
    private static Set<JError> exceptionList = new HashSet<>();
    private static Map<String, String> failedClassGens = new HashMap<>();

    public static Map<String, String> aliases = new HashMap<>();

    BindingsGenerator(PrintStream out, PrintStream err) {
        this.outStream = out;
        this.errStream = err;
        this.env = new BindgenEnv();
    }

    void generateJavaBindings() throws BindgenException {
        // Resolve existing platform.libraries specified in the Ballerina.toml
        resolvePlatformLibraries();

        // Resolve the maven dependency received through the tool and update the Ballerina.toml file
        // with the direct and transitive platform.libraries
        if ((mvnGroupId != null) && (mvnArtifactId != null) && (mvnVersion != null)) {
            new BindgenMvnResolver(outStream, env).mavenResolver(mvnGroupId, mvnArtifactId, mvnVersion,
                    env.getProjectRoot(), true);
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
                env.setDirectJavaClass(false);
            }
            while (!classListForLooping.isEmpty()) {
                Set<String> newSet = new HashSet<>(classListForLooping);
                newSet.removeAll(classNames);
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
            try {
                ((URLClassLoader) classLoader).close();
            } catch (IOException e) {
                outStream.println("\nError while exiting the classloader:\n" + e.getMessage());
            } catch (ClassCastException ignore) {
                // Ignore if the classloader is not a URLClassLoader.
            }
        }
    }

    private void resolvePlatformLibraries() throws BindgenException {
        if (env.getProjectRoot() != null) {
            TomlDocument tomlDocument = env.getTomlDocument();
            PackageManifest.Platform platform = getPackagePlatform(tomlDocument);
            if (platform != null && platform.dependencies() != null) {
                for (Map<String, Object> library : platform.dependencies()) {
                    if (library.get("path") != null) {
                        handlePathDependency(library.get("path").toString());
                    } else if (library.get("groupId") != null && library.get("artifactId") != null &&
                            library.get("version") != null) {
                        resolveMvnDependency(library.get("groupId").toString(),
                                library.get("artifactId").toString(), library.get("version").toString());
                    }
                }
            }
        }
    }

    private PackageManifest.Platform getPackagePlatform(TomlDocument tomlDocument) {
        if (tomlDocument != null) {
            PackageManifest packageManifest = ManifestBuilder.from(tomlDocument, null, null,
                    env.getProjectRoot()).packageManifest();
            if (packageManifest != null) {
                return packageManifest.platform(JvmTarget.JAVA_11.code());
            }
        }
        return null;
    }

    private void handlePathDependency(String libPath) {
        Path libraryPath;
        if (Paths.get(libPath).isAbsolute()) {
            libraryPath = Paths.get(libPath);
        } else {
            libraryPath = Paths.get(env.getProjectRoot().toString(), libPath);
        }
        env.addClasspath(libraryPath.toString());
    }

    private void resolveMvnDependency(String mvnGroupId, String mvnArtifactId, String mvnVersion)
            throws BindgenException {
        new BindgenMvnResolver(outStream, env).mavenResolver(mvnGroupId, mvnArtifactId, mvnVersion,
                env.getProjectRoot(), false);
    }

    private ClassLoader setClassLoader() throws BindgenException {
        ClassLoader classLoader;
        try {
            if (!env.getClassPaths().isEmpty()) {
                classLoader = getClassLoader(env.getClassPaths(), this.getClass().getClassLoader());
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
        String outputPath = env.getOutputPath();
        if (env.getModulesFlag()) {
            userPath = Paths.get(userPath, MODULES_DIR).toString();
        } else if (outputPath != null) {
            if (!Paths.get(outputPath).toFile().exists()) {
                throw new BindgenException("Output path provided [" + outputPath + "] could not be found.");
            }
            userPath = outputPath;
        }
        utilsDirPath = dependenciesPath = modulePath = Paths.get(userPath);
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

        // Create the Constants.bal file.
        if (!env.getModulesFlag()) {
            Path constantsPath = Paths.get(utilsDirPath.toString(), CONSTANTS_FILE_NAME);
            generateConstantFiles(constantsPath);
        } else {
            for (String packagePath : allPackages) {
                Path constantsPath = Paths.get(modulePath.toString(), packagePath, CONSTANTS_FILE_NAME);
                generateConstantFiles(constantsPath);
            }
        }

        // Create the .bal files for Ballerina error types.
        for (JError jError : exceptionList) {
            jError.setAccessModifier(accessModifier);
            String fileName = jError.getShortExceptionName() + BAL_EXTENSION;
            if (env.getModulesFlag()) {
                utilsDirStrPath = Paths.get(modulePath.toString(), jError.getPackageName()).toString();
                createDirectory(utilsDirStrPath);
            }
            // The folder structure is flattened to address the Project API changes.
            writeOutputFile(jError, DEFAULT_TEMPLATE_DIR, ERROR_TEMPLATE_NAME,
                    Paths.get(utilsDirStrPath, fileName).toString(), false);
        }
    }

    private void generateConstantFiles(Path constantPaths) throws BindgenException {
        Set<String> names = new HashSet<>(allClasses);
        if (constantPaths.toFile().exists()) {
            getUpdatedConstantsList(constantPaths, names);
        }
        if (!names.isEmpty()) {
            writeOutputFile(names, DEFAULT_TEMPLATE_DIR, CONSTANTS_TEMPLATE_NAME, constantPaths.toString(), true);
        }
    }

    void setOutputPath(String output) {
        this.env.setOutputPath(output);
    }

    void setDependentJars(String[] jarPaths) {
        for (String path : jarPaths) {
            env.addClasspath(path);
        }
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
                        JClass jClass = new JClass(classInstance, env);
                        jClass.setAccessModifier(accessModifier);
                        allPackages.add(jClass.getPackageName());
                        String filePath;
                        if (env.getModulesFlag()) {
                            String outputFile = Paths.get(modulePath.toString(), jClass.getPackageName()).toString();
                            createDirectory(outputFile);
                            filePath = Paths.get(outputFile, jClass.getShortClassName() + BAL_EXTENSION).toString();
                        } else {
                            filePath = Paths.get(modulePath.toString(), jClass.getShortClassName()
                                    + BAL_EXTENSION).toString();
                        }
                        writeOutputFile(jClass, DEFAULT_TEMPLATE_DIR, BBGEN_CLASS_TEMPLATE_NAME, filePath, false);
                        outStream.println("\t" + c);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                failedClassGens.put(c, e.toString());
            }
        }
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

    void setMvnGroupId(String mvnGroupId) {
        this.mvnGroupId = mvnGroupId;
    }

    void setMvnArtifactId(String mvnArtifactId) {
        this.mvnArtifactId = mvnArtifactId;
    }

    void setMvnVersion(String mvnVersion) {
        this.mvnVersion = mvnVersion;
    }

    void setPublic() {
        this.accessModifier = "public ";
    }

    void setModulesFlag(boolean modulesFlag) {
        this.env.setModulesFlag(modulesFlag);
    }

    void setProject(Project project) {
        this.env.setProject(project);
    }
}
