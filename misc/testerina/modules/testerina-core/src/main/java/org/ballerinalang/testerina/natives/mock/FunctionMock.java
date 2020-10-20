package org.ballerinalang.testerina.natives.mock;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.testerina.natives.Executor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import static org.ballerinalang.testerina.natives.mock.MockConstants.MOCK_STRAND_NAME;

/**
 * Class that contains inter-op function related to function mocking.
 */
public class FunctionMock {

    public static BError thenReturn(BObject caseObj) {
        BObject mockFunctionObj = caseObj.getObjectValue(StringUtils.fromString("mockFuncObj"));
        BArray args = caseObj.getArrayValue(StringUtils.fromString("args"));
        Object returnVal = caseObj.get(StringUtils.fromString("returnValue"));
        MockRegistry.getInstance().registerCase(mockFunctionObj, null, args, returnVal);
        return null;
    }

    public static Object mockHandler(BObject mockFuncObj, Object... args) {
        List<String> caseIds = getCaseIds(mockFuncObj, args);
        String originalFunction =
                mockFuncObj.getStringValue(StringUtils.fromString("functionToMock")).toString();
        String originalFunctionPackage =
                mockFuncObj.getStringValue(StringUtils.fromString("functionToMockPackage")).toString();
        originalFunctionPackage = formatFunctionPackage(originalFunctionPackage);
        Object returnVal = null;
        for (String caseId : caseIds) {
            if (MockRegistry.getInstance().hasCase(caseId)) {
                returnVal = MockRegistry.getInstance().getReturnValue(caseId);
                if (returnVal instanceof BString) {
                    if (returnVal.toString().contains(MockConstants.FUNCTION_CALL_PLACEHOLDER)) {
                        return callFunction(originalFunction, originalFunctionPackage, returnVal.toString(), args);
                    } else if (returnVal.toString().equals(MockConstants.FUNCTION_CALLORIGINAL_PLACEHOLDER)) {
                        return callOriginal(originalFunction, originalFunctionPackage, args);
                    }
                }
                break;
            }
        }
        if (returnVal == null) {
            String detail = "no return value or action registered for function";
            return ErrorCreator.createDistinctError(MockConstants.FUNCTION_CALL_ERROR, MockConstants.TEST_PACKAGE_ID,
                                                    StringUtils.fromString(detail));
        }
        return returnVal;
    }

    private static Object callOriginal(String originalFunction, String originalFunctionPackage, Object... args) {

        Strand strand = Scheduler.getStrand();
        ClassLoader classLoader = FunctionMock.class.getClassLoader();
        String[] packageValues = originalFunctionPackage.split("\\.");

        String orgName = packageValues[0];
        String packageName = packageValues[1];
        String version = packageValues[2];
        String className = packageValues[3];

        List<Object> argsList = Arrays.asList(args);
        StrandMetadata metadata = new StrandMetadata(orgName, packageName, version, originalFunction);
        return Executor.executeFunction(strand.scheduler, MOCK_STRAND_NAME, metadata, classLoader, orgName,
                                        packageName, version, className, originalFunction, argsList.toArray());
    }

    private static Object callFunction(String originalFunction, String originalFunctionPackage, String returnVal,
                                       Object... args) {
        int prefixPos = returnVal.indexOf(MockConstants.FUNCTION_CALL_PLACEHOLDER);
        String methodName = returnVal.substring(prefixPos + MockConstants.FUNCTION_CALL_PLACEHOLDER.length());
        Strand strand = Scheduler.getStrand();

        String className;
        String orgName;
        String packageName;
        String version;

        // Set project info
        try {
            String[] projectInfo = Thread.currentThread().getStackTrace()[4].getClassName().split(Pattern.quote("."));
            orgName = projectInfo[0];
            packageName = projectInfo[1];
            version = projectInfo[2].replace("_", ".");
            className = "tests." + getClassName(methodName, orgName, packageName, version, originalFunction,
                    originalFunctionPackage);
        } catch (IOException | ClassNotFoundException e) {
            return ErrorCreator.createDistinctError(MockConstants.FUNCTION_CALL_ERROR, MockConstants.TEST_PACKAGE_ID,
                                                    StringUtils.fromString(e.getMessage()));
        }

        List<Object> argsList = Arrays.asList(args);
        ClassLoader classLoader = FunctionMock.class.getClassLoader();
        StrandMetadata metadata = new StrandMetadata(orgName, packageName, version, methodName);
        return Executor.executeFunction(strand.scheduler, MOCK_STRAND_NAME, metadata, classLoader, orgName,
                                        packageName, version, className, methodName, argsList.toArray());
    }

    private static String getClassName(String mockMethodName, String orgName, String packageName, String version,
                                       String originalMethodName, String originalPackageName)
            throws IOException, ClassNotFoundException {
        String jarName = orgName + "-" + packageName + "-" + version + "-testable.jar";
        Path jarPath = Paths.get(System.getProperty("user.dir"), "target", "caches", "jar_cache", orgName,
                packageName, version, jarName);

        Method mockMethod = null;
        Method originalMethod;

        // Get the mock method
        try (JarFile jar = new JarFile(jarPath.toString())) {
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
                String file = entries.nextElement().getName();
                // Get .class files but dont contain '..Frame.class'
                if (file.endsWith(".class") && !file.contains("Frame.class") && !file.contains("__init")
                        && file.contains("/tests/") && mockMethod == null) {
                    mockMethod = getClassDeclaredMethod(file, mockMethodName);
                }
            }
            originalMethod = getOriginalMethod(originalMethodName, originalPackageName);
        }

        validateFunctionSignature(mockMethod, originalMethod, mockMethodName);
        return  mockMethod.getDeclaringClass().getSimpleName();
    }

    private static Method getOriginalMethod(String methodName, String packageName) throws ClassNotFoundException {
        Method[] methodList = FunctionMock.class.getClassLoader().loadClass(packageName).getDeclaredMethods();

        for (Method method : methodList) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        return null;
    }

    private static void validateFunctionSignature(Method mockMethod, Method originalMethod, String mockMethodName) {
        // Validation
        if (mockMethod != null && originalMethod != null) {
            // Methods type and parameters
            Class<?> mockMethodType = mockMethod.getReturnType();
            Class<?>[] mockMethodParameters = mockMethod.getParameterTypes();
            Class<?> originalMethodType = originalMethod.getReturnType();
            Class<?>[] originalMethodParameters = originalMethod.getParameterTypes();

            // Validate Return types
            if (mockMethodType != originalMethodType) {
                throw ErrorCreator.createDistinctError(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                                       MockConstants.TEST_PACKAGE_ID,
                                                       StringUtils.fromString("Return Type of function " +
                                                                                       mockMethod.getName() +
                                                                                       " does not match function" +
                                                                                       originalMethod.getName()));
            }

            // Validate if param number is the same
            if (mockMethodParameters.length != originalMethodParameters.length) {
                throw ErrorCreator.createDistinctError(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                                       MockConstants.TEST_PACKAGE_ID, StringUtils.fromString(
                                "Parameter types of function " + mockMethod.getName() +
                                        "does not match function" + originalMethod.getName()));
            }

            // Validate each param
            for (int i = 0; i < mockMethodParameters.length; i++) {
                if (mockMethodParameters [i] != originalMethodParameters[i]) {
                    throw ErrorCreator.createDistinctError(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                                           MockConstants.TEST_PACKAGE_ID, StringUtils.fromString(
                                    "Parameter types of function " + mockMethod.getName() +
                                            "does not match function" + originalMethod.getName()));
                }
            }

        } else {
            throw ErrorCreator.createDistinctError(MockConstants.FUNCTION_NOT_FOUND_ERROR,
                                                   MockConstants.TEST_PACKAGE_ID,
                                                   StringUtils.fromString(
                                                            "Mock function \'" + mockMethodName + "\' " +
                                                                    "cannot be found"));
        }
    }

    private static Method getClassDeclaredMethod(String file, String methodName) throws ClassNotFoundException {
        String className = file.replace('/', '.').substring(0, file.length() - 6);
        Class<?> clazz = Class.forName(className);

        for (Method method : clazz.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }

        return null;
    }

    private static List<String> getCaseIds(BObject mockObj, Object... args) {
        List<String> caseIdList = new ArrayList<>();
        StringBuilder caseId = new StringBuilder();

        // add case for function without args
        caseId.append(mockObj.hashCode());
        caseIdList.add(caseId.toString());

        // add case for function with ANY specified for objects and records
        for (Object arg: args) {
            caseId.append("-");
            if (arg instanceof BObject || arg instanceof RecordType) {
                caseId.append(MockRegistry.ANY);
            } else {
                caseId.append(arg);
            }
        }

        // skip if entry exists in list
        if (!caseIdList.contains(caseId.toString())) {
            caseIdList.add(caseId.toString());
        }
        // reversing the list to prioritize cases that have arguments specified
        Collections.reverse(caseIdList);

        return caseIdList;
    }

    private static String formatFunctionPackage(String fnPackage) {
        fnPackage = fnPackage.replace('.', '_');
        fnPackage = fnPackage.replace('/', '.');
        fnPackage = fnPackage.replace(':', '.');

        return fnPackage;
    }
}

