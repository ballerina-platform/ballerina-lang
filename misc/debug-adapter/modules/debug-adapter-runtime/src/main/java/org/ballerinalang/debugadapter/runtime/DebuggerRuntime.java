/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.debugadapter.runtime;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlDetails;
import io.ballerina.runtime.internal.launch.LaunchUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.StringValue;
import io.ballerina.runtime.internal.values.TypedescValue;
import org.ballerinalang.langlib.internal.GetElements;
import org.ballerinalang.langlib.internal.GetFilteredChildrenFlat;
import org.ballerinalang.langlib.internal.SelectDescendants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static io.ballerina.runtime.api.creators.TypeCreator.createErrorType;

/**
 * This class contains the set of runtime helper util methods to support debugger expression evaluation.
 * <p>
 * These utils methods must be class-loaded into the program JVM to evaluate
 * <ul>
 *  <li> functions
 *  <li> object methods
 *  <li> remote call actions
 *  <li> wait actions
 *  </ul>
 * invocations using the debugger expression evaluation engine.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class DebuggerRuntime {

    private static final String EVALUATOR_STRAND_NAME = "evaluator-strand";
    private static final String XML_STEP_SEPARATOR = "/";
    private static final String XML_ALL_CHILDREN_STEP = "\\*";
    private static final String XML_DESCENDANT_STEP_PREFIX = "**";
    private static final String XML_NAME_PATTERN_SEPARATOR = "\\|";
    private static final String MODULE_INIT_CLASS_NAME = "$_init";
    private static final String CONFIGURE_INIT_CLASS_NAME = "$configurationMapper";
    private static final String MODULE_INIT_METHOD_NAME = "$moduleInit";
    private static final String MODULE_START_METHOD_NAME = "$moduleStart";
    private static final String CONFIGURE_INIT_METHOD_NAME = "$configureInit";

    /**
     * Invokes Ballerina object methods in blocking manner.
     *
     * @param bObject    ballerina object instance
     * @param methodName name of the object method to be invoked
     * @param args       object method arguments
     * @return return values
     */
    public static Object invokeObjectMethod(BObject bObject, String methodName, Object... args) {
        try {
            Scheduler scheduler = new Scheduler(1, false);
            CountDownLatch latch = new CountDownLatch(1);
            final Object[] finalResult = new Object[1];
            final Object[] paramValues = args[0] instanceof Strand ? Arrays.copyOfRange(args, 1, args.length) : args;

            Function<?, ?> func = o -> bObject.call((Strand) (((Object[]) o)[0]), methodName, paramValues);
            Object resultFuture = scheduler.schedule(new Object[1], func, null, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    latch.countDown();
                    finalResult[0] = result;
                }

                @Override
                public void notifyFailure(BError error) {
                    latch.countDown();
                    finalResult[0] = error;
                }
            }, EVALUATOR_STRAND_NAME, null).result;

            scheduler.start();
            latch.await();
            return finalResult[0];
        } catch (Exception e) {
            throw new BallerinaException("invocation failed: " + e.getMessage());
        }
    }

    /**
     * Invoke Ballerina functions in blocking manner.
     *
     * @param classLoader normal classLoader
     * @param className   which the function resides/ or file name
     * @param methodName  to be invokable unit
     * @param paramValues to be passed to invokable unit
     * @return return values
     */
    public static Object invokeFunction(ClassLoader classLoader, Scheduler scheduler, String className,
                                        String methodName, Object... paramValues) {
        try {
            if (scheduler == null) {
                scheduler = new Scheduler(1, false);
            }
            Class<?> clazz = classLoader.loadClass(className);
            Method method = getMethod(methodName, clazz);

            Function<Object[], Object> func = args -> {
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BallerinaException("'" + methodName + "' function invocation failed: " + e.getMessage());
                }
            };

            final Object[] finalResult = new Object[1];
            CountDownLatch latch = new CountDownLatch(1);
            BFuture futureValue = scheduler.schedule(paramValues, func, null, new Callback() {
                @Override
                public void notifySuccess(Object result) {
                    latch.countDown();
                    finalResult[0] = result;
                }

                @Override
                public void notifyFailure(BError error) {
                    latch.countDown();
                    finalResult[0] = error;
                }
            }, new HashMap<>(), PredefinedTypes.TYPE_NULL, EVALUATOR_STRAND_NAME, null);

            scheduler.start();
            latch.await();
            return finalResult[0];
        } catch (Exception e) {
            throw new BallerinaException("'" + methodName + "' function invocation failed: " + e.getMessage());
        }
    }

    /**
     * Creates and returns a new ballerina object instance.
     *
     * @param pkgOrg         org name of the module
     * @param pkgName        package name of the module
     * @param pkgVersion     package version of the module
     * @param objectTypeName type name of the class
     * @param fieldValues    field values
     * @return Ballerina object instance
     */
    public static Object createObjectValue(String pkgOrg, String pkgName, String pkgVersion, String objectTypeName,
                                           Object... fieldValues) {
        Module packageId = new Module(pkgOrg, pkgName, pkgVersion);
        return ValueCreator.createObjectValue(packageId, objectTypeName, fieldValues);
    }

    /**
     * Initializes and returns a Ballerina array instance with a given element type and the list of members.
     *
     * @param arrayElementType element type
     * @param values           member values
     * @return Ballerina array instance
     */
    public static Object getRestArgArray(Type arrayElementType, BValue... values) {
        ArrayType arrayType = TypeCreator.createArrayType(arrayElementType);
        if (values.length == 0) {
            return ValueCreator.createArrayValue(arrayType);
        } else if (values.length == 1) {
            if (values[0].getType().equals(arrayType)) {
                return values[0];
            }
        }
        return ValueCreator.createArrayValue(values, arrayType);
    }

    /**
     * Creates an error with given message, cause and details.
     *
     * @param message error message
     * @param cause   cause for the error
     * @param details error details
     * @return new error
     */
    public static Object createErrorValue(Object message, Object cause, Object... details) {
        if (!(message instanceof BString)) {
            return "incompatible types: expected 'string', found '" + getBTypeName(message) + "' for error message";
        } else if (cause != null && !(cause instanceof BError)) {
            return "incompatible types: expected 'error?', found '" + getBTypeName(cause) + "' for error cause";
        }

        List<BMapInitialValueEntry> errorDetailEntries = new ArrayList<>();
        for (int i = 0; i < details.length; i += 2) {
            errorDetailEntries.add(ValueCreator.createKeyFieldEntry(details[i], details[i + 1]));
        }

        ErrorType bErrorType = createErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage());
        BMap<BString, Object> errorDetailsMap = ValueCreator.createMapValue((MapType) PredefinedTypes.TYPE_ERROR_DETAIL,
                errorDetailEntries.toArray(errorDetailEntries.toArray(new BMapInitialValueEntry[0])));
        return ErrorCreator.createError(bErrorType, (StringValue) message, (ErrorValue) cause, errorDetailsMap);
    }

    /**
     * Returns the annotation value with the given name, w.r.t. a given typedesc value.
     *
     * @param typedescValue  typedesc value
     * @param annotationName name of the annotation
     * @return annotation value with the given name
     */
    public static Object getAnnotationValue(Object typedescValue, String annotationName) {
        if (!(typedescValue instanceof TypedescValue)) {
            return ErrorCreator.createError(StringUtils.fromString("Incompatible types: expected 'typedesc`, " +
                    "found '" + typedescValue.toString() + "'."));
        }
        Type type = ((TypedescValue) typedescValue).getDescribingType();
        if (type instanceof BAnnotatableType) {
            return ((BAnnotatableType) type).getAnnotations().entrySet()
                    .stream()
                    .filter(annotationEntry -> annotationEntry.getKey().getValue().endsWith(annotationName))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(null);
        }

        return ErrorCreator.createError(StringUtils.fromString("type: '" + TypeUtils.getType(type.getEmptyValue())
                + "' does not support annotation access."));
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

    private static String getBTypeName(Object value) {
        if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Integer || value instanceof Long) {
            return "int";
        } else if (value instanceof Float || value instanceof Double) {
            return "float";
        } else if (value instanceof BValue) {
            return ((BValue) value).getType().getName();
        } else {
            return "unknown";
        }
    }

    /**
     * Returns an array of extracted children elements from a given {@link BXmlSequence} with a given range.
     *
     * @param xmlSequence parent XML sequence
     * @param start       start index of the children range
     * @param count       children count that needs to be extracted
     * @return child variable array (or any exceptions, otherwise).
     */
    public static Object getXmlChildrenInRange(BXmlSequence xmlSequence, int start, int count) {
        try {
            if (count > 0) {
                return xmlSequence.getChildrenList().subList(start, start + count).toArray(new BXml[0]);
            } else {
                return xmlSequence.getChildrenList().toArray(new BXml[0]);
            }
        } catch (Exception e) {
            return e;
        }
    }

    /**
     * Performs and returns the result of XML filter expression.
     *
     * @param xmlVal              parent XML value
     * @param xmlPatternChainList filter expression pattern
     * @return the result of XML filter expression
     */
    public static BXml getXMLFilterResult(BXml xmlVal, BString... xmlPatternChainList) {
        return GetElements.getElements(xmlVal, xmlPatternChainList);
    }

    /**
     * Performs and returns the result of XML step expression.
     *
     * @param xmlVal         parent XML value
     * @param xmlStepPattern step expression pattern
     * @return the result of XML step expression
     */
    public static Object getXMLStepResult(BXml xmlVal, String xmlStepPattern) {
        try {
            if (xmlStepPattern.startsWith(XML_STEP_SEPARATOR)) {
                xmlStepPattern = xmlStepPattern.substring(1);
            }

            BString[] elementNames;
            if (xmlStepPattern.equals(XML_ALL_CHILDREN_STEP)) {
                elementNames = new BString[]{StringUtils.fromString(xmlStepPattern)};
                return GetFilteredChildrenFlat.getFilteredChildrenFlat(xmlVal, -1, elementNames);
            } else if (xmlStepPattern.startsWith(XML_DESCENDANT_STEP_PREFIX)) {
                String[] namePatternParts = xmlStepPattern.split(XML_STEP_SEPARATOR);
                xmlStepPattern = namePatternParts[namePatternParts.length - 1];
                elementNames = processXMLNamePattern(xmlStepPattern);
                return SelectDescendants.selectDescendants(xmlVal, elementNames);
            } else {
                elementNames = processXMLNamePattern(xmlStepPattern);
                return GetFilteredChildrenFlat.getFilteredChildrenFlat(xmlVal, -1, elementNames);
            }
        } catch (Exception e) {
            return ErrorCreator.createError(StringUtils.fromString(e.getMessage()));
        }
    }

    private static BString[] processXMLNamePattern(String xmlNamePattern) {
        // removes LT and GT tokens if presents.
        xmlNamePattern = xmlNamePattern.replaceAll("<", "").replaceAll(">", "");

        if (xmlNamePattern.contains(XML_STEP_SEPARATOR)) {
            String[] stepParts = xmlNamePattern.split(XML_ALL_CHILDREN_STEP);
            xmlNamePattern = stepParts[stepParts.length - 1];
        }

        return Arrays.stream(xmlNamePattern.split(XML_NAME_PATTERN_SEPARATOR))
                .map(entry -> StringUtils.fromString(entry.trim()))
                .toArray(BString[]::new);
    }

    /**
     * Invoke the function and return the result by classloading a given Ballerina executable jar.
     *
     * @param executablePath path of the jar to be classloaded
     * @param mainClass      main class name
     * @param functionName   name of the function to be executed
     * @param argValues      argument values
     * @return result of the function invocation
     */
    public static Object classloadAndInvokeFunction(String executablePath, String mainClass, String functionName,
                                                    Object... argValues) {
        try {
            URL pathUrl = Paths.get(executablePath).toUri().toURL();
            URLClassLoader classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () ->
                    new URLClassLoader(new URL[]{pathUrl}, ClassLoader.getSystemClassLoader()));

            List<Object> generatedArgs = new ArrayList<>();
            generatedArgs.add(null);
            for (Object arg : argValues) {
                generatedArgs.add(arg);
                // since the generated functions will contain an additional boolean flag for each parameter (to indicate
                // whether its an user provided arg), need to inject additional boolean values.
                // Todo - remove once https://github.com/ballerina-platform/ballerina-lang/pull/31589 is merged.
                generatedArgs.add(true);
            }

            // Derives the namespace of the generated classes.
            String[] mainClassNameParts = mainClass.split("\\.");
            String packageOrg = mainClassNameParts[0];
            String packageName = mainClassNameParts[1];
            String packageVersion = mainClassNameParts[2];
            String packageNameSpace = String.join(".", packageOrg, packageName, packageVersion);

            // Initialize a new scheduler
            Scheduler scheduler = new Scheduler(1, false);
            // Initialize configurations
            TomlDetails configurationDetails = LaunchUtils.getConfigurationDetails();
            invokeMethodDirectly(classLoader, String.join(".", packageNameSpace, CONFIGURE_INIT_CLASS_NAME),
                    CONFIGURE_INIT_METHOD_NAME, new Class[]{String[].class, Path[].class, String.class},
                    new Object[]{new String[]{}, configurationDetails.paths, configurationDetails.configContent});
            // Initialize the module
            invokeFunction(classLoader, scheduler, String.join(".", packageNameSpace, MODULE_INIT_CLASS_NAME),
                    MODULE_INIT_METHOD_NAME, new Object[1]);
            // Start the module
            invokeFunction(classLoader, scheduler, String.join(".", packageNameSpace, MODULE_INIT_CLASS_NAME),
                    MODULE_START_METHOD_NAME, new Object[1]);
            // Run the actual method
            return invokeFunction(classLoader, scheduler, mainClass, functionName, generatedArgs.toArray());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Invokes a method that is in the given class.
     * This is directly invoked without scheduling.
     * The method must be a static method accepting the given parameters.
     *
     * @param classLoader Class loader to find the class.
     * @param className   Class name with the method.
     * @param methodName  Method name to invoke.
     * @param argTypes    Types of arguments.
     * @param args        Arguments to provide.
     * @return The result of the invocation.
     */
    protected static Object invokeMethodDirectly(ClassLoader classLoader, String className, String methodName,
                                                 Class<?>[] argTypes, Object[] args) throws Exception {
        Class<?> clazz = classLoader.loadClass(className);
        Method method = clazz.getDeclaredMethod(methodName, argTypes);
        return method.invoke(null, args);
    }

    private DebuggerRuntime() {
    }
}
