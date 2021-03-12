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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.debugadapter.DebugSourceType;
import org.ballerinalang.debugadapter.ExecutionContext;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.runtime.api.utils.IdentifierUtils.decodeIdentifier;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;

/**
 * Package Utils.
 */
public class PackageUtils {

    public static final String BAL_FILE_EXT = ".bal";
    public static final String INIT_CLASS_NAME = "$_init";
    public static final String INIT_TYPE_INSTANCE_PREFIX = "$type$";
    public static final String GENERATED_VAR_PREFIX = "$";
    private static final String MODULE_DIR_NAME = "modules";

    private static final String SEPARATOR_REGEX = File.separatorChar == '\\' ? "\\\\" : File.separator;

    /**
     * Derives the source path of the breakpoint location from the JDI breakpoint hit information.
     */
    public static Path getSrcPathFromBreakpointLocation(Location location, Project currentProject)
            throws AbsentInformationException {

        String sourcePath = location.sourcePath();
        String[] moduleParts = getQModuleNameParts(sourcePath);
        String locationOrg = moduleParts[0];
        String locationModule = decodeIdentifier(moduleParts[1]);

        // 1. If the debug hit location resides within the current debug source project, retrieves the returns the
        // absolute path of the project file source.
        // 2. Else, checks whether the debug hit location resides within a project dependency (lang library, standard
        // library, central module, etc.) and if so, retrieves the dependency file path.
        if (isWithinProject(location, currentProject)) {
            return getSourceFilePath(location, currentProject);
        } else if (isWithinProjectDependency(currentProject, locationOrg, locationModule)) {
            return getDependencyFilePath(currentProject, locationOrg, locationModule, moduleParts[3]);
        }
        return null;
    }

    /**
     * Returns whether the given debug hit location resides within the sources of the given project/package.
     */
    private static boolean isWithinProject(Location location, Project project) throws AbsentInformationException {
        if (project instanceof SingleFileProject) {
            DocumentId docId = project.currentPackage().getDefaultModule().documentIds().iterator().next();
            Document document = project.currentPackage().getDefaultModule().document(docId);
            return document.name().equals(location.sourcePath()) && document.name().equals(location.sourceName());
        } else if (project instanceof BuildProject) {
            String projectOrg = getOrgName(project);
            String locSourcePath = location.sourcePath();
            String[] moduleNameParts = getQModuleNameParts(locSourcePath);
            String locationOrg = moduleNameParts[0];
            return locationOrg.equals(projectOrg);
        }
        return false;
    }

    /**
     * Returns whether the given debug hit location resides within a dependency module of the given project/package.
     */
    private static boolean isWithinProjectDependency(Project project, String orgName, String moduleName) {
        List<PackageName> possiblePackageNames = ProjectUtils.getPossiblePackageNames(moduleName);
        for (PackageName possiblePackageName : possiblePackageNames) {
            PackageResolution resolution = project.currentPackage().getResolution();
            for (ResolvedPackageDependency resolvedPackageDependency : resolution.dependencyGraph().getNodes()) {
                if (resolvedPackageDependency.packageInstance().packageOrg().value().equals(orgName)
                        && resolvedPackageDependency.packageInstance().packageName().equals(possiblePackageName)) {
                    return true;
                }
            }
        }
        return false;
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
            String[] split = filePath.toString().split(SEPARATOR_REGEX);
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
                .add(context.getPackageVersion().get().replace(".", "_"))
                .add(className);
        return classNameJoiner.toString();
    }

    /**
     * Returns the derived full-qualified class name for a given ballerina source file.
     *
     * @param filePath file path
     * @return full-qualified class name
     */
    public static String getQualifiedClassName(String filePath) {
        Path path = Paths.get(filePath);
        Project project = ProjectLoader.loadProject(path);
        if (project instanceof SingleFileProject) {
            DocumentId documentId = project.currentPackage().getDefaultModule().documentIds().iterator().next();
            String docName = project.currentPackage().getDefaultModule().document(documentId).name();
            if (docName.endsWith(BAL_FILE_EXT)) {
                docName = docName.replace(BAL_FILE_EXT, "");
            }
            return docName;
        }

        DocumentId documentId = project.documentId(path);
        Module module = project.currentPackage().module(documentId.moduleId());
        Document document = module.document(documentId);

        StringJoiner classNameJoiner = new StringJoiner(".");
        classNameJoiner.add(document.module().packageInstance().packageOrg().value())
                .add(encodeModuleName(document.module().moduleName().toString()))
                .add(document.module().packageInstance().packageVersion().toString().replace(".", "_"))
                .add(document.name().replace(BAL_FILE_EXT, "").replace(SEPARATOR_REGEX, ".").replace("/", "."));
        return classNameJoiner.toString();
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
     * Retries name parts (org name,package name, module name) from the given qualified ballerina module name.
     */
    public static String[] getQModuleNameParts(String path) {
        String[] srcNames;
        if (path.contains("/")) {
            srcNames = path.split("/");
        } else if (path.contains("\\")) {
            srcNames = path.split("\\\\");
        } else {
            srcNames = new String[]{path};
        }
        return srcNames;
    }

    private static String replaceSeparators(String path) {
        if (path.contains("/")) {
            return path.replaceAll("/", ".");
        } else if (path.contains("\\")) {
            return path.replaceAll("\\\\", ".");
        }
        return path;
    }

    /**
     * Returns the absolute file path of the project source, which is represented by the given JDI location information.
     */
    private static Path getSourceFilePath(Location location, Project project) throws AbsentInformationException {
        String projectRoot = project.sourceRoot().toAbsolutePath().toString();

        if (project instanceof SingleFileProject) {
            DocumentId docId = project.currentPackage().getDefaultModule().documentIds().iterator().next();
            Document document = project.currentPackage().getDefaultModule().document(docId);
            if (!document.name().equals(location.sourcePath()) || !document.name().equals(location.sourceName())) {
                return null;
            }
            return Paths.get(projectRoot);
        } else if (project instanceof BuildProject) {
            String projectOrg = getOrgName(project);
            String defaultModuleName = getDefaultModuleName(project);
            String locationPath = location.sourcePath();
            String locationName = location.sourceName();
            String[] moduleNameParts = getQModuleNameParts(locationPath);
            String locationOrg = moduleNameParts[0];

            if (!locationOrg.equals(projectOrg)) {
                return null;
            }

            String modulePart = decodeIdentifier(moduleNameParts[1]);
            modulePart = modulePart.replaceFirst(defaultModuleName, "");
            if (modulePart.startsWith(".")) {
                modulePart = modulePart.replaceFirst("\\.", "");
            }

            if (modulePart.isBlank()) {
                // default module
                return Paths.get(projectRoot, locationName);
            } else {
                // other modules
                return Paths.get(projectRoot, MODULE_DIR_NAME, modulePart, locationName);
            }
        }
        return null;
    }

    /**
     * Returns the path of the specified file from dependency bala.
     *
     * @param project    current ballerina debug source project
     * @param orgName    package org of the dependency
     * @param moduleName module name of the dependency
     * @param filename   name of the file to get the path of
     * @return path of the file
     * @throws ProjectException if file cannot be found in the dependency package
     */
    private static Path getDependencyFilePath(Project project, String orgName, String moduleName, String filename) {
        List<PackageName> possiblePackageNames = ProjectUtils.getPossiblePackageNames(moduleName);
        for (PackageName possiblePackageName : possiblePackageNames) {
            PackageResolution resolution = project.currentPackage().getResolution();
            for (ResolvedPackageDependency resolvedPackageDependency : resolution.dependencyGraph().getNodes()) {
                if (!resolvedPackageDependency.packageInstance().packageOrg().value().equals(orgName)
                        || !resolvedPackageDependency.packageInstance().packageName().equals(possiblePackageName)) {
                    continue;
                }

                ModuleName modName = findModuleName(possiblePackageName, moduleName);
                Module module = resolvedPackageDependency.packageInstance().module(modName);
                if (module == null) {
                    continue;
                }

                for (DocumentId documentId : module.documentIds()) {
                    if (!module.document(documentId).name().equals(filename)) {
                        continue;
                    }
                    Project dependencyProject = resolvedPackageDependency.packageInstance().project();
                    Optional<Path> documentPath = dependencyProject.documentPath(documentId);
                    if (documentPath.isPresent()) {
                        return documentPath.get();
                    }
                }
            }
        }

        throw new ProjectException(String.format("no matching file '%s' found in dependency graph of '%s/%s'",
                filename, orgName, moduleName));
    }

    private static ModuleName findModuleName(PackageName packageName, String moduleNameStr) {
        if (packageName.value().equals(moduleNameStr)) {
            return ModuleName.from(packageName);
        } else {
            String moduleNamePart = moduleNameStr.substring(packageName.value().length() + 1);
            return ModuleName.from(packageName, moduleNamePart);
        }
    }
}
