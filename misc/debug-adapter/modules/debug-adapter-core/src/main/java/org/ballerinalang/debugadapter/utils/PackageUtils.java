/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.utils;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import org.ballerinalang.debugadapter.DebugSourceType;
import org.ballerinalang.debugadapter.ExecutionContext;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static org.ballerinalang.debugadapter.DebugSourceType.DEPENDENCY;
import static org.ballerinalang.debugadapter.DebugSourceType.PACKAGE;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;

/**
 * Package Utils.
 */
public class PackageUtils {

    public static final String BAL_FILE_EXT = ".bal";
    public static final String BAL_TOML_FILE_NAME = "Ballerina.toml";
    public static final String INIT_CLASS_NAME = "$_init";
    public static final String INIT_TYPE_INSTANCE_PREFIX = "$type$";
    public static final String GENERATED_VAR_PREFIX = "$";
    static final String MODULE_DIR_NAME = "modules";
    private static final String URI_SCHEME_FILE = "file";
    private static final String URI_SCHEME_BALA = "bala";

    private static final String FILE_SEPARATOR_REGEX = File.separatorChar == '\\' ? "\\\\" : File.separator;

    /**
     * Returns the corresponding debug source path based on the given stack frame location.
     *
     * @param stackFrameLocation stack frame location
     * @param sourceProject      project instance of the detected debug source
     */
    public static Optional<Map.Entry<Path, DebugSourceType>> getStackFrameSourcePath(Location stackFrameLocation,
                                                                                     Project sourceProject) {
        // Source resolving is processed according to the following order .
        // 1. Checks whether debug hit location resides within the current debug source project and if so, returns
        // the absolute path of the project file source.
        // 2. Checks whether the debug hit location resides within a internal dependency (lang library) and if so,
        // returns the absolute file path resolved using package cache.
        // 3. Checks whether the debug hit location resides within a external dependency (standard library or central
        // module) and if so, returns the dependency file path resolved using package resolution.
        List<SourceResolver> sourceResolvers = new ArrayList<>();
        sourceResolvers.add(new ProjectSourceResolver(sourceProject));
        sourceResolvers.add(new LangLibSourceResolver(sourceProject));
        sourceResolvers.add(new DependencySourceResolver(sourceProject));

        for (SourceResolver sourceResolver : sourceResolvers) {
            if (sourceResolver.isSupported(stackFrameLocation)) {
                Optional<Path> resolvedPath = sourceResolver.resolve(stackFrameLocation);
                if (resolvedPath.isPresent()) {
                    if (sourceResolver instanceof DependencySourceResolver) {
                        return Optional.of(new AbstractMap.SimpleEntry<>(resolvedPath.get(), DEPENDENCY));
                    } else {
                        return Optional.of(new AbstractMap.SimpleEntry<>(resolvedPath.get(), PACKAGE));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Loads the target ballerina source project instance using the Project API, from the file path of the open/active
     * editor instance in the client(plugin) side.
     *
     * @param filePath file path of the open/active editor instance in the plugin side.
     */
    public static Project loadProject(String filePath) {
        Map.Entry<ProjectKind, Path> projectKindAndProjectRootPair = computeProjectKindAndRoot(Paths.get(filePath));
        ProjectKind projectKind = projectKindAndProjectRootPair.getKey();
        Path projectRoot = projectKindAndProjectRootPair.getValue();
        BuildOptions options = new BuildOptionsBuilder().offline(true).build();
        if (projectKind == ProjectKind.BUILD_PROJECT) {
            return BuildProject.load(projectRoot, options);
        } else if (projectKind == ProjectKind.SINGLE_FILE_PROJECT) {
            return SingleFileProject.load(projectRoot, options);
        } else {
            return ProjectLoader.loadProject(projectRoot, options);
        }
    }

    /**
     * Computes the source root and the shape(kind) of the enclosing Ballerina project, using the given file path.
     *
     * @param path file path
     * @return A pair of project kind and the project root.
     */
    public static Map.Entry<ProjectKind, Path> computeProjectKindAndRoot(Path path) {
        if (ProjectPaths.isStandaloneBalFile(path)) {
            return new AbstractMap.SimpleEntry<>(ProjectKind.SINGLE_FILE_PROJECT, path);
        }
        // Following is a temp fix to distinguish Bala and Build projects.
        Path tomlPath = ProjectPaths.packageRoot(path).resolve(ProjectConstants.BALLERINA_TOML);
        if (Files.exists(tomlPath)) {
            return new AbstractMap.SimpleEntry<>(ProjectKind.BUILD_PROJECT, ProjectPaths.packageRoot(path));
        }
        return new AbstractMap.SimpleEntry<>(ProjectKind.BALA_PROJECT, ProjectPaths.packageRoot(path));
    }

    /**
     * Returns the org name for a given ballerina project source.
     *
     * @param project ballerina project instance
     * @return organization name
     */
    public static String getOrgName(Project project) {
        if (project instanceof BuildProject) {
            return project.currentPackage().packageOrg().value();
        }
        return "";
    }

    /**
     * Returns the default module name for a given ballerina project source.
     *
     * @param project ballerina project instance
     * @return organization name
     */
    public static String getDefaultModuleName(Project project) {
        if (project instanceof BuildProject) {
            return project.currentPackage().getDefaultModule().moduleName().toString();
        }
        return "";
    }

    public static String getFileNameFrom(Path filePath) {
        try {
            String[] split = filePath.toString().split(FILE_SEPARATOR_REGEX);
            String fileName = split[split.length - 1];
            if (fileName.endsWith(BAL_FILE_EXT)) {
                return fileName.replace(BAL_FILE_EXT, "");
            }
            return fileName;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns full-qualified class name for a given ballerina JVM generated class name.
     *
     * @param context   suspended context
     * @param className class name
     * @return full-qualified class name
     */
    public static String getQualifiedClassName(SuspendedContext context, String className) {
        if (context.getSourceType() == DebugSourceType.SINGLE_FILE) {
            return className;
        }
        StringJoiner classNameJoiner = new StringJoiner(".");
        classNameJoiner.add(context.getPackageOrg().get())
                .add(context.getModuleName().get())
                .add(context.getPackageMajorVersion().get())
                .add(className);
        return classNameJoiner.toString();
    }

    /**
     * Returns the derived full-qualified class name for a given ballerina source file.
     *
     * @param filePath file path
     * @return full-qualified class name
     */
    public static Optional<String> getQualifiedClassName(ExecutionContext context, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Project project = context.getProjectCache().getProject(path);
            if (project instanceof SingleFileProject) {
                DocumentId documentId = project.currentPackage().getDefaultModule().documentIds().iterator().next();
                String docName = project.currentPackage().getDefaultModule().document(documentId).name();
                if (docName.endsWith(BAL_FILE_EXT)) {
                    docName = docName.replace(BAL_FILE_EXT, "");
                }
                return Optional.of(docName);
            }

            DocumentId documentId = project.documentId(path);
            Module module = project.currentPackage().module(documentId.moduleId());
            Document document = module.document(documentId);

            // Need to use only the major version of the packages/modules, as qualified class names of the generated
            // ballerina classes includes only the major version.
            int packageMajorVersion = document.module().packageInstance().packageVersion().value().major();
            StringJoiner classNameJoiner = new StringJoiner(".");
            classNameJoiner.add(document.module().packageInstance().packageOrg().value())
                    .add(encodeModuleName(document.module().moduleName().toString()))
                    .add(String.valueOf(packageMajorVersion))
                    .add(document.name().replace(BAL_FILE_EXT, "").replace(FILE_SEPARATOR_REGEX, ".")
                            .replace("/", "."));

            return Optional.ofNullable(classNameJoiner.toString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Returns full-qualified class name for a given JDI class reference instance.
     *
     * @param context       Debug context
     * @param referenceType JDI class reference instance
     * @return full-qualified class name
     */
    public static String getQualifiedClassName(ExecutionContext context, ReferenceType referenceType) {
        try {
            List<String> paths = referenceType.sourcePaths(null);
            List<String> names = referenceType.sourceNames(null);
            if (paths.isEmpty() || names.isEmpty()) {
                return referenceType.name();
            }
            String path = paths.get(0);
            String name = names.get(0);
            String[] nameParts = getQModuleNameParts(name);
            String srcFileName = nameParts[nameParts.length - 1];

            if (!path.endsWith(BAL_FILE_EXT) || (context.getSourceProject() instanceof BuildProject &&
                    !path.startsWith(context.getSourceProject().currentPackage().packageOrg().value()))) {
                return referenceType.name();
            }

            // Removes ".bal" extension if exists.
            srcFileName = srcFileName.replaceAll(BAL_FILE_EXT + "$", "");
            path = path.replaceAll(name + "$", srcFileName);
            return replaceSeparators(path);
        } catch (Exception e) {
            return referenceType.name();
        }
    }

    /**
     * Closes the given Closeable and swallows any IOException that may occur.
     *
     * @param c Closeable to close, can be null.
     */
    public static void closeQuietly(final Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (final IOException ignored) { // NOPMD
            }
        }
    }

    /**
     * Retrieves name parts (org name, module name, file name) from the given qualified ballerina module name.
     */
    public static String[] getQModuleNameParts(String path) {
        String[] moduleParts;
        // Makes the path os-independent, as some of the incoming windows source paths can contain both of the
        // separator types(possibly due to a potential JDI bug).
        path = path.replaceAll("\\\\", "/");
        if (path.contains("/")) {
            moduleParts = path.split("/");
        } else {
            moduleParts = new String[]{path};
        }
        return moduleParts;
    }

    /**
     * Converts a given file URI to a Ballerina-specific custom URI scheme.
     *
     * @param fileUri file URI
     * @return bala URI
     */
    public static URI covertToBalaUri(URI fileUri) throws URISyntaxException, IllegalArgumentException {
        if (fileUri.getScheme().equals(URI_SCHEME_FILE)) {
            return new URI(URI_SCHEME_BALA, fileUri.getHost(), fileUri.getPath(), fileUri.getFragment());
        }

        throw new IllegalArgumentException("unsupported URI with scheme: " + fileUri.getScheme());
    }

    private static String replaceSeparators(String path) {
        if (path.contains("/")) {
            return path.replaceAll("/", ".");
        } else if (path.contains("\\")) {
            return path.replaceAll("\\\\", ".");
        }
        return path;
    }
}
