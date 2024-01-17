/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.formatter.cli;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class for compilation and format execution for formatting CLI tool.
 */
class FormatUtil {
    static final String CMD_NAME = "format";
    private static final PrintStream outStream = System.err;

    /**
     * Execute formatter.
     *
     * @param argList        argument list from the console
     * @param helpFlag       flag to get the help page
     * @param dryRun         run the whole formatting
     * @param sourceRootPath execution path
     */
    static void execute(List<String> argList, boolean helpFlag, String moduleName, String fileName, boolean dryRun,
                        Path sourceRootPath) {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CMD_NAME);
            outStream.println(commandUsageInfo);
            return;
        }

        // Cannot allow both moduleName and fileName options
        if (moduleName != null && fileName != null) {
            throw LauncherUtils.createLauncherException(Messages.getCantAllowBothModuleAndFileOptions());
        }

        if (argList != null && argList.size() > 1) {
            throw LauncherUtils.createLauncherException(Messages.getArgumentError());
        }

        String ballerinaFilePath;

        try {
            // If parameters are available user has given either the module name or the ballerina file path.
            // Else user is in a ballerina project and expecting to format the whole ballerina project or with options
            if (argList != null && !argList.isEmpty() && !argList.get(0).equals(".")) {
                if (FormatUtil.isBalFile(argList.get(0))) {

                    // Cannot allow moduleName and fileName options in single file projects
                    if (moduleName != null || fileName != null) {
                        throw LauncherUtils.createLauncherException(Messages.getCantAllowModuleOrFileOptions());
                    }

                    ballerinaFilePath = argList.get(0);
                    Path filePath = Paths.get(ballerinaFilePath);

                    SingleFileProject project;
                    try {
                        project = SingleFileProject.load(filePath, constructBuildOptions());
                    } catch (ProjectException e) {
                        throw LauncherUtils.createLauncherException(e.getMessage());
                    }

                    String source = Files.readString(project.sourceRoot());
                    // Format and get the generated formatted source code content.
                    String formattedSourceCode = Formatter.format(source);

                    if (areChangesAvailable(source, formattedSourceCode)) {
                        if (!dryRun) {
                            // Write the formatted content back to the file.
                            FormatUtil.writeFile(filePath.toAbsolutePath().toString(), formattedSourceCode);
                            outStream.println(Messages.getModifiedFiles() + System.lineSeparator() + ballerinaFilePath);
                            outStream.println(System.lineSeparator() + Messages.getSuccessMessage());
                        } else {
                            outStream.println(Messages.getFilesToModify() + System.lineSeparator() + ballerinaFilePath);
                        }
                    } else {
                        outStream.println(Messages.getNoChanges());
                    }
                } else if (Paths.get(argList.get(0)).toFile().isFile()) {
                    // If file is a regular file but not a ballerina source file
                    // throw the following exception.
                    throw LauncherUtils.createLauncherException(Messages.getNotABallerinaFile());
                } else {
                    // Project Path
                    Path projectPath = sourceRootPath.resolve(argList.get(0));

                    BuildProject project;

                    try {
                        project = BuildProject.load(projectPath, constructBuildOptions());
                    } catch (ProjectException e) {
                        throw LauncherUtils.createLauncherException(e.getMessage());
                    }

                    List<String> formattedFiles = new ArrayList<>();

                    if (moduleName != null) {
                        // Check whether the module dir exists.
                        if (FormatUtil.isModuleExist(project, moduleName) == null) {
                            throw LauncherUtils.createLauncherException(Messages.getNoModuleFound(moduleName));
                        }
                        // Iterate and format all the ballerina files in the specified module.
                        Module moduleToBeFormatted =
                                project.currentPackage().module(FormatUtil.isModuleExist(project, moduleName));
                        try {
                            formattedFiles.addAll(iterateAndFormat(getDocumentPaths(project,
                                    moduleToBeFormatted.moduleId()), sourceRootPath, dryRun));
                        } catch (IOException | FormatterException e) {
                            throw LauncherUtils.createLauncherException(Messages.getException() + e);
                        }
                        generateChangeReport(formattedFiles, dryRun);
                    } else if (fileName != null) {
                        if (FormatUtil.isBalFile(fileName)) {
                            Path filePath = projectPath.resolve(fileName);
                            // If the file doesn't exist or is a directory.
                            if (!filePath.toFile().exists() || filePath.toFile().isDirectory()) {
                                throw LauncherUtils.createLauncherException(Messages.getNoBallerinaFile(fileName));
                            }

                            String source = Files.readString(filePath);
                            // Format and get the generated formatted source code content.
                            String formattedSourceCode = Formatter.format(source);

                            if (areChangesAvailable(source, formattedSourceCode)) {
                                if (!dryRun) {
                                    // Write the formatted content back to the file.
                                    FormatUtil.writeFile(filePath.toAbsolutePath().toString(), formattedSourceCode);
                                    outStream.println(Messages.getModifiedFiles() + System.lineSeparator() + fileName);
                                    outStream.println(System.lineSeparator() + Messages.getSuccessMessage());
                                } else {
                                    outStream.println(Messages.getFilesToModify() + System.lineSeparator() + fileName);
                                }
                            } else {
                                outStream.println(Messages.getNoChanges());
                            }
                        } else {
                            throw LauncherUtils.createLauncherException(Messages.getNotABallerinaFile());
                        }
                    } else {
                        // Iterate and format all the ballerina packages.
                        project.currentPackage().moduleIds().forEach(moduleId -> {
                            try {
                                formattedFiles.addAll(iterateAndFormat(getDocumentPaths(project, moduleId),
                                        sourceRootPath, dryRun));
                            } catch (IOException | FormatterException e) {
                                throw LauncherUtils.createLauncherException(Messages.getException() + e);
                            }
                        });
                        generateChangeReport(formattedFiles, dryRun);
                    }
                }
            } else {
                BuildProject project;

                try {
                    project = BuildProject.load(sourceRootPath, constructBuildOptions());
                } catch (ProjectException e) {
                    throw LauncherUtils.createLauncherException(e.getMessage());
                }

                List<String> formattedFiles = new ArrayList<>();

                if (moduleName != null) {
                    // Check whether the module dir exists.
                    if (FormatUtil.isModuleExist(project, moduleName) == null) {
                        throw LauncherUtils.createLauncherException(Messages.getNoModuleFound(moduleName));
                    }
                    // Iterate and format all the ballerina files in the specified module.
                    Module moduleToBeFormatted =
                            project.currentPackage().module(FormatUtil.isModuleExist(project, moduleName));
                    try {
                        formattedFiles.addAll(iterateAndFormat(getDocumentPaths(project,
                                moduleToBeFormatted.moduleId()), sourceRootPath, dryRun));
                    } catch (IOException | FormatterException e) {
                        throw LauncherUtils.createLauncherException(Messages.getException() + e);
                    }
                    generateChangeReport(formattedFiles, dryRun);
                } else if (fileName != null) {
                    if (FormatUtil.isBalFile(fileName)) {
                        Path filePath = sourceRootPath.resolve(fileName);
                        // If the file doesn't exist or is a directory.
                        if (!filePath.toFile().exists() || filePath.toFile().isDirectory()) {
                            throw LauncherUtils.createLauncherException(Messages.getNoBallerinaFile(fileName));
                        }

                        String source = Files.readString(filePath);
                        // Format and get the generated formatted source code content.
                        String formattedSourceCode = Formatter.format(source);

                        if (areChangesAvailable(source, formattedSourceCode)) {
                            if (!dryRun) {
                                // Write the formatted content back to the file.
                                FormatUtil.writeFile(filePath.toAbsolutePath().toString(), formattedSourceCode);
                                outStream.println(Messages.getModifiedFiles() + System.lineSeparator() + fileName);
                                outStream.println(System.lineSeparator() + Messages.getSuccessMessage());
                            } else {
                                outStream.println(Messages.getFilesToModify() + System.lineSeparator() + fileName);
                            }
                        } else {
                            outStream.println(Messages.getNoChanges());
                        }
                    } else {
                        throw LauncherUtils.createLauncherException(Messages.getNotABallerinaFile());
                    }
                } else {
                    // Iterate and format all the ballerina packages.
                    project.currentPackage().moduleIds().forEach(moduleId -> {
                        try {
                            formattedFiles.addAll(iterateAndFormat(getDocumentPaths(project, moduleId),
                                    sourceRootPath, dryRun));
                        } catch (IOException | FormatterException e) {
                            throw LauncherUtils.createLauncherException(Messages.getException() + e);
                        }
                    });
                    generateChangeReport(formattedFiles, dryRun);
                }
            }
        } catch (IOException | NullPointerException | FormatterException e) {
            throw LauncherUtils.createLauncherException(Messages.getException() + e);
        }
    }

    private static List<Path> getDocumentPaths(BuildProject project, ModuleId moduleId) {
        List<Path> documentPaths = new ArrayList<>();
        Module module = project.currentPackage().module(moduleId);
        List<DocumentId> documentIds = new ArrayList<>();

        documentIds.addAll(module.documentIds());
        documentIds.addAll(module.testDocumentIds());
        documentIds.forEach(documentId -> {
            if (project.documentPath(documentId).isPresent()) {
                documentPaths.add(project.documentPath(documentId).get());
            }
        });
        return documentPaths;
    }

    private static void generateChangeReport(List<String> formattedFiles, boolean dryRun) {
        if (!formattedFiles.isEmpty()) {
            StringBuilder fileList = new StringBuilder();
            if (dryRun) {
                fileList.append(Messages.getFilesToModify()).append(System.lineSeparator());
            } else {
                fileList.append(Messages.getModifiedFiles()).append(System.lineSeparator());
            }
            for (String file : formattedFiles) {
                fileList.append(file).append(System.lineSeparator());
            }
            outStream.println(fileList.toString());
            if (!dryRun) {
                outStream.println(Messages.getSuccessMessage());
            }
        } else {
            outStream.println(Messages.getNoChanges());
        }
    }

    private static void formatAndWrite(Path documentPath, Path sourceRootPath,
                               List<String> formattedFiles, boolean dryRun) throws IOException, FormatterException {
        String fileName = Paths.get(sourceRootPath.toString()).resolve("modules").resolve(documentPath).toString();

        String originalSource = Files.readString(Paths.get(fileName));
        // Format and get the formatted source.
        String formattedSource = Formatter.format(originalSource);

        if (areChangesAvailable(originalSource, formattedSource)) {
            if (!dryRun) {
                // Write formatted content to the file.
                FormatUtil.writeFile(fileName, formattedSource);
            }
            formattedFiles.add(fileName);
        }
    }

    private static List<String> iterateAndFormat(List<Path> documentPaths, Path sourceRootPath, boolean dryRun)
            throws IOException, FormatterException {
        List<String> formattedFiles = new ArrayList<>();

        // Iterate compilation units and format.
        for (Path path : documentPaths) {
            formatAndWrite(path, sourceRootPath, formattedFiles, dryRun);
        }

        return formattedFiles;
    }

    private static BuildOptions constructBuildOptions() {
        return BuildOptions.builder()
                .setCodeCoverage(false)
                .setExperimental(true)
                .setOffline(false)
                .setSkipTests(false)
                .setTestReport(false)
                .setObservabilityIncluded(false)
                .build();
    }

    /**
     * Check whether the given module name exists.
     *
     * @param project project in which module exist
     * @param moduleNamePart module name part
     * @return {@link Boolean} true or false
     */
    private static ModuleId isModuleExist(BuildProject project, String moduleNamePart) {
        ModuleName moduleName = ModuleName.from(project.currentPackage().packageName(), moduleNamePart);
        for (Module module : project.currentPackage().modules()) {
            if (module.moduleName().equals(moduleName)) {
                return module.moduleId();
            }
        }
        return null;
    }

    /**
     * Write content to a file.
     *
     * @param filePath - path of the file to add the content
     * @param content  - content to be added to the file
     * @throws IOException - throws and IO exception
     */
    private static void writeFile(String filePath, String content) throws IOException {
        OutputStreamWriter fileWriter = null;
        FileOutputStream fileStream = null;
        try {
            File newFile = new File(filePath);
            fileStream = new FileOutputStream(newFile);
            fileWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            fileWriter.write(content);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
            if (fileStream != null) {
                fileStream.close();
            }
        }
    }

    private static boolean areChangesAvailable(String originalSource, String formattedSource) {
        return !originalSource.equals(formattedSource);
    }

    /**
     * Check whether the given file is a ballerina file.
     *
     * @param fileName file name to be check whether a ballerina file
     * @return {@link Boolean} true or false
     */
    private static boolean isBalFile(String fileName) {
        return fileName.endsWith(".bal");
    }


    /**
     * Empty print stream extending the print stream.
     */
    static class EmptyPrintStream extends PrintStream {
        EmptyPrintStream() throws UnsupportedEncodingException {
            super(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }, true, "UTF-8");
        }
    }
}
