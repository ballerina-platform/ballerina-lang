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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenConstants.BAL_EXTENSION;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MODULES_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;
import static org.ballerinalang.bindgen.utils.BindgenUtils.createDirectory;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getClassLoader;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicClass;
import static org.ballerinalang.bindgen.utils.BindgenUtils.outputSyntaxTreeFile;

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
    private PrintStream errStream;
    private PrintStream outStream;
    private Set<String> classNames = new HashSet<>();
    private Path userDir = Paths.get(System.getProperty(USER_DIR));

    BindingsGenerator(PrintStream out, PrintStream err) {
        this.outStream = out;
        this.errStream = err;
        this.env = new BindgenEnv();
    }

    void generateJavaBindings() throws BindgenException {
        outStream.println("\nResolving maven dependencies...");
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

            // Generate bindings for super classes of directly specified Java classes.
            if (!env.getSuperClasses().isEmpty()) {
                env.setAllJavaClasses(env.getSuperClasses());
                // Remove the explicitly generated classes from the list of super classes.
                env.getSuperClasses().removeAll(classNames);
                generateBindings(new HashSet<>(env.getSuperClasses()), classLoader, modulePath);
            }

            // Generate bindings for dependent Java classes.
            if (!env.getClassListForLooping().isEmpty()) {
                outStream.println("\nGenerating dependency bindings for: ");
                env.setDirectJavaClass(false);
            }
            while (!env.getClassListForLooping().isEmpty()) {
                Set<String> newSet = new HashSet<>(env.getClassListForLooping());
                newSet.removeAll(classNames);
                newSet.removeAll(env.getSuperClasses());
                env.setAllJavaClasses(newSet);
                env.clearClassListForLooping();
                generateBindings(newSet, classLoader, dependenciesPath);
            }

            // Generate the required util files.
            generateUtilFiles();

            // Handle failed binding generations.
            if (env.getFailedClassGens() != null) {
                handleFailedClassGens();
            }

            // Handle failed method generations.
            if (env.getFailedMethodGens() != null) {
                for (String entry : env.getFailedMethodGens()) {
                    errStream.println(entry);
                }
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
            PackageManifest packageManifest = ManifestBuilder.from(tomlDocument, null,
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
        if (!env.getClassPaths().isEmpty()) {
            classLoader = getClassLoader(env.getClassPaths(), this.getClass().getClassLoader());
        } else {
            outStream.println("\nNo classpaths were detected.");
            classLoader = this.getClass().getClassLoader();
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
                throw new BindgenException("error: output path provided could not be found: " + outputPath);
            }
            userPath = outputPath;
        }
        utilsDirPath = dependenciesPath = modulePath = Paths.get(userPath);
    }

    private void handleFailedClassGens() {
        errStream.print("\n");
        for (Map.Entry<String, String> entry : env.getFailedClassGens().entrySet()) {
            if (classNames.contains(entry.getKey())) {
                errStream.println("error: unable to generate the '" + entry.getKey() + "' binding class: "
                        + entry.getValue());
            }
        }
    }

    private void generateUtilFiles() throws BindgenException {
        String utilsDirStrPath = utilsDirPath.toString();
        createDirectory(utilsDirStrPath);

        // Create the .bal files for Ballerina error types.
        for (JError jError : env.getExceptionList()) {
            String fileName = jError.getShortExceptionName() + BAL_EXTENSION;
            if (env.getModulesFlag()) {
                utilsDirStrPath = Paths.get(modulePath.toString(), jError.getPackageName()).toString();
                createDirectory(utilsDirStrPath);
            }
            // The folder structure is flattened to address the Project API changes.
            outputSyntaxTreeFile(jError, env, Paths.get(utilsDirStrPath, fileName).toString(), false);
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
                        Path filePath;
                        if (env.getModulesFlag()) {
                            String outputFile = Paths.get(modulePath.toString(), jClass.getPackageName()).toString();
                            createDirectory(outputFile);
                            filePath = Paths.get(outputFile, jClass.getShortClassName() + BAL_EXTENSION);
                        } else {
                            filePath = Paths.get(modulePath.toString(), jClass.getShortClassName() + BAL_EXTENSION);
                        }
                        // Prevent the overwriting of existing class implementations with partially generated classes.
                        if (Files.exists(filePath) && !env.isDirectJavaClass()) {
                            return;
                        }
                        outputSyntaxTreeFile(jClass, env, filePath.toString(), false);
                        outStream.println("\t" + c);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                env.setFailedClassGens(c, e.toString());
            }
        }
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
        this.env.setPublicFlag(true);
    }

    void setModulesFlag(boolean modulesFlag) {
        this.env.setPublicFlag(true);
        this.env.setModulesFlag(modulesFlag);
    }

    void setProject(Project project) {
        this.env.setProject(project);
    }
}
