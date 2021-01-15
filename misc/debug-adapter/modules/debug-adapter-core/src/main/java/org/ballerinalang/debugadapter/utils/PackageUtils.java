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
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.debugadapter.DebugContext;
import org.ballerinalang.debugadapter.DebugSourceType;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
     * Some additional processing is required to rectify the source path, as the source name will be the
     * relative path instead of just the file name, for the ballerina module sources.
     */
    public static Path getRectifiedSourcePath(Location location, Project project, String projectRoot)
            throws AbsentInformationException {
        if (project instanceof SingleFileProject) {
            DocumentId docId = project.currentPackage().getDefaultModule().documentIds().iterator().next();
            Document document = project.currentPackage().getDefaultModule().document(docId);
            if (!document.name().equals(location.sourcePath()) || !document.name().equals(location.sourceName())) {
                return null;
            }
            return Paths.get(projectRoot);
        } else if (project instanceof BuildProject) {
            String orgName = getOrgName(project);
            String defaultModuleName = getDefaultModuleName(project);
            String sourcePath = location.sourcePath();
            String sourceName = location.sourceName();
            String[] srcPaths = getNameParts(sourcePath);

            if (!srcPaths[0].equals(orgName)) {
                return null;
            }

            String modulePart = decodeIdentifier(srcPaths[1]);
            modulePart = modulePart.replaceFirst(defaultModuleName, "");
            if (modulePart.startsWith(".")) {
                modulePart = modulePart.replaceFirst("\\.", "");
            }

            if (modulePart.isBlank()) {
                // default module.
                return Paths.get(projectRoot, sourceName);
            } else {
                // other modules
                return Paths.get(projectRoot, MODULE_DIR_NAME, modulePart, sourceName);
            }
        }
        return null;
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

    /**
     * Returns all the module class names which belong to the current module that is being debugged.
     *
     * @param context Suspended context
     * @return All the module class names which belong to the current module that is being debugged.
     */
    public static List<String> getModuleClassNames(SuspendedContext context) throws EvaluationException {
        try {
            // Todo - use balo reader to derive module class names by accessing module balo files.
            return new ArrayList<>();
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to retrieve source file names of the current module."));
        }
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
    public static String getQualifiedClassName(DebugContext context, ReferenceType referenceType) {
        try {
            List<String> paths = referenceType.sourcePaths(null);
            List<String> names = referenceType.sourceNames(null);
            if (paths.isEmpty() || names.isEmpty()) {
                return referenceType.name();
            }
            String path = paths.get(0);
            String name = names.get(0);
            String[] nameParts = getNameParts(name);
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

    public static String[] getNameParts(String path) {
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
}
