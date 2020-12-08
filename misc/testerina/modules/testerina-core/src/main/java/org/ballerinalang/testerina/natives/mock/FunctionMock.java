package org.ballerinalang.testerina.natives.mock;

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
import org.ballerinalang.testerina.natives.Executor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.ballerinalang.test.runtime.util.TesterinaUtils.getQualifiedClassName;
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

    private static Object callOriginal(String originalFunction, String originalClassName, Object... args) {

        Strand strand = Scheduler.getStrand();
        ClassLoader classLoader = FunctionMock.class.getClassLoader();
        String[] packageValues = originalClassName.split("\\.");

        String orgName = packageValues[0];
        String packageName = packageValues[1];
        String version = packageValues[2];

        List<Object> argsList = Arrays.asList(args);
        StrandMetadata metadata = new StrandMetadata(orgName, packageName, version, originalFunction);
        return Executor.executeFunction(strand.scheduler, MOCK_STRAND_NAME, metadata, classLoader,
                originalClassName, originalFunction, argsList.toArray());
    }

    private static Object callFunction(String originalFunction, String originalClassName, String returnVal,
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
            version = projectInfo[2];
            className = "tests." + getClassName(methodName, orgName, packageName, version, originalFunction,
                    originalClassName);
            className = getQualifiedClassName(orgName, packageName, version, className);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return ErrorCreator.createDistinctError(MockConstants.FUNCTION_CALL_ERROR, MockConstants.TEST_PACKAGE_ID,
                                                    StringUtils.fromString(e.getMessage()));
        }

        List<Object> argsList = Arrays.asList(args);
        ClassLoader classLoader = FunctionMock.class.getClassLoader();
        StrandMetadata metadata = new StrandMetadata(orgName, packageName, version, methodName);
        return Executor.executeFunction(
                strand.scheduler, MOCK_STRAND_NAME, metadata, classLoader, className, methodName, argsList.toArray());
    }

    private static String getClassName(String mockMethodName, String orgName, String packageName, String version,
                                       String originalMethodName, String originalPackageName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        // Get the list of classes in classLoader and find the class that has the mock method
        Field f = ClassLoader.class.getDeclaredField("classes");
        f.setAccessible(true);
        Vector<Class> classes =  (Vector<Class>) f.get(ClassLoader.getSystemClassLoader());
        List<Class> classList = classes.stream().filter(aClass ->
                aClass.getName().contains(orgName + "." + packageName)
                        && aClass.getName().contains(version)
                        && !aClass.getName().contains("$_init"))
                .collect(Collectors.toList());

        Method mockMethod = null;
        Method originalMethod;

        for (var clz : classList) {
            Method classDeclaredMethod = getClassDeclaredMethod(clz.getName(), mockMethodName);
            if (classDeclaredMethod != null) {
                mockMethod = classDeclaredMethod;
            }
        }

        originalMethod = getOriginalMethod(originalMethodName, originalPackageName);

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

    private static Method getClassDeclaredMethod(String className, String methodName) throws ClassNotFoundException {
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
}

