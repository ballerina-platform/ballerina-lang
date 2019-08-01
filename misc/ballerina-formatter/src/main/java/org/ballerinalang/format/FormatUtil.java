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
package org.ballerinalang.format;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.format.FormattingVisitorEntry;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProgramDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SIDDHI_RUNTIME_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil.generateJSON;

/**
 * Util class for compilation and format execution for formatting CLI tool.
 */
public class FormatUtil {
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
            if (argList != null && argList.size() > 0) {
                // If the given param is a ballerina file path, this will format the file.
                // Else the given param is a module name, this will format the module.
                if (FormatUtil.isBalFile(argList.get(0))) {
                    ballerinaFilePath = argList.get(0);
                    Path filePath = Paths.get(ballerinaFilePath);
                    Path programDir = filePath.toAbsolutePath().getParent();
                    String fileName = filePath.toAbsolutePath().getFileName().toString();

                    // If the file doesn't exist or is a directory.
                    if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                        throw LauncherUtils.createLauncherException(Messages.getNoBallerinaFile(ballerinaFilePath));
                    }

                    // Compile ballerina file.
                    BLangPackage bLangPackage = compileFile(programDir, fileName);

                    // If there are compilation errors do not continue the process.
                    if (bLangPackage.diagCollector.hasErrors()) {
                        return;
                    }

                    BLangCompilationUnit compilationUnit = bLangPackage.getCompilationUnits().get(0);

                    // Format and get the generated formatted source code content.
                    String formattedSourceCode = format(compilationUnit);
                    if (doChangesAvailable(compilationUnit, formattedSourceCode)) {
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
                } else if (Files.isRegularFile(Paths.get(argList.get(0)))) {
                    // If file is a regular file but not a ballerina source file
                    // throw the following exception.
                    throw LauncherUtils.createLauncherException(Messages.getNotABallerinaFile());
                } else {
                    moduleName = argList.get(0);

                    // Check whether the module dir exists.
                    if (!FormatUtil.isModuleExist(moduleName, sourceRootPath)) {
                        // If module directory doesn't exist and contains a "."
                        // throw a exception to say file or module doesn't exist.
                        // Else throw a exception to say module doesn't exist.
                        if (moduleName.contains(".")) {
                            throw LauncherUtils.createLauncherException(Messages
                                    .getNoBallerinaModuleOrFile(moduleName));
                        } else {
                            throw LauncherUtils.createLauncherException(Messages.getNoModuleFound(moduleName));
                        }
                    }

                    // Check whether the given directory is not in a ballerina project.
                    if (FormatUtil.notABallerinaProject(sourceRootPath)) {
                        throw LauncherUtils.createLauncherException(Messages.getNotBallerinaProject());
                    }
                    BLangPackage bLangPackage = FormatUtil
                            .compileModule(sourceRootPath, getModuleName(moduleName));

                    // If there are no compilation errors do not continue the process.
                    if (bLangPackage.diagCollector.hasErrors()) {
                        return;
                    }

                    // Iterate and format the ballerina package.
                    List<String> formattedFiles = iterateAndFormat(bLangPackage, sourceRootPath, dryRun);
                    generateChangeReport(formattedFiles, dryRun);
                }
            } else {
                // Check whether the given source root is not
                if (FormatUtil.notABallerinaProject(sourceRootPath)) {
                    throw LauncherUtils.createLauncherException(Messages.getNotBallerinaProject());
                }

                List<BLangPackage> packages = FormatUtil.compileProject(sourceRootPath);
                boolean hasCompilationErrors = false;
                for (BLangPackage bLangPackage : packages) {
                    if (bLangPackage.diagCollector.hasErrors()) {
                        hasCompilationErrors = true;
                        break;
                    }
                }
                if (hasCompilationErrors) {
                    return;
                }

                List<String> formattedFiles = new ArrayList<>();
                // Iterate and format all the ballerina packages.
                for (BLangPackage bLangPackage : packages) {
                    formattedFiles.addAll(iterateAndFormat(bLangPackage, sourceRootPath, dryRun));
                }

                generateChangeReport(formattedFiles, dryRun);
            }
        } catch (IOException | JSONGenerationException | NullPointerException e) {
            throw LauncherUtils.createLauncherException(Messages.getException());
        }
    }

    /**
     * Check whether the given path isn't a source root of a ballerina project.
     *
     * @param path - path where the command is executed from
     * @return {@link boolean} true or false
     */
    private static boolean notABallerinaProject(Path path) {
        Path cachePath = path.resolve("Ballerina.toml");
        return !Files.exists(cachePath);
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
        try {
            File newFile = new File(filePath);
            FileOutputStream fileStream = new FileOutputStream(newFile);
            fileWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
            fileWriter.write(content);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
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

        return Files.isDirectory(modulePath);
    }

    /**
     * Compile whole ballerina project.
     *
     * @param sourceRoot source root
     * @return {@link List<BLangPackage>} list of BLangPackages
     */
    private static List<BLangPackage> compileProject(Path sourceRoot) {
        CompilerContext context = getCompilerContext(sourceRoot);
        Compiler compiler = Compiler.getInstance(context);
        return compiler.compilePackages(false);
    }

    /**
     * Compile only a ballerina module.
     *
     * @param sourceRoot source root
     * @param moduleName name of the module to be compiled
     * @return {@link BLangPackage} ballerina package
     */
    private static BLangPackage compileModule(Path sourceRoot, String moduleName) {
        CompilerContext context = getCompilerContext(sourceRoot);
        Compiler compiler = Compiler.getInstance(context);
        return compiler.compile(moduleName);
    }

    /**
     * Compile a ballerina file.
     *
     * @param sourceRoot  source root of the file
     * @param packageName package name of the file
     * @return {@link BLangPackage} ballerina package
     */
    private static BLangPackage compileFile(Path sourceRoot, String packageName) {
        CompilerContext context = getCompilerContext(sourceRoot);
        // Set the SourceDirectory to process this compilation as a program directory.
        context.put(SourceDirectory.class, new FileSystemProgramDirectory(sourceRoot));
        Compiler compiler = Compiler.getInstance(context);
        return compiler.compile(packageName);
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
        options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(true));
        options.put(PRESERVE_WHITESPACE, Boolean.toString(true));

        return context;
    }

    private static String format(BLangCompilationUnit compilationUnit) throws JSONGenerationException {
        JsonElement modelElement = generateJSON(compilationUnit, new HashMap<>(), new HashMap<>());
        JsonObject model = modelElement.getAsJsonObject();
        FormattingSourceGen.build(model, "CompilationUnit");

        // Format the given AST.
        FormattingVisitorEntry formattingUtil = new FormattingVisitorEntry();
        formattingUtil.accept(model);

        return FormattingSourceGen.getSourceOf(model);
    }

    private static boolean doChangesAvailable(BLangCompilationUnit compilationUnit, String formattedSource)
            throws JSONGenerationException {
        JsonElement modelElement = generateJSON(compilationUnit, new HashMap<>(), new HashMap<>());
        JsonObject model = modelElement.getAsJsonObject();
        FormattingSourceGen.build(model, "CompilationUnit");
        String originalSource = FormattingSourceGen.getSourceOf(model);
        return !originalSource.equals(formattedSource);
    }

    private static List<String> iterateAndFormat(BLangPackage bLangPackage, Path sourceRootPath, boolean dryRun)
            throws IOException, JSONGenerationException {
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

    private static void formatAndWrite(BLangCompilationUnit compilationUnit, Path sourceRootPath,
                                       List<String> formattedFiles, boolean dryRun)
            throws JSONGenerationException, IOException {
        String fileName = sourceRootPath.toString() + File.separator
                + "src"
                + File.separator
                + compilationUnit.getPosition().getSource().getPackageName()
                + File.separator
                + compilationUnit.getPosition().getSource().getCompilationUnitName();

        // Format and get the formatted source.
        String formattedSource = format(compilationUnit);

        if (doChangesAvailable(compilationUnit, formattedSource)) {
            if (!dryRun) {
                // Write formatted content to the file.
                FormatUtil.writeFile(fileName, formattedSource);
            }
            formattedFiles.add(fileName);
        }
    }

    private static void generateChangeReport(List<String> formattedFiles, boolean dryRun) {
        if (formattedFiles.size() > 0) {
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
}
