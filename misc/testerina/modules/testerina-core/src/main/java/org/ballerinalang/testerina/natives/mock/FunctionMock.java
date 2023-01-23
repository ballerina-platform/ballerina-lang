package org.ballerinalang.testerina.natives.mock;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.values.MapValueImpl;
import org.ballerinalang.testerina.natives.Executor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.ballerinalang.test.runtime.BTestMain.getClassLoader;
import static org.ballerinalang.test.runtime.util.TesterinaUtils.getQualifiedClassName;
import static org.ballerinalang.testerina.natives.mock.MockConstants.MOCK_STRAND_NAME;
import static org.ballerinalang.testerina.natives.mock.MockConstants.ORIGINAL_FUNC_NAME_PREFIX;

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
        String[] mockFunctionClasses =
                mockFuncObj.getArrayValue(StringUtils.fromString("mockFunctionClasses")).getStringArray();

        String[] splitInfo = originalFunctionPackage.split(Pattern.quote("/"));
        String originalOrg = splitInfo[0];
        String originalPackage = splitInfo[1].split(Pattern.quote(":"))[0];
        String originalVersion = splitInfo[1].split(Pattern.quote(":"))[1];
        String originalClass = splitInfo[2];

        originalFunctionPackage = getQualifiedClassName(originalOrg, originalPackage, originalVersion, originalClass);
        Object returnVal = null;
        for (String caseId : caseIds) {
            if (MockRegistry.getInstance().hasCase(caseId)) {
                returnVal = MockRegistry.getInstance().getReturnValue(caseId);
                if (returnVal instanceof BString) {
                    if (returnVal.toString().contains(MockConstants.FUNCTION_CALL_PLACEHOLDER)) {
                        return callMockFunction(originalFunction, originalFunctionPackage,
                                mockFunctionClasses,
                                returnVal.toString(), args);
                    } else if (returnVal.toString().equals(MockConstants.FUNCTION_CALLORIGINAL_PLACEHOLDER)) {
                        return callOriginalFunction(ORIGINAL_FUNC_NAME_PREFIX + originalFunction,
                                originalFunctionPackage, args);
                    }
                }
                break;
            }
        }
        if (returnVal == null) {
            String message = "no return value or action registered for function";
            throw ErrorCreator.createError(
                    MockConstants.TEST_PACKAGE_ID,
                    MockConstants.FUNCTION_CALL_ERROR,
                    StringUtils.fromString(message),
                    null,
                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
        return returnVal;
    }

    private static Object callOriginalFunction(String originalFunction, String originalClassName, Object... args) {

        Strand strand = Scheduler.getStrand();
        String[] packageValues = originalClassName.split("\\.");

        String orgName = packageValues[0];
        String packageName = packageValues[1];
        String version = packageValues[2];

        List<Object> argsList = Arrays.asList(args);
        StrandMetadata metadata = new StrandMetadata(orgName, packageName, version, originalFunction);
        return Executor.executeFunction(strand.scheduler, MOCK_STRAND_NAME, metadata, getClassLoader(),
                originalClassName, originalFunction, argsList.toArray());
    }

    private static Object callMockFunction(String originalFunction, String originalClassName,
                                           String[] mockFunctionClasses, String returnVal, Object... args) {
        int prefixPos = returnVal.indexOf(MockConstants.FUNCTION_CALL_PLACEHOLDER);
        String mockFunctionName = returnVal.substring(prefixPos + MockConstants.FUNCTION_CALL_PLACEHOLDER.length());
        Strand strand = Scheduler.getStrand();

        String className;
        String orgName;
        String packageName;
        String version;

        if (Thread.currentThread().getStackTrace().length >= 5) {
            String classname = Thread.currentThread().getStackTrace()[4].getClassName();
            if (classname.contains("/")) {
                classname = classname.replaceAll("/", ".");
            }
            String[] projectInfo = classname.split(Pattern.quote("."));
            // Set project info
            try {
                orgName = projectInfo[0];
                packageName = projectInfo[1];
                version = projectInfo[2];
                className = "tests." + getMockClassName(orgName, packageName, version, originalFunction,
                        originalClassName, mockFunctionName, mockFunctionClasses, getClassLoader());
                className = getQualifiedClassName(orgName, packageName, version, className);
            } catch (ClassNotFoundException e) {
                return ErrorCreator.createError(
                        MockConstants.TEST_PACKAGE_ID,
                        MockConstants.FUNCTION_CALL_ERROR,
                        StringUtils.fromString(e.getMessage()),
                        null,
                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
            }

            List<Object> argsList = Arrays.asList(args);
            StrandMetadata metadata = new StrandMetadata(orgName, packageName, version, mockFunctionName);

            return Executor.executeFunction(
                    strand.scheduler, MOCK_STRAND_NAME, metadata, getClassLoader(), className, mockFunctionName,
                    argsList.toArray());
        } else {
            return null;
        }
    }

    private static String getMockClassName(String orgName, String packageName, String version,
                                       String originalMethodName, String originalPackageName, String mockMethodName,
                                       String[] mockFunctionClasses, ClassLoader classLoader)
            throws ClassNotFoundException {
        Method mockMethod = getMockMethod(orgName, packageName, version, mockMethodName, mockFunctionClasses,
                classLoader);
        Method originalMethod = getClassDeclaredMethod(originalPackageName, originalMethodName, classLoader);

        validateFunctionSignature(mockMethod, originalMethod, mockMethodName);
        return  mockMethod.getDeclaringClass().getSimpleName();
    }


    private static Method getMockMethod(String orgName, String packageName, String version, String mockMethodName,
                                        String[] mockFunctionClasses, ClassLoader classLoader)
            throws ClassNotFoundException {
        Method mockMethod = null;
        String resolvedMockClass = resolveMockClass(mockMethodName, mockFunctionClasses,
                orgName, packageName, version, classLoader);

        if (resolvedMockClass != null) {
            Method classDeclaredMethod = getClassDeclaredMethod(resolvedMockClass, mockMethodName, classLoader);
            if (classDeclaredMethod != null) {
                mockMethod = classDeclaredMethod;
            }
        }

        return mockMethod;
    }
    //Identify the class with the mockMethod(method within call)
    private static String resolveMockClass(String mockMethodName, String[] mockFunctionClasses, String orgName,
                                           String packageName, String version, ClassLoader classLoader)
            throws ClassNotFoundException {
        String mockClass = null;
        for (String clazz : mockFunctionClasses) {
            clazz = orgName + "." + packageName + "." + version + "." + clazz;
            Class<?> resolvedClass = classLoader.loadClass(clazz);
            for (Method method : resolvedClass.getDeclaredMethods()) {
                if (mockMethodName.equals(method.getName())) {
                    mockClass = clazz;
                }
            }
        }

        return mockClass;
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
                throw ErrorCreator.createError(
                        MockConstants.TEST_PACKAGE_ID,
                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                        StringUtils.fromString("Return type of function " + mockMethod.getName() +
                                " does not match function" + originalMethod.getName()),
                        null,
                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
            }

            // Validate if param number is the same
            if (mockMethodParameters.length != originalMethodParameters.length) {
                throw ErrorCreator.createError(
                        MockConstants.TEST_PACKAGE_ID,
                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                        StringUtils.fromString(
                                "Parameter types of function " + mockMethod.getName() +
                                        " does not match function" + originalMethod.getName()),
                        null,
                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
            }

            // Validate each param
            for (int i = 0; i < mockMethodParameters.length; i++) {
                if (mockMethodParameters [i] != originalMethodParameters[i]) {
                    throw ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(
                                    "Parameter types of function " + mockMethod.getName() +
                                            "does not match function" + originalMethod.getName()), null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
            }

        } else {
            throw ErrorCreator.createError(
                    MockConstants.TEST_PACKAGE_ID,
                    MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                    StringUtils.fromString("Mock function '" + mockMethodName + "' " +
                            "cannot be found"),
                    null,
                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
    }

    private static Method getClassDeclaredMethod(String className, String methodName, ClassLoader classLoader)
            throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(className);

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
}

