/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test;

import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.PackageManifest;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.providers.ConfigDetails;
import io.ballerina.runtime.internal.launch.LaunchUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.BmpStringValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.FutureValue;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.NonBmpStringValue;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.XmlValue;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ANON_ORG;
import static io.ballerina.runtime.api.constants.RuntimeConstants.DOT;
import static org.ballerinalang.test.util.TestConstant.CONFIGURATION_CLASS_NAME;
import static org.ballerinalang.test.util.TestConstant.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.util.Names.DEFAULT_MAJOR_VERSION;

/**
 * Utility methods for run Ballerina functions with JVM arguments and return values.
 *
 * @since 2.0.0
 */
public class BRunUtil {

    private static final Boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.getDefault())
            .contains("win");

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @return return values of the function
     */
    public static Object invoke(CompileResult compileResult, String functionName, Object[] args) {
        return invokeOnJBallerina(compileResult, functionName, args, getJvmParamTypes(args));
    }

    private static Class<?>[] getJvmParamTypes(Object[] args) {
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                paramTypes[i] = null;
            } else if (arg instanceof ObjectValue) {
                paramTypes[i] = ObjectValue.class;
            } else if (arg instanceof XmlValue) {
                paramTypes[i] = XmlValue.class;
            } else if (arg instanceof BmpStringValue) {
                paramTypes[i] = BmpStringValue.class;
            } else if (arg instanceof NonBmpStringValue) {
                paramTypes[i] = NonBmpStringValue.class;
            } else if (arg instanceof ArrayValue) {
                paramTypes[i] = ArrayValue.class;
            } else if (arg instanceof Integer) {
                paramTypes[i] = Long.class;
            } else if (arg instanceof Float) {
                paramTypes[i] = Double.class;
            } else if (arg instanceof Double) {
                paramTypes[i] = Double.class;
            } else if (arg instanceof Long) {
                paramTypes[i] = Long.class;
            } else if (arg instanceof Boolean) {
                paramTypes[i] = Boolean.class;
            } else if (arg instanceof MapValue) {
                paramTypes[i] = MapValue.class;
            } else if (arg instanceof ErrorValue) {
                paramTypes[i] = ErrorValue.class;
            } else if (arg instanceof DecimalValue) {
                paramTypes[i] = DecimalValue.class;
            } else if (arg instanceof HandleValue) {
                paramTypes[i] = HandleValue.class;
            } else if (arg instanceof Byte) {
                paramTypes[i] = Integer.class;
            } else {
                // This is done temporarily, until blocks are added here for all possible cases.
                throw new RuntimeException("unknown param type: " + arg.getClass());
            }
        }
        return paramTypes;
    }

    /**
     * This method takes care of invocation on JBallerina and the mapping of input and output values. It will use the
     * given JVM based argument and function details to invoke on JBallerina and return results as BValues to maintain
     * backward compatibility with existing invoke methods in BRunUtil.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @param args          Input parameters for the function
     * @param paramTypes    Types of the parameters of the function
     * @return return values of the function
     */
    private static Object invokeOnJBallerina(CompileResult compileResult, String functionName, Object[] args,
                                             Class<?>[] paramTypes) {
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        return invoke(compileResult, function, functionName, args, paramTypes);
    }

    /**
     * This method handles the input arguments.
     *
     * @param compileResult CompileResult instance
     * @param function      function model instance from BIR model
     * @param functionName  name of the function to be invoked
     * @param args          input arguments to be used with function invocation
     * @param paramTypes    types of the parameters of the function
     * @return return the result from function invocation
     */
    private static Object invoke(CompileResult compileResult, BIRNode.BIRFunction function, String functionName,
                                 Object[] args, Class<?>[] paramTypes) {
        assert args.length == paramTypes.length;

        Object[] jvmArgs = populateJvmArgumentArrays(args);

        Object jvmResult;
        PackageManifest packageManifest = compileResult.packageManifest();
        String funcClassName = JarResolver.getQualifiedClassName(packageManifest.org().toString(),
                packageManifest.name().toString(),
                packageManifest.version().toString(),
                getClassName(function.pos.lineRange().fileName()));

        try {
            Class<?> funcClass = compileResult.getClassLoader().loadClass(funcClassName);
            Method method = getMethod(functionName, funcClass);
            Function<Object[], Object> func = a -> {
                try {
                    return method.invoke(null, a);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if (t instanceof BLangTestException) {
                        throw ErrorCreator.createError(StringUtils.fromString(t.getMessage()));
                    }
                    if (t instanceof io.ballerina.runtime.api.values.BError bError) {
                        throw ErrorCreator.createError(StringUtils.fromString(
                                "error: " + bError.getPrintableStackTrace()));
                    }
                    if (t instanceof StackOverflowError) {
                        throw ErrorCreator.createError(StringUtils.fromString("error: " +
                                "{ballerina}StackOverflow {\"message\":\"stack overflow\"}"));
                    }
                    throw ErrorCreator.createError(StringUtils.fromString("Error while invoking function '" +
                            functionName + "'"), e);
                }
            };

            Scheduler scheduler = new Scheduler(false);
            FutureValue futureValue = scheduler.schedule(jvmArgs, func, null, null, new HashMap<>(),
                    PredefinedTypes.TYPE_ANY, "test",
                    new StrandMetadata(ANON_ORG, DOT, DEFAULT_MAJOR_VERSION.value, functionName));
            scheduler.start();
            if (futureValue.panic instanceof RuntimeException) {
                throw new BLangTestException(futureValue.panic.getMessage(),
                        futureValue.panic);
            }
            jvmResult = futureValue.result;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
        }

        return jvmResult;
    }

    private static Object[] populateJvmArgumentArrays(Object[] args) {
        Object[] jvmArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, jvmArgs, 1, args.length);
        return jvmArgs;
    }

    private static Method getMethod(String functionName, Class<?> funcClass) throws NoSuchMethodException {
        Method declaredMethod = Arrays.stream(funcClass.getDeclaredMethods())
                .filter(method -> functionName.equals(method.getName()))
                .findAny()
                .orElse(null);

        if (declaredMethod != null) {
            return declaredMethod;
        } else {
            throw new NoSuchMethodException(functionName + " is not found");
        }
    }

    private static String getClassName(String balFileName) {
        if (!balFileName.endsWith(".bal")) {
            return balFileName;
        }
        return balFileName.substring(0, balFileName.length() - 4);
    }

    private static BIRNode.BIRFunction getInvokedFunction(CompileResult compileResult, String functionName) {
        checkAndNotifyCompilationErrors(compileResult);
        BIRNode.BIRPackage birPackage = compileResult.defaultModuleBIR();
        for (BIRNode.BIRFunction function : birPackage.functions) {
            if (functionName.equals(function.name.value)) {
                return function;
            }
        }
        throw new RuntimeException("Function '" + functionName + "' is not defined");
    }

    private static void checkAndNotifyCompilationErrors(CompileResult compileResult) {
        if (compileResult.getErrorCount() > 0) {
            StringJoiner stringJoiner = new StringJoiner("\n", "\n", "");
            for (Diagnostic diagnostic : compileResult.getDiagnostics()) {
                stringJoiner.add(diagnostic.toString());
            }
            throw new IllegalStateException("There were compilation errors: " + stringJoiner);
        }
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName  Name of the function to invoke
     * @return return values of the function
     */
    public static Object invoke(CompileResult compileResult, String functionName) {
        Object[] args = {};
        return invoke(compileResult, functionName, args);
    }

    public static Object invokeAndGetJVMResult(CompileResult compileResult, String functionName) {
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        return invoke(compileResult, function, functionName, new Object[0], new Class<?>[0]);
    }

    public static String runMain(CompileResult compileResult, String... args) {
        return runMain(compileResult, new ArrayList<>(), args);
    }

    public static String runMain(CompileResult compileResult, List<String> javaOpts, String... args) {
        ExitDetails exitDetails = run(compileResult, javaOpts, args);
        if (exitDetails.exitCode != 0) {
            throw new RuntimeException(exitDetails.errorOutput);
        }
        return exitDetails.consoleOutput;
    }

    public static ExitDetails run(CompileResult compileResult, String... args) {
        return run(compileResult, new ArrayList<>(), args);
    }

    public static ExitDetails run(CompileResult compileResult, List<String> javaOpts, String... args) {
        checkAndNotifyCompilationErrors(compileResult);
        PackageManifest packageManifest = compileResult.packageManifest();
        String initClassName = JarResolver.getQualifiedClassName(packageManifest.org().toString(),
                packageManifest.name().toString(),
                packageManifest.version().toString(),
                MODULE_INIT_CLASS_NAME);
        Collection<JarLibrary> jarPathRequiredForExecution = compileResult.getJarPathRequiredForExecution();
        StringBuilder classPath = new StringBuilder();
        for (JarLibrary jarLibrary : jarPathRequiredForExecution) {
            classPath.append(File.pathSeparator).append(jarLibrary.path());
        }

        final List<String> actualArgs = new ArrayList<>();
        int index = 0;
        actualArgs.add(index++, "java");
        for (String javaOpt : javaOpts) {
            actualArgs.add(index++, javaOpt);
        }
        actualArgs.add(index++, "-cp");

        String classPathString = System.getProperty("java.class.path") + classPath;
        // Create an argument file for Windows to mitigate the long classpath issue.
        if (IS_WINDOWS) {
            classPathString = classPathString.replace(" ", "%20");
            String classPathArgs = "classPathArgs";
            try {
                File classPathArgsFile = File.createTempFile(classPathArgs, ".txt");
                FileWriter fileWriter = new FileWriter(classPathArgsFile);
                fileWriter.write(classPathString);
                fileWriter.close();
                actualArgs.add(index++, "@" + classPathArgsFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Error while creating classpath arguments file: " + classPathArgs, e);
            }
        } else {
            actualArgs.add(index++, classPathString);
        }
        actualArgs.add(index, initClassName);
        actualArgs.addAll(Arrays.asList(args));

        try {
            final Runtime runtime = Runtime.getRuntime();
            final Process process = runtime.exec(actualArgs.toArray(new String[0]));
            String consoleError = getConsoleOutput(process.getErrorStream());
            String consoleInput = getConsoleOutput(process.getInputStream());
            process.waitFor();
            int exitValue = process.exitValue();
            return new ExitDetails(exitValue, consoleInput, consoleError);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Main method invocation failed", e);
        }
    }

    private static String getConsoleOutput(InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
        reader.lines().iterator().forEachRemaining(sj::add);
        return sj.toString();
    }

    public static void runInit(CompileResult compileResult) throws ClassNotFoundException {
        PackageManifest packageManifest = compileResult.packageManifest();
        String org = packageManifest.org().toString();
        String module = packageManifest.name().toString();
        String version = packageManifest.version().toString();
        String initClassName = JarResolver.getQualifiedClassName(org, module, version, MODULE_INIT_CLASS_NAME);
        String configClassName = JarResolver.getQualifiedClassName(org, module, version, CONFIGURATION_CLASS_NAME);

        Class<?> initClazz = compileResult.getClassLoader().loadClass(initClassName);
        final Scheduler scheduler = new Scheduler(false);
        ConfigDetails configurationDetails = LaunchUtils.getConfigurationDetails();
        directRun(compileResult.getClassLoader().loadClass(configClassName), "$configureInit",
                new Class[]{Map.class, String[].class, Path[].class, String.class},
                new Object[]{new HashMap<>(), new String[]{},
                        configurationDetails.paths, configurationDetails.configContent});
        runOnSchedule(initClazz, "$moduleInit", scheduler);
        runOnSchedule(initClazz, "$moduleStart", scheduler);
//        if (temp) {
//            scheduler.immortal = true;
//            new Thread(scheduler::start).start();
//        }
    }

    private static void directRun(Class<?> initClazz, String functionName, Class[] paramTypes, Object[] args) {
        String funcName = JvmCodeGenUtil.cleanupFunctionName(functionName);
        String errorMsg = "Failed to invoke the function '%s' due to %s";
        Object response;
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, paramTypes);
            response = method.invoke(null, args);
            if (response instanceof Throwable throwable) {
                throw new BLangTestException(String.format(errorMsg, funcName, response), throwable);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new BLangTestException(String.format(errorMsg, funcName, e.getMessage()), e);
        }
    }

    public static void runOnSchedule(CompileResult compileResult, String functionName, Scheduler scheduler) {
        BIRNode.BIRFunction function = getInvokedFunction(compileResult, functionName);
        PackageManifest packageManifest = compileResult.packageManifest();
        String funcClassName = JarResolver.getQualifiedClassName(packageManifest.org().toString(),
                packageManifest.name().toString(),
                packageManifest.version().toString(),
                getClassName(function.pos.lineRange().fileName()));
        Class<?> funcClass = null;
        try {
            funcClass = compileResult.getClassLoader().loadClass(funcClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error while invoking function '" + functionName + "'", e);
        }
        runOnSchedule(funcClass, functionName, scheduler);
    }

    private static void runOnSchedule(Class<?> initClazz, String name, Scheduler scheduler) {
        runOnSchedule(initClazz, ASTBuilderUtil.createIdentifier(null, name), scheduler);
    }

    private static void runOnSchedule(Class<?> initClazz, BLangIdentifier name, Scheduler scheduler) {
        String funcName = JvmCodeGenUtil.cleanupFunctionName(name.value);
        try {
            final Method method = initClazz.getDeclaredMethod(funcName, Strand.class);
            //TODO fix following method invoke to scheduler.schedule()
            Function<Object[], Object> func = objects -> {
                try {
                    return method.invoke(null, objects[0]);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof RuntimeException runtimeException) {
                        throw runtimeException;
                    } else {
                        throw new RuntimeException(targetException);
                    }
                } catch (IllegalAccessException e) {
                    throw new BLangTestException("Method has private access", e);
                }
            };
            final FutureValue out = scheduler
                    .schedule(new Object[1], func, null, null, null, PredefinedTypes.TYPE_ANY, null, null);
            Scheduler.setDaemonStrand(out.strand);
            scheduler.start();
            final Throwable t = out.panic;
            if (t != null) {
                if (t instanceof ErrorValue errorValue) {
                    throw new BLangTestException("error: " + errorValue.getPrintableStackTrace());
                }
                throw (RuntimeException) t;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error while invoking function '" + funcName + "'", e);
        }
    }

    /**
     * Class to hold program execution outputs.
     */
    public static class ExitDetails {

        public final int exitCode;
        public final String consoleOutput;
        public final String errorOutput;

        public ExitDetails(int exitCode, String consoleOutput, String errorOutput) {
            this.exitCode = exitCode;
            this.consoleOutput = consoleOutput;
            this.errorOutput = errorOutput;
        }
    }

    private BRunUtil() {

    }
}
