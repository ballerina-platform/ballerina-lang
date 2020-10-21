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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

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
import java.util.regex.Pattern;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * Util class for compilation and format execution for formatting CLI tool.
 */
class FormatUtil {
    static final String CMD_NAME = "format";
    private static final PrintStream outStream = System.err;
    private static EmptyPrintStream emptyPrintStream;

    /**
     * Execute formatter.
     *
     * @param argList        argument list from the console
     * @param helpFlag       flag to get the help page
     * @param dryRun         run the whole formatting
     * @param sourceRootPath execution path
     */
    static void execute(List<String> argList, boolean helpFlag, boolean dryRun, Path sourceRootPath) {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CMD_NAME);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            throw LauncherUtils.createLauncherException(Messages.getArgumentError());
        }

        String moduleName;
        String ballerinaFilePath;

        try {
            // If parameters are available user has given either the module name or the ballerina file path.
            // Else user is in a ballerina project and expecting to format the whole ballerina project
            if (argList != null && !argList.isEmpty()) {
                if (FormatUtil.isBalFile(argList.get(0))) {
                    ballerinaFilePath = argList.get(0);
                    Path filePath = Paths.get(ballerinaFilePath);

                    // If the file doesn't exist or is a directory.
                    if (!filePath.toFile().exists() || filePath.toFile().isDirectory()) {
                        throw LauncherUtils.createLauncherException(Messages.getNoBallerinaFile(ballerinaFilePath));
                    }

                    String source = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
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
                    moduleName = argList.get(0);

                    // Check whether the module dir exists.
                    if (!FormatUtil.isModuleExist(moduleName, sourceRootPath)) {
                        // If module directory doesn't exist and contains a "."
                        // throw an exception to say file or module doesn't exist.
                        // Else throw a exception to say module doesn't exist.
                        if (moduleName.contains(".")) {
                            throw LauncherUtils.createLauncherException(Messages
                                    .getNoBallerinaModuleOrFile(moduleName));
                        } else {
                            throw LauncherUtils.createLauncherException(Messages.getNoModuleFound(moduleName));
                        }
                    }

                    // Check whether the given directory is not in a ballerina project.
                    if (!FormatUtil.isBallerinaProject(sourceRootPath)) {
                        throw LauncherUtils.createLauncherException(Messages.getNotBallerinaProject());
                    }
                    BLangPackage bLangPackage = FormatUtil
                            .compileModule(sourceRootPath, getModuleName(moduleName));

                    // Iterate and format the ballerina package.
                    List<String> formattedFiles = iterateAndFormat(bLangPackage, sourceRootPath, dryRun);
                    generateChangeReport(formattedFiles, dryRun);
                }
            } else {
                List<BLangPackage> packages = FormatUtil.compileProject(sourceRootPath);
                List<String> formattedFiles = new ArrayList<>();
                // Iterate and format all the ballerina packages.
                for (BLangPackage bLangPackage : packages) {
                    formattedFiles.addAll(iterateAndFormat(bLangPackage, sourceRootPath, dryRun));
                }

                generateChangeReport(formattedFiles, dryRun);
            }
        } catch (IOException | NullPointerException e) {
            throw LauncherUtils.createLauncherException(Messages.getException() + e);
        }
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

    private static String getModuleName(String moduleName) {
        String pattern = Pattern.quote(File.separator);
        String[] splitedTokens = moduleName.split(pattern);
        return splitedTokens[splitedTokens.length - 1];
    }

    /**
     * Compile whole ballerina project.
     *
     * @param sourceRoot source root
     * @return {@link List<BLangPackage>} list of BLangPackages
     */
    private static List<BLangPackage> compileProject(Path sourceRoot) throws UnsupportedEncodingException {
        emptyPrintStream = new EmptyPrintStream();
        CompilerContext context = getCompilerContext(sourceRoot);
        Compiler compiler = Compiler.getInstance(context);
        // Set an EmptyPrintStream to hide unnecessary outputs from compiler.
        compiler.setOutStream(emptyPrintStream);
        return compiler.compilePackages(false);
    }

    /**
     * Compile only a ballerina module.
     *
     * @param sourceRoot source root
     * @param moduleName name of the module to be compiled
     * @return {@link BLangPackage} ballerina package
     */
    private static BLangPackage compileModule(Path sourceRoot, String moduleName) throws UnsupportedEncodingException {
        emptyPrintStream = new EmptyPrintStream();
        CompilerContext context = getCompilerContext(sourceRoot);
        Compiler compiler = Compiler.getInstance(context);
        // Set an EmptyPrintStream to hide unnecessary outputs from compiler.
        compiler.setOutStream(emptyPrintStream);
        return compiler.compile(moduleName);
    }

    private static void formatAndWrite(BLangCompilationUnit compilationUnit, Path sourceRootPath,
                                       List<String> formattedFiles, boolean dryRun) throws IOException {
        String fileName = Paths.get(sourceRootPath.toString()).resolve("src")
                .resolve(compilationUnit.getPosition().getSource().getPackageName())
                .resolve(compilationUnit.getPosition().getSource().getCompilationUnitName()).toString();

        String originalSource = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
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

    private static List<String> iterateAndFormat(BLangPackage bLangPackage, Path sourceRootPath, boolean dryRun)
            throws IOException {
        List<String> formattedFiles = new ArrayList<>();

        // Iterate compilation units and format.
        for (BLangCompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            formatAndWrite(compilationUnit, sourceRootPath, formattedFiles, dryRun);
        }

        // Iterate testable packages and format.
        for (BLangTestablePackage testablePackage : bLangPackage.getTestablePkgs()) {
            for (BLangCompilationUnit compilationUnit : testablePackage.getCompilationUnits()) {
                formatAndWrite(compilationUnit, sourceRootPath, formattedFiles, dryRun);
            }
        }

        return formattedFiles;
    }

    /**
     * Get prepared compiler context.
     *
     * @param sourceRootPath ballerina compilable source root path
     * @return {@link CompilerContext} compiler context
     */
    private static CompilerContext getCompilerContext(Path sourceRootPath) {
        CompilerPhase compilerPhase = CompilerPhase.DEFINE;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(false));
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(SKIP_TESTS, Boolean.toString(false));
        options.put(TEST_ENABLED, "true");
        options.put(LOCK_ENABLED, Boolean.toString(false));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(true));
        options.put(PRESERVE_WHITESPACE, Boolean.toString(true));

        return context;
    }

    /**
     * Check whether the given module name exists.
     *
     * @param module      module name
     * @param projectRoot path of the ballerina project root
     * @return {@link Boolean} true or false
     */
    private static boolean isModuleExist(String module, Path projectRoot) {
        Path modulePath;
        if (module.startsWith("src/")) {
            modulePath = projectRoot.resolve(module);
        } else {
            modulePath = projectRoot.resolve("src").resolve(module);
        }

        return modulePath.toFile().isDirectory();
    }

    /**
     * Check whether the given path is a source root of a ballerina project.
     *
     * @param path - path where the command is executed from
     * @return {@link boolean} true or false
     */
    private static boolean isBallerinaProject(Path path) {
        Path cachePath = path.resolve("Ballerina.toml");
        return cachePath.toFile().exists();
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
