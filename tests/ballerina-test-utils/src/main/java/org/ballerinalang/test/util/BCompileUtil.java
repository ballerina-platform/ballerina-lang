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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.FutureValue;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.BootstrapRunner;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.bir.BackendDriver;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.test.util.TestConstant.ENABLE_JBALLERINA_TESTS;
import static org.ballerinalang.test.util.TestConstant.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.JAR_CACHE_DIR_NAME;

/**
 * Utility methods for compile Ballerina files.
 *
 * @since 0.94
 */
public class BCompileUtil {

    //TODO find a way to remove below line.
    private static Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

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
        return compileOnJBallerina(sourceFilePath, false, true);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source module/file
     * @return Semantic errors
     */
    public static CompileResult compileOffline(String sourceFilePath) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(OFFLINE, "true");
        context.put(CompilerOptions.class, options);
        return compileOnJBallerina(context, sourceFilePath, false, true);
    }

    /**
     * Compile on a separated process.
     *
     * @param sourceFilePath Path to source module/file
     * @return Semantic errors
     */
    public static CompileResult compileInProc(String sourceFilePath) {
        System.setProperty("java.command", "java");
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        CompilerContext context = new CompilerContext();
        return compileOnJBallerina(context, sourceRoot.toString(), packageName, false, true, true, false);
    }

    /**
     * Only compiles the file and does not run them.
     *
     * @param sourceFilePath Path to source module/file
     * @return compiled results
     */
    public static CompileResult compileOnly(String sourceFilePath) {
        return compileOnJBallerina(sourceFilePath, false, false);
    }

    // This is a temp fix until service test are fix
    public static CompileResult compile(boolean temp, String sourceFilePath) {
        return compileOnJBallerina(sourceFilePath, temp, true);
    }

    // This is a temp fix until service test are fix
    public static CompileResult compileOffline(boolean temp, String sourceFilePath) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());

        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(OFFLINE, "true");
        context.put(CompilerOptions.class, options);
        return compileOnJBallerina(context, sourceRoot.toString(), packageName, temp, true);
    }

    private static void runInit(BLangPackage bLangPackage, ClassLoader classLoader, boolean temp)
            throws ClassNotFoundException {
        String initClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                                                               bLangPackage.packageID.name.value,
                                                               TestConstant.MODULE_INIT_CLASS_NAME);
        Class<?> initClazz = classLoader.loadClass(initClassName);
        final Scheduler scheduler = new Scheduler(false);
        runOnSchedule(initClazz, ASTBuilderUtil.createIdentifier(null, "$moduleInit"), scheduler);
        runOnSchedule(initClazz, ASTBuilderUtil.createIdentifier(null, "$moduleStart"), scheduler);
        if (temp) {
            scheduler.immortal = true;
            new Thread(scheduler::start).start();
        }
    }

    private static void runOnSchedule(Class<?> initClazz, BLangIdentifier name, Scheduler scheduler) {
        String funcName = cleanupFunctionName(name);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, Strand.class);
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return method.invoke(null, objects[0]);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof RuntimeException) {
                        throw (RuntimeException) targetException;
                    } else {
                        throw new RuntimeException(targetException);
                    }
                } catch (IllegalAccessException e) {
                    throw new BallerinaException("Method has private access", e);
                }
            };
            final FutureValue out = scheduler.schedule(new Object[1], func, null, null, null,
                    BTypes.typeAny);
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

    public static CompileResult compileWithoutExperimentalFeatures(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.BIR_GEN, false);
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
     * @return Semantic errors
     */
    public static CompileResult compileWithTests(String sourceRoot, String packageName) {
        return compile(sourceRoot, packageName, true, true);
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
        return compile(sourceRoot, packageName, init, false);
    }

    private static CompileResult compile(String sourceRoot, String packageName, boolean init, boolean withTests) {
        String filePath = concatFileName(sourceRoot, resourceDir);
        Path rootPath = Paths.get(filePath);
        Path packagePath = Paths.get(packageName);
        return getCompileResult(packageName, rootPath, packagePath, init, withTests);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot  root path of the modules as a path object
     * @param packageName name of the module to compile
     * @param init init the module or not
     * @param withTests compile with tests or not
     * @return Semantic errors
     */
    public static CompileResult compile(Path sourceRoot, String packageName, boolean init, boolean withTests) {
        Path packagePath = Paths.get(packageName);
        return getCompileResult(packageName, sourceRoot, packagePath, init, withTests);
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
        return getCompileResult(packageName, rootPath, packagePath, init, false);
    }

    private static CompileResult getCompileResult(String packageName, Path rootPath, Path packagePath, boolean init,
            boolean withTests) {
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
            return compileOnJBallerina(rootPath.toString(), effectiveSource, false, init, withTests);
        }

        effectiveSource = packageName;
        return compileOnJBallerina(rootPath.toString(), effectiveSource, new FileSystemProjectDirectory(rootPath),
                init, withTests);
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
            path.append(windowsFolderSeparator).append(fileName);
        } else {
            path.append(unixFolderSeparator).append(fileName);
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
        return comResult;
    }

    /**
     * Compile and return the compiled package node.
     *
     * @param sourceFilePath Path to source module/file
     * @param compilerPhase  The compiler phase - BIR_GEN
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
        InputStream is = new FileInputStream(path);
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

    public static boolean jBallerinaTestsEnabled() {
        String value = System.getProperty(ENABLE_JBALLERINA_TESTS);
        return Boolean.parseBoolean(value);
    }

    private static CompileResult compileOnJBallerina(String sourceRoot, String packageName,
                                                     SourceDirectory sourceDirectory, boolean init, boolean withTests) {
        CompilerContext context = new CompilerContext();
        context.put(SourceDirectory.class, sourceDirectory);
        return compileOnJBallerina(context, sourceRoot, packageName, false, init, withTests);
    }

    public static CompileResult compileOnJBallerina(String sourceRoot, String packageName, boolean temp, boolean init) {
        return compileOnJBallerina(sourceRoot, packageName, temp, init, false);
    }

    private static CompileResult compileOnJBallerina(String sourceRoot, String packageName, boolean temp, boolean init,
            boolean withTests) {
        CompilerContext context = new CompilerContext();
        return compileOnJBallerina(context, sourceRoot, packageName, temp, init, withTests);
    }

    public static CompileResult compileOnJBallerina(CompilerContext context, String sourceRoot, String packageName,
                                                    boolean temp, boolean init) {
        return compileOnJBallerina(context, sourceRoot, packageName, temp, init, false, false);
    }

    private static CompileResult compileOnJBallerina(CompilerContext context, String sourceRoot, String packageName,
                                                    boolean temp, boolean init, boolean withTests) {
        return compileOnJBallerina(context, sourceRoot, packageName, temp, init, false, withTests);
    }

    private static CompileResult compileOnJBallerina(CompilerContext context, String sourceRoot, String packageName,
                                                     boolean temp, boolean init, boolean inProc, boolean withTests) {
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(LOCK_ENABLED, Boolean.toString(true));
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());
        if (withTests) {
            options.put(CompilerOptionName.SKIP_TESTS, "false");
            options.put(CompilerOptionName.TEST_ENABLED, "true");
        }

        CompileResult compileResult = compile(context, packageName, CompilerPhase.BIR_GEN, withTests);
        if (compileResult.getErrorCount() > 0) {
            return compileResult;
        }

        BLangPackage bLangPackage = (BLangPackage) compileResult.getAST();
        BackendDriver backendDriver = BackendDriver.getInstance(context);
        try {
            Path buildDir = Paths.get("build").toAbsolutePath().normalize();
            Path systemBirCache = buildDir.resolve("bir-cache");
            URLClassLoader cl = createClassLoaders(backendDriver, bLangPackage, systemBirCache, buildDir,
                    Optional.empty(), false, inProc);
            compileResult.setClassLoader(cl);

            // TODO: calling run on compile method is wrong, should be called from BRunUtil
            if (compileResult.getErrorCount() == 0 && init) {
                runInit(bLangPackage, cl, temp);
            }
            return compileResult;
        } catch (ClassNotFoundException | IOException e) {
            throw new BLangRuntimeException("Error during jvm code gen of the test", e);
        }
    }

    public static String runMain(CompileResult compileResult, String[] args) {
        ExitDetails exitDetails = run(compileResult, args);

        if (exitDetails.exitCode != 0) {
            throw new RuntimeException(exitDetails.errorOutput);
        }
        return exitDetails.consoleOutput;
    }

    public static ExitDetails run(CompileResult compileResult, String[] args) {
        BLangPackage compiledPkg = ((BLangPackage) compileResult.getAST());
        String initClassName = BFileUtil.getQualifiedClassName(compiledPkg.packageID.orgName.value,
                compiledPkg.packageID.name.value, MODULE_INIT_CLASS_NAME);
        URLClassLoader classLoader = compileResult.classLoader;


        try {
            Class<?> initClazz = classLoader.loadClass(initClassName);
            final List<String> actualArgs = new ArrayList<>();
            actualArgs.add(0, "java");
            actualArgs.add(1, "-cp");
            String classPath = System.getProperty("java.class.path") + ":" + getClassPath(classLoader);
            actualArgs.add(2, classPath);
            actualArgs.add(3, initClazz.getCanonicalName());
            actualArgs.addAll(Arrays.asList(args));

            final Runtime runtime = Runtime.getRuntime();
            final Process process = runtime.exec(actualArgs.toArray(new String[0]));
            String consoleInput = getConsoleOutput(process.getInputStream());
            String consoleError = getConsoleOutput(process.getErrorStream());
            process.waitFor();
            int exitValue = process.exitValue();
            return new ExitDetails(exitValue, consoleInput, consoleError);
        } catch (ClassNotFoundException | InterruptedException | IOException e) {
            throw new RuntimeException("Main method invocation failed", e);
        }
    }

    private static String getClassPath(URLClassLoader cl) {
        URL[] urls = cl.getURLs();
        StringJoiner joiner = new StringJoiner(":");
        for (URL url : urls) {
            joiner.add(url.getPath());
        }
        return joiner.toString();
    }

    private static String getConsoleOutput(InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
        reader.lines().iterator().forEachRemaining(sj::add);
        return sj.toString();
    }

    private static CompileResult compileOnJBallerina(String sourceFilePath, boolean temp, boolean init) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return compileOnJBallerina(sourceRoot.toString(), packageName, temp, init);
    }

    private static CompileResult compileOnJBallerina(CompilerContext context, String sourceFilePath,
                                                     boolean temp, boolean init) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return compileOnJBallerina(context, sourceRoot.toString(), packageName, temp, init);
    }

    private static URLClassLoader createClassLoaders(BackendDriver backendDriver,
                                                     BLangPackage bLangPackage,
                                                     Path systemBirCache,
                                                     Path buildRoot,
                                                     Optional<Path> jarTargetRoot,
                                                     boolean dumpBir,
                                                     boolean inProc) throws IOException {
        Path testBirTempRoot = buildRoot.resolve("test-bir-temp");
        byte[] bytes = PackageFileWriter.writePackage(bLangPackage.symbol.birPackageFile);
        String fileName = calcFileNameForJar(bLangPackage);
        Files.createDirectories(testBirTempRoot);
        Path intermediates = Files.createTempDirectory(testBirTempRoot, fileName + "-");
        Path entryBir = intermediates.resolve(fileName + ".bir");
        Path jarTarget = jarTargetRoot.orElse(intermediates).resolve(fileName + ".jar");
        Files.write(entryBir, bytes);

        Path importsBirCache = intermediates.resolve("imports").resolve("bir-cache");
        Path importsTarget = importsBirCache.getParent().resolve("generated-bir-jar");
        Files.createDirectories(importsTarget);
        writeNonEntryPkgs(bLangPackage.symbol.imports, systemBirCache, importsBirCache, importsTarget, dumpBir,
                backendDriver);

        backendDriver.execute(bLangPackage.symbol.bir, dumpBir, jarTarget);

        if (!Files.exists(jarTarget)) {
            throw new RuntimeException("Compiled binary jar is not found: " + jarTarget);
        }
        int index = 0;
        URL[] urls;
        List<URL> jarFiles = new ArrayList<>();

        addLibsFromJarCache(buildRoot, jarFiles);

        addLibsFromImportsCache(importsTarget, jarFiles);

        if (jarFiles.size() > 0) {
            urls = new URL[jarFiles.size() + 1];
            for (URL file : jarFiles) {
                urls[index++] = file;
            }
        } else {
            urls = new URL[1];
        }
        urls[index] = jarTarget.toFile().toURI().toURL();
        return new URLClassLoader(urls);
    }

    private static void addLibsFromImportsCache(Path importsTarget, List<URL> jarFiles) throws MalformedURLException {
        File importsCache = importsTarget.toFile();
        if (importsCache.isDirectory()) {
            for (String jarFile : Objects.requireNonNull(importsCache.list())) {
                jarFiles.add(Paths.get(importsCache.getPath(), jarFile).toUri().toURL());
            }
        }
    }

    private static void addLibsFromJarCache(Path buildRoot, List<URL> jarFiles) throws MalformedURLException {
        Path importModulesJarCache = buildRoot.resolve(Paths.get(System.getProperty(BALLERINA_HOME))).
                resolve(BALLERINA_HOME_LIB).resolve(JAR_CACHE_DIR_NAME);
        File importsJarCache = importModulesJarCache.toFile();
        if (importsJarCache.isDirectory()) {
            for (String jarFile : Objects.requireNonNull(importsJarCache.list())) {
                jarFiles.add(Paths.get(importsJarCache.getPath(), jarFile).toUri().toURL());
            }
        }
    }

    private static void generateJarBinary(String entryBir, String jarOutputPath, boolean dumpBir,
                                          List<String> jarFilePaths, String... birCachePaths) {
        ExitHandler exitHandler = new ExitHandler();
        System.setSecurityManager(exitHandler);
        try {
            Class<?> backendMain = Class.forName("ballerina.compiler_backend_jvm.___init");
            Method backendMainMethod = backendMain.getMethod("main", String[].class);
            List<String> params = BootstrapRunner.createArgsForCompilerBackend(entryBir, jarOutputPath, dumpBir, true,
                    birCachePaths, jarFilePaths);
            backendMainMethod.invoke(null, new Object[] { params.toArray(new String[0]) });
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof StopExitException) {
                // ignore the vm exiting and continue;
                return;
            }
            throw new BLangCompilerException(target.getMessage(), e);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new BLangCompilerException("could not invoke compiler backend", e);
        } finally {
            exitHandler.reset();
        }
    }

    private static void writeNonEntryPkgs(List<BPackageSymbol> imports, Path birCache, Path importsBirCache,
                                          Path jarTargetDir, boolean dumpBir, BackendDriver backendDriver) {
        for (BPackageSymbol pkg : imports) {
            PackageID id = pkg.pkgID;
            // Todo: ballerinax check shouldn't be here. This should be fixed by having a proper package hierarchy.
            // Todo: Remove ballerinax check after fixing it by the packerina team
            if (!"ballerina".equals(id.orgName.value) && !"ballerinax".equals(id.orgName.value)) {
                writeNonEntryPkgs(pkg.imports, birCache, importsBirCache, jarTargetDir, dumpBir, backendDriver);
                Path jarOutputPath = jarTargetDir.resolve(id.name.value + ".jar");
                if (pkg.bir != null) {
                    backendDriver.execute(pkg.bir, dumpBir, jarOutputPath);
                }
            }
        }
    }

    private static Path getModuleBir(BPackageSymbol pkg, Path importsBirCache) throws IOException {
        PackageID id = pkg.pkgID;
        byte[] bytes = PackageFileWriter.writePackage(pkg.birPackageFile);
        Path pkgBirDir = importsBirCache.resolve(id.orgName.value).resolve(id.name.value)
                .resolve(id.version.value.isEmpty() ? "0.0.0" : id.version.value);
        Files.createDirectories(pkgBirDir);
        Path pkgBir = pkgBirDir.resolve(id.name.value + ".bir");
        Files.write(pkgBir, bytes);
        return pkgBir;
    }

    private static String calcFileNameForJar(BLangPackage bLangPackage) {
        PackageID pkgID = bLangPackage.pos.src.pkgID;
        Name sourceFileName = pkgID.sourceFileName;
        if (sourceFileName != null) {
            return sourceFileName.value.replaceAll("\\.bal$", "");
        }
        return pkgID.name.value;
    }

    private static class ExitHandler extends SecurityManager {

        private SecurityManager defaultMgr = System.getSecurityManager();

        public void checkPermission(Permission perm) {
        }

        public void checkExit(int status) {
            super.checkExit(status);
            // Throw an exception if an System.exit(0) is called.
            throw new StopExitException();
        }

        public void reset() {
            System.setSecurityManager(defaultMgr);
        }
    }

    private static class StopExitException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Class to hold program execution outputs.
     */
    public static class ExitDetails {
        public int exitCode;
        public String consoleOutput;
        public String errorOutput;

        public ExitDetails(int exitCode, String consoleOutput, String errorOutput) {
            this.exitCode = exitCode;
            this.consoleOutput = consoleOutput;
            this.errorOutput = errorOutput;
        }
    }
}
