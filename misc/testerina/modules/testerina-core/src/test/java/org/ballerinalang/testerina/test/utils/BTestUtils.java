/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.test.utils;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility methods for unit tests.
 *
 * @since 0.94
 */
public class BTestUtils {

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source package/file
     * @return Semantic errors
     */
    public static org.ballerinalang.launcher.util.CompileResult compile(String sourceRoot, String sourceFilePath) {
        return BCompileUtil.compile(sourceRoot, sourceFilePath, CompilerPhase.CODE_GEN);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot  root path of the source packages
     * @param packageName name of the package to compile
     * @return Semantic errors
     */
//    public static CompileResult compile(String sourceRoot, String packageName) {
//        try {
//            String effectiveSource;
//            Path rootPath = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource()
//                                              .getLocation().toURI().getPath().concat(sourceRoot));
//            if (Files.isDirectory(Paths.get(packageName))) {
//                String[] pkgParts = packageName.split("\\/");
//                List<Name> pkgNameComps = Arrays.stream(pkgParts)
//                        .map(part -> {
//                            if (part.equals("")) {
//                                return Names.EMPTY;
//                            } else if (part.equals("_")) {
//                                return Names.EMPTY;
//                            }
//                            return new Name(part);
//                        })
//                        .collect(Collectors.toList());
//                PackageID pkgId = new PackageID(pkgNameComps, Names.DEFAULT_VERSION);
//                effectiveSource = pkgId.getName().getValue();
//            } else {
//                effectiveSource = packageName;
//            }
//            return compile(rootPath.toString(), effectiveSource, CompilerPhase.CODE_GEN);
//        } catch (URISyntaxException e) {
//            throw new IllegalArgumentException("error while running test: " + e.getMessage());
//        }
//    }

//    /**
//     * Compile and return the semantic errors.
//     *
//     * @param sourceFilePath Path to source package/file
//     * @param compilerPhase  Compiler phase
//     * @return Semantic errors
//     */
//    public static CompileResult compile(String sourceFilePath, CompilerPhase compilerPhase) {
//        Path sourcePath = Paths.get(sourceFilePath);
//        String packageName = sourcePath.getFileName().toString();
//        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
//        return compile(sourceRoot.toString(), packageName, compilerPhase);
//    }
//
//    /**
//     * Compile and return the semantic errors.
//     *
//     * @param sourceRoot    root path of the source packages
//     * @param packageName   name of the package to compile
//     * @param compilerPhase Compiler phase
//     * @return Semantic errors
//     */
//    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase) {
//        CompilerContext context = new CompilerContext();
//        CompilerOptions options = CompilerOptions.getInstance(context);
//        options.put(SOURCE_ROOT, resourceDir.resolve(sourceRoot).toString());
//        options.put(COMPILER_PHASE, compilerPhase.toString());
//        options.put(PRESERVE_WHITESPACE, "false");
//
//        CompileResult comResult = new CompileResult();
//
//        // catch errors
//        DiagnosticListener listener = comResult::addDiagnostic;
//        context.put(DiagnosticListener.class, listener);
//
//        // compile
//        Compiler compiler = Compiler.getInstance(context);
//        compiler.compile(packageName);
//        org.wso2.ballerinalang.programfile.ProgramFile programFile = compiler.getCompiledProgram();
//        if (programFile != null) {
//            comResult.setProgFile(LauncherUtils.getExecutableProgram(programFile));
//        }
//
//        return comResult;
//    }
//
//    /**
//     * Compile and return the compiled package node.
//     *
//     * @param sourceFilePath Path to source package/file
//     * @return compiled package node
//     */
//    public static BLangPackage compileAndGetPackage(String sourceFilePath) {
//        Path sourcePath = Paths.get(sourceFilePath);
//        String packageName = sourcePath.getFileName().toString();
//        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
//        CompilerContext context = new CompilerContext();
//        CompilerOptions options = CompilerOptions.getInstance(context);
//        options.put(SOURCE_ROOT, resourceDir.resolve(sourceRoot).toString());
//        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
//        options.put(PRESERVE_WHITESPACE, "false");
//
//        CompileResult comResult = new CompileResult();
//
//        // catch errors
//        DiagnosticListener listener = comResult::addDiagnostic;
//        context.put(DiagnosticListener.class, listener);
//
//        // compile
//        Compiler compiler = Compiler.getInstance(context);
//        compiler.compile(packageName);
//        BLangPackage compiledPkg = (BLangPackage) compiler.getAST();
//
//        return compiledPkg;
//    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String packageName, String functionName,
                                  BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            String msg = "";
            for (Diagnostic diagnostic : compileResult.getDiagnostics()) {
                msg += diagnostic.getMessage() + "\n";
            }
            throw new IllegalStateException("compilation contains errors.. " + msg);
        }
        ProgramFile programFile = compileResult.getProgFile();
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        return BLangFunctions.invokeEntrypointCallable(programFile, packageName, functionName, args);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param packageName   Name of the package to invoke
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String packageName, String functionName) {
        BValue[] args = {};
        return invoke(compileResult, packageName, functionName, args);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            String msg = "";
            for (Diagnostic diagnostic : compileResult.getDiagnostics()) {
                msg += diagnostic + "\n";
            }
            throw new IllegalStateException("compilation contains errors.. " + msg);
        }
        ProgramFile programFile = compileResult.getProgFile();
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        return BLangFunctions.invokeEntrypointCallable(programFile, programFile.getEntryPkgName(), functionName, args);
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName) {
        BValue[] args = {};
        return invoke(compileResult, functionName, args);
    }

    /**
     * Compile and run a ballerina file.
     *
     * @param sourceFilePath Path to the ballerina file.
     */
//    public static void run(String sourceFilePath) {
//        // TODO: improve. How to get the output
//        CompileResult result = compile(sourceFilePath);
//        ProgramFile programFile = result.getProgFile();
//        Debugger debugger = new Debugger(programFile);
//        programFile.setDebugger(debugger);
//
//        // If there is no main or service entry point, throw an error
//        if (!programFile.isMainEPAvailable() && !programFile.isServiceEPAvailable()) {
//            throw new RuntimeException("main function not found in '" + programFile.getProgramFilePath() + "'");
//        }
//
//        if (programFile.isMainEPAvailable()) {
//            LauncherUtils.runMain(programFile, new String[0]);
//        } else {
//            LauncherUtils.runServices(programFile);
//        }
//    }

    /**
     * Assert an error.
     *
     * @param result          Result from compilation
     * @param errorIndex      Index of the error in the result
     * @param expectedErrMsg  Expected error message
     * @param expectedErrLine Expected line number of the error
     * @param expectedErrCol  Expected column number of the error
     */
    public static void validateError(CompileResult result, int errorIndex, String expectedErrMsg, int expectedErrLine,
                                     int expectedErrCol) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.getMessage(), expectedErrMsg, "incorrect error message:");
        Assert.assertEquals(diag.getPosition().getStartLine(), expectedErrLine, "incorrect line number:");
        Assert.assertEquals(diag.getPosition().getStartColumn(), expectedErrCol, "incorrect column position:");
    }

    public static String readFileAsString(String path) {
        InputStream is = ClassLoader.getSystemResourceAsStream(path);
        InputStreamReader inputStreamREader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStreamREader = new InputStreamReader(is);
            br = new BufferedReader(inputStreamREader);
            String content = br.readLine();
            if (content == null) {
                return sb.toString();
            }

            sb.append(content);

            while ((content = br.readLine()) != null) {
                sb.append("\n" + content);
            }
        } catch (IOException ignore) {
        } finally {
            if (inputStreamREader != null) {
                try {
                    inputStreamREader.close();
                } catch (IOException ignore) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                }
            }
        }
        return sb.toString();
    }

    public static BStruct createAndGetStruct(ProgramFile programFile, String packagePath, String structName) {
        PackageInfo structPackageInfo = programFile.getPackageInfo(packagePath);
        StructInfo structInfo = structPackageInfo.getStructInfo(structName);
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }
}
