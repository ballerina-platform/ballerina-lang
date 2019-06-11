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
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.format.FormattingVisitorEntry;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.wso2.ballerinalang.compiler.Compiler;
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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

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
     * @param overWriteFile  file overwrite confirmation
     * @param helpFlag       flag to get the help page
     * @param sourceRootPath execution path
     */
    static void execute(List<String> argList, boolean overWriteFile, boolean helpFlag, Path sourceRootPath) {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CMD_NAME);
            outStream.println(commandUsageInfo);
            return;
        }

        if (argList != null && argList.size() > 1) {
            throw LauncherUtils.createUsageExceptionWithHelp(Messages.getArgumentError());
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
                    if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                        String sourceRoot = LSCompilerUtil.getSourceRoot(filePath);
                        String packageName = LSCompilerUtil.getPackageNameForGivenFile(sourceRoot, filePath.toString());
                        if ("".equals(packageName)) {
                            Path path = filePath.getFileName();
                            if (path != null) {
                                packageName = path.toString();
                            }
                        }
                        BLangPackage bLangPackage = compileFile(Paths.get(sourceRoot), packageName);

                        // If there is no compilation error continue the process.
                        if (!bLangPackage.diagCollector.hasErrors()) {
                            BLangCompilationUnit compilationUnit = bLangPackage.getCompilationUnits().get(0);
                            String formattedSourceCode = format(compilationUnit);
                            if (overWriteFile) {
                                FormatUtil.writeFile(ballerinaFilePath, formattedSourceCode);
                                outStream.println(Messages.getSuccessMessage(ballerinaFilePath));
                            } else {
                                outStream.println(Messages.getConfirmationMessage());
                                Scanner userInput = new Scanner(System.in, "UTF-8");
                                boolean wait = true;
                                while (wait) {
                                    String input = userInput.nextLine();
                                    if (input != null) {
                                        if (input.equals("y")) {
                                            FormatUtil.writeFile(ballerinaFilePath, formattedSourceCode);
                                            outStream.println(Messages.getSuccessMessage(ballerinaFilePath));
                                            wait = false;
                                        } else {
                                            outStream.println(Messages.getCancelMessage());
                                            wait = false;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        throw LauncherUtils.createLauncherException(Messages.getNoBallerinaFile(ballerinaFilePath));
                    }
                } else if (Files.isRegularFile(Paths.get(argList.get(0)))) {
                    // If file is a regular file but not a ballerina source file
                    // throw the following exception.
                    throw LauncherUtils.createLauncherException(Messages.getNotABallerinaFile());
                } else {
                    moduleName = argList.get(0);

                    // Check whether the module dir exists.
                    if (FormatUtil.isModuleExist(moduleName, sourceRootPath)) {
                        // Check whether the given directory is not in a ballerina project.
                        if (FormatUtil.notABallerinaProject(sourceRootPath)) {
                            throw LauncherUtils.createLauncherException(Messages.getNotBallerinaProject());
                        }
                        BLangPackage bLangPackage = FormatUtil
                                .compileModule(sourceRootPath, moduleName);

                        // If there is no compilation error continue the process.
                        if (!bLangPackage.diagCollector.hasErrors()) {
                            if (overWriteFile) {
                                iterateAndFormat(bLangPackage, sourceRootPath);
                            } else {
                                outStream.println(Messages.getConfirmationMessage());
                                Scanner userInput = new Scanner(System.in, "UTF-8");
                                boolean wait = true;
                                while (wait) {
                                    String input = userInput.nextLine();
                                    if (input != null) {
                                        if (input.equals("y")) {
                                            iterateAndFormat(bLangPackage, sourceRootPath);
                                            wait = false;
                                        } else {
                                            outStream.println(Messages.getCancelMessage());
                                            wait = false;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
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
                }
            } else {
                // Check whether the given source root is not
                if (FormatUtil.notABallerinaProject(sourceRootPath)) {
                    throw LauncherUtils.createLauncherException(Messages.getNotBallerinaProject());
                }

                List<BLangPackage> packages = FormatUtil.compileProject(sourceRootPath);
                boolean noCompilationError = true;
                for (BLangPackage bLangPackage : packages) {
                    if (bLangPackage.diagCollector.hasErrors()) {
                        noCompilationError = false;
                        break;
                    }
                }
                if (noCompilationError) {
                    if (overWriteFile) {
                        for (BLangPackage bLangPackage : packages) {
                            iterateAndFormat(bLangPackage, sourceRootPath);
                        }
                    } else {
                        outStream.println(Messages.getConfirmationMessage());
                        Scanner userInput = new Scanner(System.in, "UTF-8");
                        boolean wait = true;
                        while (wait) {
                            String input = userInput.nextLine();
                            if (input != null) {
                                if (input.equals("y")) {
                                    for (BLangPackage bLangPackage : packages) {
                                        iterateAndFormat(bLangPackage, sourceRootPath);
                                    }
                                    wait = false;
                                } else {
                                    outStream.println(Messages.getCancelMessage());
                                    wait = false;
                                }
                            }
                        }
                    }
                }
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
        Path cachePath = path.resolve(".ballerina");
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
        Path modulePath = projectRoot.resolve(module);
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

        // Format the given ast.
        FormattingVisitorEntry formattingUtil = new FormattingVisitorEntry();
        formattingUtil.accept(model);

        return FormattingSourceGen.getSourceOf(model);
    }

    private static void iterateAndFormat(BLangPackage bLangPackage, Path sourceRootPath)
            throws IOException, JSONGenerationException {
        // Iterate compilation units and format
        for (BLangCompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
            formatAndWrite(compilationUnit, sourceRootPath);
        }

        // Iterate testable packages and format.
        for (BLangTestablePackage testablePackage : bLangPackage.getTestablePkgs()) {
            for (BLangCompilationUnit compilationUnit : testablePackage.getCompilationUnits()) {
                formatAndWrite(compilationUnit, sourceRootPath);
            }
        }
    }

    private static void formatAndWrite(BLangCompilationUnit compilationUnit, Path sourceRootPath)
            throws JSONGenerationException, IOException {
        String fileName = sourceRootPath.toString() + File.separator
                + compilationUnit.getPosition().getSource().getPackageName()
                + File.separator
                + compilationUnit.getPosition().getSource().getCompilationUnitName();

        // Format and get the formatted source.
        String formattedSource = format(compilationUnit);

        // Write formatted content to the file.
        FormatUtil.writeFile(fileName, formattedSource);
        outStream.println(Messages.getSuccessMessage(fileName));
    }
}
