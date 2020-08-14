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
package org.ballerinalang.openapi.validator;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.tool.BLauncherCmd;
import org.ballerinalang.tool.BLauncherException;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProgramDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class OpenApiValidatorUtil {
    static final String CMD_NAME = "build";
    private static final PrintStream outStream = System.err;
    private static EmptyPrintStream emptyPrintStream;

    /**
     * Execute formatter.
     * @param argList        argument list from the console
     * @param helpFlag       flag to get the help page
     * @param sourceRootPath execution path
     */
    public static void execute(List<String> argList, boolean helpFlag, Path sourceRootPath) throws BLauncherException {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(CMD_NAME);
            outStream.println(commandUsageInfo);
            return;
        }
        String moduleName;
        String ballerinaFilePath;

        try {
            // If parameters are available user has given either the module name or the ballerina file path.
            // Else user is in a ballerina project
            if (argList != null && argList.size() > 0) {
                // If the given param is a ballerina file path, this will compile the file.
                // Else the given param is a module name, this will compile the module.
                if (OpenApiValidatorUtil.isBalFile(argList.get(0))) {
                    ballerinaFilePath = argList.get(0);
                    Path filePath = Paths.get(ballerinaFilePath);
                    Path programDir = filePath.toAbsolutePath().getParent();
                    String fileName = filePath.toAbsolutePath().getFileName().toString();

                    // Compile ballerina file.
                    BLangPackage bLangPackage = compileFile(programDir, fileName);

                    // If there are compilation errors do not continue the process.
                    if (bLangPackage.diagCollector.hasErrors()) {
                        return;
                    }

                } else {
                    moduleName = argList.get(0);

                    BLangPackage bLangPackage = OpenApiValidatorUtil
                            .compileModule(sourceRootPath, getModuleName(moduleName));

                    // If there are no compilation errors do not continue the process.
                    if (bLangPackage.diagCollector.hasErrors()) {
                        return;
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
//            throw LauncherUtils.createLauncherException(Messages.getException());
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
     * Compile only a ballerina module.
     *
     * @param sourceRoot source root
     * @param moduleName name of the module to be compiled
     * @return {@link BLangPackage} ballerina package
     */
    public static BLangPackage compileModule(Path sourceRoot, String moduleName) throws UnsupportedEncodingException {
        emptyPrintStream = new EmptyPrintStream();
        CompilerContext context = getCompilerContext(sourceRoot);
        Compiler compiler = Compiler.getInstance(context);
        // Set an EmptyPrintStream to hide unnecessary outputs from compiler.
        compiler.setOutStream(emptyPrintStream);
        return compiler.compile(moduleName);
    }

    /**
     * Compile a ballerina file.
     *
     * @param sourceRoot  source root of the file
     * @param packageName package name of the file
     * @return {@link BLangPackage} ballerina package
     */
    public static BLangPackage compileFile(Path sourceRoot, String packageName) throws UnsupportedEncodingException {
        emptyPrintStream = new EmptyPrintStream();
        CompilerContext context = getCompilerContext(sourceRoot);
        // Set the SourceDirectory to process this compilation as a program directory.
        context.put(SourceDirectory.class, new FileSystemProgramDirectory(sourceRoot));
        Compiler compiler = Compiler.getInstance(context);
        // Set an EmptyPrintStream to hide unnecessary outputs from compiler.
        compiler.setOutStream(emptyPrintStream);
        return compiler.compile(packageName);
    }

    /**
     * Get prepared compiler context.
     *
     * @param sourceRootPath ballerina compilable source root path
     * @return {@link CompilerContext} compiler context
     */
    public static CompilerContext getCompilerContext(Path sourceRootPath) {
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

    public static String getModuleName(String moduleName) {
        String pattern = Pattern.quote(File.separator);
        String[] splitedTokens = moduleName.split(pattern);
        return splitedTokens[splitedTokens.length - 1];
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


