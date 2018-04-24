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
package org.ballerinalang.launcher.util;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Utility methods for compile Ballerina files.
 *
 * @since 0.94
 */
public class BCompileUtil {

    //TODO find a way to remove below line.
    private static Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

//    Compile and setup methods
    /**
     * Compile and return the semantic errors. Error scenarios cannot use this method.
     *
     * @param sourceFilePath Path to source package/file
     * @return compileResult
     */
    public static CompileResult compileAndSetup(String sourceFilePath) {
        CompileResult compileResult = compile(sourceFilePath, CompilerPhase.CODE_GEN);
        BRunUtil.invokePackageInit(compileResult);
        return compileResult;
    }

    /**
     * Compile and return the semantic errors. Error scenarios cannot use this method.
     *
     * @param obj this is to find the original callers location.
     * @param sourceRoot  root path of the source packages
     * @param packageName name of the package to compile
     * @return compileResult
     */
    public static CompileResult compileAndSetup(Object obj, String sourceRoot, String packageName) {
        CompileResult compileResult = compile(obj, sourceRoot, packageName);
        BRunUtil.invokePackageInit(compileResult, packageName);
        return compileResult;
    }

//    Compile methods
    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source package/file
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.CODE_GEN);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param obj this is to find the original callers location.
     * @param sourceRoot  root path of the source packages
     * @param packageName name of the package to compile
     * @return Semantic errors
     */
    public static CompileResult compile(Object obj, String sourceRoot, String packageName) {
        try {
            String effectiveSource;
            CodeSource codeSource = obj.getClass().getProtectionDomain().getCodeSource();
            URL location = codeSource.getLocation();
            URI locationUri = location.toURI();
            Path pathLocation = Paths.get(locationUri);
            String filePath = concatFileName(sourceRoot, pathLocation);
            Path rootPath = Paths.get(filePath);
            Path packagePath = Paths.get(packageName);
            if (Files.isDirectory(packagePath)) {
                String[] pkgParts = packageName.split("\\/");
                List<Name> pkgNameComps = Arrays.stream(pkgParts)
                        .map(part -> {
                            if (part.equals("")) {
                                return Names.EMPTY;
                            } else if (part.equals("_")) {
                                return Names.EMPTY;
                            }
                            return new Name(part);
                        })
                        .collect(Collectors.toList());
                // TODO: orgName is anon, fix it.
                PackageID pkgId = new PackageID(Names.ANON_ORG, pkgNameComps, Names.DEFAULT_VERSION);
                effectiveSource = pkgId.getName().getValue();
                return compile(rootPath.toString(), effectiveSource, CompilerPhase.CODE_GEN);
            } else {
                effectiveSource = packageName;
                return compile(rootPath.toString(), effectiveSource, CompilerPhase.CODE_GEN,
                        new FileSystemProjectDirectory(rootPath));
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
    }

    /**
     * <p>
     * concatenates a given filename to the provided path in directory.
     * </p>
     * <p>
     * <b>Note : </b> this function is relevant since in Unix the directory would be separated from backslash and
     * in unix the folder will be separated from forward slash.
     * </p>
     *
     * @param fileName     name of the file.
     * @param pathLocation location of the directory.
     * @return the path with directoryName + file.
     */
    public static String concatFileName(String fileName, Path pathLocation) {
        final String windowsFolderSeparator = "\\";
        final String unixFolderSeparator = "/";
        StringBuilder path = new StringBuilder(pathLocation.toAbsolutePath().toString());
        if (pathLocation.endsWith(windowsFolderSeparator)) {
            path = path.append(windowsFolderSeparator).append(fileName);
        } else {
            path = path.append(unixFolderSeparator).append(fileName);
        }
        return path.toString();
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source package/file
     * @param compilerPhase  Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath, CompilerPhase compilerPhase) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return compile(sourceRoot.toString(), packageName, compilerPhase);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot    root path of the source packages
     * @param packageName   name of the package to compile
     * @param compilerPhase Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        CompileResult comResult = new CompileResult();

        // catch errors
        DiagnosticListener listener = comResult::addDiagnostic;
        context.put(DiagnosticListener.class, listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        BLangPackage packageNode = compiler.compile(packageName);
        comResult.setAST(packageNode);
        if (comResult.getErrorCount() > 0 || CompilerPhase.CODE_GEN.compareTo(compilerPhase) > 0) {
            return comResult;
        }

        CompiledBinaryFile.ProgramFile programFile = compiler.getExecutableProgram(packageNode);
        if (programFile != null) {
            ProgramFile pFile = LauncherUtils.getExecutableProgram(programFile);
            comResult.setProgFile(pFile);
        }

        return comResult;
    }

    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase,
                                        SourceDirectory sourceDirectory) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        context.put(SourceDirectory.class, sourceDirectory);

        CompileResult comResult = new CompileResult();

        // catch errors
        DiagnosticListener listener = comResult::addDiagnostic;
        context.put(DiagnosticListener.class, listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        BLangPackage packageNode = compiler.compile(packageName);
        comResult.setAST(packageNode);
        CompiledBinaryFile.ProgramFile programFile = compiler.getExecutableProgram(packageNode);
        if (programFile != null) {
            comResult.setProgFile(LauncherUtils.getExecutableProgram(programFile));
        }

        return comResult;
    }

    /**
     * Compile and return the compiled package node.
     *
     * @param sourceFilePath Path to source package/file
     * @return compiled package node
     */
    public static BLangPackage compileAndGetPackage(String sourceFilePath) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, resourceDir.resolve(sourceRoot).toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        CompileResult comResult = new CompileResult();

        // catch errors
        DiagnosticListener listener = comResult::addDiagnostic;
        context.put(DiagnosticListener.class, listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        return compiler.compile(packageName);
    }

    /**
     * Compile and run a ballerina file.
     *
     * @param sourceFilePath Path to the ballerina file.
     */
    public static void run(String sourceFilePath) {
        // TODO: improve. How to get the output
        CompileResult result = compile(sourceFilePath);
        ProgramFile programFile = result.getProgFile();

        // If there is no main or service entry point, throw an error
        if (!programFile.isMainEPAvailable() && !programFile.isServiceEPAvailable()) {
            throw new RuntimeException("main function not found in '" + programFile.getProgramFilePath() + "'");
        }

        if (programFile.isMainEPAvailable()) {
            LauncherUtils.runMain(programFile, new String[0]);
        } else {
            LauncherUtils.runServices(programFile);
        }
    }

    public static String readFileAsString(String path) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(path);
        InputStreamReader inputStreamREader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStreamREader = new InputStreamReader(is, StandardCharsets.UTF_8);
            br = new BufferedReader(inputStreamREader);
            String content = br.readLine();
            if (content == null) {
                return sb.toString();
            }

            sb.append(content);

            while ((content = br.readLine()) != null) {
                sb.append('\n').append(content);
            }
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


    /**
     * Used by IntelliJ IDEA plugin to provide semantic analyzing capability.
     *
     * @param classLoader a {@link ClassLoader} to be set as thread context class loader. This is used by {@link
     *                    java.util.ServiceLoader}. Otherwise semantic analyzing capability providing wont work since it
     *                    cant find core package.
     * @param sourceRoot  source root of a project
     * @param fileName    either the file name (if in project root) or the package name
     * @return list of diagnostics
     */
    public static List<Diagnostic> getDiagnostics(ClassLoader classLoader, String sourceRoot, String fileName) {
        Thread.currentThread().setContextClassLoader(classLoader);
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        CompileResult comResult = new CompileResult();

        // catch errors
        DiagnosticListener listener = comResult::addDiagnostic;
        context.put(DiagnosticListener.class, listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        BLangPackage entryPackageNode = compiler.compile(fileName);
        CompiledBinaryFile.ProgramFile programFile = compiler.getExecutableProgram(entryPackageNode);
        if (programFile != null) {
            comResult.setProgFile(LauncherUtils.getExecutableProgram(programFile));
        }
        Diagnostic[] diagnostics = comResult.getDiagnostics();
        return Arrays.stream(diagnostics).collect(Collectors.toList());
    }
}
