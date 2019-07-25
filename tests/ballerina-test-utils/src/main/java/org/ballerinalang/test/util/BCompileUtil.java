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
package org.ballerinalang.test.util;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.tool.LauncherUtils;
import org.ballerinalang.util.BootstrapRunner;
import org.ballerinalang.util.JBallerinaInMemoryClassLoader;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.test.util.TestConstant.ENABLE_JBALLERINA_TESTS;
import static org.ballerinalang.test.util.TestConstant.MODULE_INIT_CLASS_NAME;

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
     * @param sourceFilePath Path to source module/file
     * @return compileResult
     */
    public static CompileResult compileAndSetup(String sourceFilePath) {
        CompileResult compileResult = compile(sourceFilePath, CompilerPhase.CODE_GEN);
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
        BLangProgramRunner.runProgram(compileResult.getProgFile(), new BValue[0]);
        return compileResult;
    }

    /**
     * Compile and return the semantic errors. Error scenarios cannot use this method.
     *
     * @param obj this is to find the original callers location.
     * @param sourceRoot  root path of the modules
     * @param packageName name of the module to compile
     * @return compileResult
     */
    public static CompileResult compileAndSetup(Object obj, String sourceRoot, String packageName) {
        CompileResult compileResult = compile(obj, sourceRoot, packageName);
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
        BLangProgramRunner.runProgram(compileResult.getProgFile(), new BValue[0]);
        return compileResult;
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source module/file
     * @return Semantic errors
     */
    public static CompileResult compileAndGetBIR(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.BIR_GEN);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source module/file
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath) {
        if (jBallerinaTestsEnabled()) {
            return compileOnJBallerina(sourceFilePath, false);
        }
        return compile(sourceFilePath, CompilerPhase.CODE_GEN);
    }

    // This is a temp fix until service test are fix
    public static CompileResult compile(boolean temp, String sourceFilePath) {
        return compileOnJBallerina(sourceFilePath, temp);
    }

    private static void runInit(BLangPackage bLangPackage, JBallerinaInMemoryClassLoader classLoader, boolean temp) {
        String initClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                                                               bLangPackage.packageID.name.value,
                                                               TestConstant.MODULE_INIT_CLASS_NAME);
        Class<?> initClazz = classLoader.loadClass(initClassName);
        if (temp) {
            final Scheduler scheduler = new Scheduler(4, false);
            runOnSchedule(initClazz, bLangPackage.initFunction.name, scheduler);
            runOnSchedule(initClazz, bLangPackage.startFunction.name, scheduler);
            scheduler.immortal = true;
            new Thread(scheduler::start).start();

        } else {
            runOnSchedule(initClazz, bLangPackage.initFunction.name);
            runOnSchedule(initClazz, bLangPackage.startFunction.name);
        }
    }

    private static void runOnSchedule(Class<?> initClazz, BLangIdentifier name) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, Strand.class);
            Scheduler scheduler = new Scheduler(4, false);
            //TODO fix following method invoke to scheduler.schedule()
            method.invoke(null, new Strand(scheduler));
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof org.ballerinalang.jvm.util.exceptions.BLangRuntimeException) {
                throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
            }
            if (t instanceof org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException) {
                throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
            }
            if (t instanceof ErrorValue) {
                throw new org.ballerinalang.util.exceptions
                        .BLangRuntimeException("error: " + ((ErrorValue) t).getPrintableStackTrace());
            }
            throw new RuntimeException("Error while invoking function '" + funcName + "'", e);
        } catch (Exception e) {
            throw new RuntimeException("Error while invoking function '" + funcName + "'", e);
        }
    }

    private static void runOnSchedule(Class<?> initClazz, BLangIdentifier name, Scheduler scheduler1) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, Strand.class);
            Scheduler scheduler = scheduler1;
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return method.invoke(null, objects[0]);
                } catch (InvocationTargetException e) {
                    throw (RuntimeException) e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };
            final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null);
            scheduler.start();
            final Throwable t = out.panic;
            if (t != null) {
                if (t instanceof org.ballerinalang.jvm.util.exceptions.BLangRuntimeException) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                }
                if (t instanceof org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(t.getMessage());
                }
                if (t instanceof ErrorValue) {
                    throw new org.ballerinalang.util.exceptions.BLangRuntimeException(
                            "error: " + ((ErrorValue) t).getPrintableStackTrace());
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error while invoking function '" + funcName + "'", e);
        }
    }

    private static String cleanupFunctionName(BLangIdentifier name) {
        return name.value.replaceAll("[.:/<>]", "_");
    }

    /**
     * Compile and return the semantic errors for tests.
     *
     * @param sourceFilePath Path to source module/file
     * @return Semantic errors
     */
    @Deprecated
    public static CompileResult compileOnBVM(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.CODE_GEN);
    }

    public static CompileResult compileWithoutExperimentalFeatures(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.CODE_GEN, false);
    }

    public static CompileResult compile(String sourceFilePath, boolean isSiddhiRuntimeEnabled) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return compile(sourceRoot.toString(), packageName, CompilerPhase.CODE_GEN, isSiddhiRuntimeEnabled, true);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot  root path of the modules
     * @param packageName name of the module to compile
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceRoot, String packageName) {
        return compile(sourceRoot, packageName, true);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot  root path of the modules
     * @param packageName name of the module to compile
     * @param init init the module or not
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceRoot, String packageName, boolean init) {
        String filePath = concatFileName(sourceRoot, resourceDir);
        Path rootPath = Paths.get(filePath);
        Path packagePath = Paths.get(packageName);
        return getCompileResult(packageName, rootPath, packagePath, init);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param obj this is to find the original callers location.
     * @param sourceRoot  root path of the modules
     * @param packageName name of the module to compile
     * @return Semantic errors
     */
    public static CompileResult compile(Object obj, String sourceRoot, String packageName) {
        return compile(obj, sourceRoot, packageName, true);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param obj this is to find the original callers location.
     * @param sourceRoot  root path of the modules
     * @param packageName name of the module to compile
     * @param init the module or not
     * @return Semantic errors
     */
    public static CompileResult compile(Object obj, String sourceRoot, String packageName, boolean init) {
        String filePath = concatFileName(sourceRoot, resourceDir);
        Path rootPath = Paths.get(filePath);
        Path packagePath = Paths.get(packageName);
        return getCompileResult(packageName, rootPath, packagePath, init);
    }

    private static CompileResult getCompileResult(String packageName, Path rootPath, Path packagePath, boolean init) {
        String effectiveSource;
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

            if (jBallerinaTestsEnabled()) {
                return compileOnJBallerina(rootPath.toString(), effectiveSource, false, init);
            }

            return compile(rootPath.toString(), effectiveSource, CompilerPhase.CODE_GEN);
        }

        effectiveSource = packageName;
        if (jBallerinaTestsEnabled()) {
            return compileOnJBallerina(rootPath.toString(), effectiveSource,
                    new FileSystemProjectDirectory(rootPath), init);
        }

        return compile(rootPath.toString(), effectiveSource, CompilerPhase.CODE_GEN,
                new FileSystemProjectDirectory(rootPath));
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
     * @param compilerPhase Compiler phase
     * @param enableExpFeatures Flag indicating to enable the experimental feature
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath, CompilerPhase compilerPhase, boolean enableExpFeatures) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return compile(sourceRoot.toString(), packageName, compilerPhase, enableExpFeatures);
    }


    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source package/file
     * @param compilerPhase  Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath, CompilerPhase compilerPhase) {
        return compile(sourceFilePath, compilerPhase, true);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot root path of the modules
     * @param packageName name of the module to compile
     * @param compilerPhase Compiler phase
     * @param enableExpFeatures Flag indicating to enable the experimental features
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase,
                                        boolean enableExpFeatures) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExpFeatures));

        return compile(context, packageName, compilerPhase, false);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot root path of the modules
     * @param packageName name of the module to compile
     * @param compilerPhase Compiler phase
     * @param isSiddhiRuntimeEnabled Flag indicating to enable siddhi runtime for stream processing
     * @param enableExpFeatures Flag indicating to enable the experimental features
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase,
                                        boolean isSiddhiRuntimeEnabled, boolean enableExpFeatures) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(CompilerOptionName.SIDDHI_RUNTIME_ENABLED, Boolean.toString(isSiddhiRuntimeEnabled));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExpFeatures));

        return compile(context, packageName, compilerPhase, false);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot    root path of the modules
     * @param packageName   name of the module to compile
     * @param compilerPhase Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase) {
        return compile(sourceRoot, packageName, compilerPhase, true);
    }

    /**
     * Compile with tests and return the semantic errors.
     *
     * @param context       Compiler Context
     * @param listener      the diagnostic log common to a project
     * @param packageName   name of the module to compile
     * @param compilerPhase Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compileWithTests(CompilerContext context,
                                                 CompileResult.CompileResultDiagnosticListener listener,
                                                 String packageName,
                                                 CompilerPhase compilerPhase) {
        return compile(context, listener, packageName, compilerPhase, true);
    }

    /**
     * Create a compiler context.
     *
     * @param sourceRoot    source root or project directory path
     * @param compilerPhase Compiler phase
     * @return new compiler context object
     */
    public static CompilerContext createCompilerContext(String sourceRoot, CompilerPhase compilerPhase) {
        return createCompilerContext(sourceRoot, compilerPhase, Boolean.TRUE);
    }

    public static CompilerContext createCompilerContext(String sourceRoot, CompilerPhase compilerPhase,
                                                        boolean enableExpFeatures) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(TEST_ENABLED, "true");
        options.put(SKIP_TESTS, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExpFeatures));
        return context;
    }

    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase,
                                        SourceDirectory sourceDirectory) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());
        context.put(SourceDirectory.class, sourceDirectory);

        CompileResult.CompileResultDiagnosticListener listener = new CompileResult.CompileResultDiagnosticListener();
        context.put(DiagnosticListener.class, listener);
        CompileResult comResult = new CompileResult(listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        BLangPackage packageNode = compiler.compile(packageName);
        comResult.setAST(packageNode);
        CompiledBinaryFile.ProgramFile programFile = compiler.getExecutableProgram(packageNode);
        if (programFile != null) {
            ProgramFile progFile = LauncherUtils.getExecutableProgram(programFile);
            progFile.setProgramFilePath(Paths.get(packageName));
            comResult.setProgFile(progFile);
        }

        return comResult;
    }

    private static CompileResult compile(CompilerContext context, String packageName,
                                         CompilerPhase compilerPhase, boolean withTests) {
        CompileResult.CompileResultDiagnosticListener listener = new CompileResult.CompileResultDiagnosticListener();
        context.put(DiagnosticListener.class, listener);
        return compile(context, listener, packageName, compilerPhase, withTests);
    }

    private static CompileResult compile(CompilerContext context,
                                         CompileResult.CompileResultDiagnosticListener listener,
                                         String packageName,
                                         CompilerPhase compilerPhase,
                                         boolean withTests) {
        CompileResult comResult = new CompileResult(listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        BLangPackage packageNode = compiler.compile(packageName, true);
        comResult.setAST(packageNode);
        if (comResult.getErrorCount() > 0) {
            return comResult;
        } else if (CompilerPhase.CODE_GEN.compareTo(compilerPhase) > 0 || compilerPhase == CompilerPhase.BIR_GEN) {
            return comResult;
        }
        CompiledBinaryFile.ProgramFile programFile;
        // If its executing tests, then check if the testable package is null or not. If its not null, then pass the
        // testable package node to generate the package program file.
        if (withTests && packageNode.containsTestablePkg()) {
            programFile = compiler.getExecutableProgram(packageNode.getTestablePkg());
        } else {
            // If its not executing tests or if its executing tests and the testable package is not present then pass
            // the bLangPackage node to generate the program file.
            programFile = compiler.getExecutableProgram(packageNode);
        }

        if (programFile != null) {
            ProgramFile pFile = LauncherUtils.getExecutableProgram(programFile);
            pFile.setProgramFilePath(Paths.get(packageName));
            comResult.setProgFile(pFile);
        }
        return comResult;
    }

    /**
     * Compile and return the compiled package node.
     *
     * @param sourceFilePath Path to source module/file
     * @return compiled module node
     */
    public static BLangPackage compileAndGetPackage(String sourceFilePath) {
        return compileAndGetPackage(sourceFilePath, CompilerPhase.CODE_GEN);
    }

    /**
     * Compile and return the compiled package node.
     *
     * @param sourceFilePath Path to source module/file
     * @param compilerPhase  The compiler phase - BIR_GEN or CODE_GEN
     * @return compiled module node
     */
    public static BLangPackage compileAndGetPackage(String sourceFilePath, CompilerPhase compilerPhase) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, resourceDir.resolve(sourceRoot).toString());
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());

        CompileResult.CompileResultDiagnosticListener listener = new CompileResult.CompileResultDiagnosticListener();
        context.put(DiagnosticListener.class, listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        return compiler.compile(packageName);
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

    public static BMap<String, BValue> createAndGetStruct(ProgramFile programFile, String packagePath,
                                                          String structName) {
        PackageInfo structPackageInfo = programFile.getPackageInfo(packagePath);
        StructureTypeInfo typeInfo = structPackageInfo.getStructInfo(structName);
        return BLangVMStructs.createBStruct(typeInfo);
    }


    /**
     * Used by IntelliJ IDEA plugin to provide semantic analyzing capability.
     *
     * @param classLoader a {@link ClassLoader} to be set as thread context class loader. This is used by {@link
     *                    java.util.ServiceLoader}. Otherwise semantic analyzing capability providing wont work since it
     *                    cant find core package.
     * @param sourceRoot  source root of a project
     * @param fileName    either the file name (if in project root) or the module name
     * @return list of diagnostics
     */
    public static List<Diagnostic> getDiagnostics(ClassLoader classLoader, String sourceRoot, String fileName) {
        Thread.currentThread().setContextClassLoader(classLoader);
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());

        CompileResult.CompileResultDiagnosticListener listener = new CompileResult.CompileResultDiagnosticListener();
        context.put(DiagnosticListener.class, listener);
        CompileResult comResult = new CompileResult(listener);

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


    public static boolean jBallerinaTestsEnabled() {
        String value = System.getProperty(ENABLE_JBALLERINA_TESTS);
        return value != null && Boolean.valueOf(value);
    }

    private static CompileResult compileOnJBallerina(String sourceRoot, String packageName,
                                                     SourceDirectory sourceDirectory, boolean init) {
        CompilerContext context = new CompilerContext();
        context.put(SourceDirectory.class, sourceDirectory);
        return compileOnJBallerina(context, sourceRoot, packageName, false, init);
    }

    public static CompileResult compileOnJBallerina(String sourceRoot, String packageName, boolean temp, boolean init) {
        CompilerContext context = new CompilerContext();
        return compileOnJBallerina(context, sourceRoot, packageName, temp, init);
    }

    public static CompileResult compileOnJBallerina(CompilerContext context, String sourceRoot, String packageName,
            boolean temp, boolean init) {
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());

        CompileResult compileResult = compile(context, packageName, CompilerPhase.BIR_GEN, false);
        if (compileResult.getErrorCount() > 0) {
            return compileResult;
        }

        BLangPackage bLangPackage = (BLangPackage) compileResult.getAST();
        try {
            Path buildDir = Paths.get("build").toAbsolutePath();
            Path systemBirCache = buildDir.resolve("bir-cache");
            JBallerinaInMemoryClassLoader cl = BootstrapRunner.createClassLoaders(bLangPackage,
                                                                                  systemBirCache,
                                                                                  buildDir.resolve("test-bir-temp"),
                                                                                  Optional.empty(), false);
            compileResult.setClassLoader(cl);

            // TODO: calling run on compile method is wrong, should be called from BRunUtil
            if (init) {
                runInit(bLangPackage, cl, temp);
            }

            return compileResult;

        } catch (IOException e) {
            throw new BLangRuntimeException("Error during jvm code gen of the test", e);
        }
    }

    public static void runMain(CompileResult compileResult, String[] args) throws Throwable {
        String initClassName = BFileUtil.getQualifiedClassName(((BLangPackage)
                compileResult.getAST()).packageID.orgName.value,
                ((BLangPackage) compileResult.getAST()).packageID.name.value, MODULE_INIT_CLASS_NAME);
        Class<?> initClazz = compileResult.classLoader.loadClass(initClassName);
        Method mainMethod = null;
        try {
            mainMethod = initClazz.getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof ErrorValue) {
                throw e.getTargetException();
            }
            throw new RuntimeException("Main method invocation failed", e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Main method invocation failed", e);
        }

    }

    private static CompileResult compileOnJBallerina(String sourceFilePath, boolean temp) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return compileOnJBallerina(sourceRoot.toString(), packageName, temp, true);
    }
}
