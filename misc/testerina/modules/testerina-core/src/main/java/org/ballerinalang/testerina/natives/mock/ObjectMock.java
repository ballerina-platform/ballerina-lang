/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.natives.mock;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.ParameterizedType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BClientType;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ObjectValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.TypeChecker.getType;

/**
 * Class that contains inter-op function related to mocking.
 */
public class ObjectMock {

    private ObjectMock() {
    }

    /**
     * Returns a generic mock object pretending to be the type of the provided typedesc.
     *
     * @param bTypedesc typedesc of the mock obejct
     * @param objectValue mock object to impersonate the type
     * @return mock object of provided type
     */
    public static BObject mock(BTypedesc bTypedesc, ObjectValue objectValue) {
        ObjectType objectValueType = (ObjectType) TypeUtils.getImpliedType(objectValue.getType());
        if (!objectValueType.getName().contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            // handle user-defined mock object
            if (objectValueType.getMethods().length == 0 &&
                    objectValueType.getFields().isEmpty() &&
                    (objectValueType instanceof BClientType bClientType &&
                            bClientType.getResourceMethods().length == 0)) {
                String detail = "mock object type '" + objectValueType.getName()
                        + "' should have at least one member function or field declared.";
                throw ErrorCreator.createError(
                        MockConstants.TEST_PACKAGE_ID,
                        MockConstants.INVALID_MOCK_OBJECT_ERROR,
                        StringUtils.fromString(detail),
                        null,
                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
            } else {
                for (MethodType attachedFunction : objectValueType.getMethods()) {
                    BError error = validateFunctionSignatures(attachedFunction,
                            ((ObjectType) bTypedesc.getDescribingType()).getMethods());
                    if (error != null) {
                        throw  error;
                    }
                }
                if (objectValueType instanceof BClientType bClientType) {
                    for (MethodType attachedFunction : bClientType.getResourceMethods()) {
                        BError error = validateFunctionSignatures(attachedFunction,
                                ((BClientType) bTypedesc.getDescribingType()).getResourceMethods());
                        if (error != null) {
                            throw  error;
                        }
                    }
                }
                for (Map.Entry<String, Field> field : objectValueType.getFields().entrySet()) {
                    BError error = validateField(field,
                            ((ObjectType) bTypedesc.getDescribingType()).getFields());
                    if (error != null) {
                        throw  error;
                    }
                }
            }
        }
        // handle default mock generation
        ObjectType bObjectType = (ObjectType) bTypedesc.getDescribingType();
        return new GenericMockObjectValue(bObjectType, objectValue);
    }

    /**
     * Validates the object provided to register mock cases.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError validatePreparedObj(BObject caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj;
        BObject mockObj = genericMock.getMockObj();
        String objectType = mockObj.getType().getName();
        if (!objectType.contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            String detail = "cases cannot be registered to user-defined object type '"
                    + genericMock.getType().getName() + "'";
            return ErrorCreator.createDistinctError(
                    MockConstants.INVALID_MOCK_OBJECT_ERROR, MockConstants.TEST_PACKAGE_ID,
                    StringUtils.fromString(detail));
        }
        return null;
    }

    /**
     * Validates the member function name provided to register mock cases.
     *
     * @param functionName function name provided
     * @param mockObject ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError validateFunctionName(String functionName, BObject mockObject) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) mockObject;
        if (!validateFunctionName(functionName,
                ((ObjectType) TypeUtils.getImpliedType(genericMock.getType())).getMethods())) {
            String detail = "invalid function name '" + functionName + " ' provided";
            throw ErrorCreator.createError(
                    MockConstants.TEST_PACKAGE_ID,
                    MockConstants.FUNCTION_NOT_FOUND_ERROR,
                    StringUtils.fromString(detail),
                    null,
                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
        return null;
    }

    private static void validateClientObject(GenericMockObjectValue genericMock) {
        if (!(genericMock.getType() instanceof BClientType)) {
            String detail = "mock object type should be a client type";
            throw ErrorCreator.createError(
                    MockConstants.TEST_PACKAGE_ID,
                    MockConstants.INVALID_MOCK_OBJECT_ERROR,
                    StringUtils.fromString(detail),
                    null,
                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
    }

    /**
     * Validates the member resource function name provided to register mock cases.
     *
     * @param pathName function name provided
     * @param mockObject ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError validateResourcePath(String pathName, BObject mockObject) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) mockObject;
        if (!validateResourcePath(pathName,
                ((BClientType) genericMock.getType()).getResourceMethods())) {
            String detail = "invalid resource path '" + pathName + "' provided";
            throw ErrorCreator.createError(
                    MockConstants.TEST_PACKAGE_ID,
                    MockConstants.FUNCTION_NOT_FOUND_ERROR,
                    StringUtils.fromString(detail),
                    null,
                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
        return null;
    }

    /**
     * Validates the member resource method .
     *
     * @param caseObj ballerina MemberResourceFunctionStub object containing information about the mock
     * @return an optional error if a validation fails
     */
    public static BError validateResourceMethod(BObject caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(
                StringUtils.fromString(MockConstants.MOCK_OBJECT));
        String functionName = caseObj.getStringValue(StringUtils.fromString(MockConstants.FUNCTION_NAME)).toString();
        String accessor = caseObj.getStringValue(StringUtils.fromString(MockConstants.ACCESSOR)).toString();
        if (!validateAccessor(functionName, accessor,
                ((BClientType) genericMock.getType()).getResourceMethods())) {
            String detail = "invalid accessor method '" + accessor + "' provided";
            throw ErrorCreator.createError(
                    MockConstants.TEST_PACKAGE_ID,
                    MockConstants.FUNCTION_NOT_FOUND_ERROR,
                    StringUtils.fromString(detail),
                    null,
                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
        return null;
    }

    private static boolean validateAccessor(String resourcePath, String accessor, ResourceMethodType[]
            resourceMethods) {
        String functionPattern = getFunctionNameForResourcePath(resourcePath);
        for (ResourceMethodType attachedFunction : resourceMethods) {
            if (attachedFunction.getName().endsWith(functionPattern) &&
                    attachedFunction.getAccessor().equals(accessor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the member resource method path parameter .
     *
     * @param caseObj ballerina MemberResourceFunctionStub object containing information about the mock
     * @param pathParams path parameters map provided by the user
     * @return an optional error if a validation fails
     */
    public static BError validatePathParams(BObject caseObj, BMap pathParams) {
        String functionName = caseObj.getStringValue(StringUtils.fromString(MockConstants.FUNCTION_NAME)).toString();
        String[] pathSegments = functionName.split(MockConstants.PATH_SEPARATOR);
        for (int i = 0; i < pathSegments.length; i++) {
            if (pathSegments[i].startsWith(MockConstants.REST_PARAMETER_INDICATOR)) {
                if (i != pathSegments.length - 1) {
                    String detail = "rest parameter '" + pathSegments[i] + "' should be the last segment of the path";
                    throw ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
                String restSegment = pathSegments[i].substring(2);
                if (pathParams.get(StringUtils.fromString(restSegment)) == null) {
                    String detail = "required rest parameter '" + restSegment + "' is not provided";
                    throw ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
            } else if (pathSegments[i].startsWith(MockConstants.PATH_PARAM_INDICATOR)) {
                String pathSegment = pathSegments[i].substring(1);
                if (pathParams.get(StringUtils.fromString(pathSegment)) == null) {
                    String detail = "required path parameter '" + pathSegment + "' is not provided";
                    throw ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
            }
        }
        return null;
    }

    /**
     * Validates the member field name provided to register mock cases.
     *
     * @param fieldName field name provided
     * @param mockObject ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError validateFieldName(String fieldName, BObject mockObject) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) mockObject;
        if (!validateFieldName(fieldName,
                ((ObjectType) TypeUtils.getImpliedType(genericMock.getType())).getFields())) {
            String detail = "invalid field name '" + fieldName + "' provided";
            throw ErrorCreator.createError(
                    MockConstants.TEST_PACKAGE_ID,
                    MockConstants.INVALID_MEMBER_FIELD_ERROR,
                    StringUtils.fromString(detail),
                    null,
                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
        }
        return null;
    }

    /**
     * Validates the member field name provided to register mock cases.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError validateArguments(BObject caseObj) {
        String mockType = caseObj.getOriginalType().getName();
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(
                StringUtils.fromString(MockConstants.MOCK_OBJECT));
        String functionName = caseObj.getStringValue(StringUtils.fromString(MockConstants.FUNCTION_NAME)).toString();
        BArray argsList = caseObj.getArrayValue(StringUtils.fromString(MockConstants.ARGS));
        for (MethodType attachedFunction :
                ((ObjectType) TypeUtils.getImpliedType(genericMock.getType())).getMethods()) {
            if (attachedFunction.getName().equals(functionName)) {

                // validate the number of arguments provided
                if (argsList.size() > attachedFunction.getType().getParameters().length) {
                    String detail = "too many argument provided to mock the function '" + functionName + "()'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }

                // validate if each argument is compatible with the type given in the function signature
                int i = 0;
                for (BIterator it = argsList.getIterator(); it.hasNext(); i++) {
                    Type paramType = TypeUtils.getImpliedType(attachedFunction.getType().getParameters()[i].type);
                    if (paramType instanceof UnionType unionType) {
                        Object arg = it.next();
                        boolean isTypeAvailable = false;
                        List<Type> memberTypes = unionType.getMemberTypes();
                        for (Type memberType : memberTypes) {
                            if (TypeChecker.checkIsType(arg, memberType)) {
                                isTypeAvailable = true;
                                break;
                            }
                        }
                        if (!isTypeAvailable) {
                            String detail =
                                    "incorrect type of argument provided at position '" + (i + 1) +
                                            "' to mock the function '" + functionName + "()'";
                            return ErrorCreator.createError(
                                    MockConstants.TEST_PACKAGE_ID,
                                    MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                    StringUtils.fromString(detail),
                                    null,
                                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                        }
                    } else if (!TypeChecker.checkIsType(it.next(), paramType)) {
                        String detail =
                                "incorrect type of argument provided at position '" + (i + 1)
                                        + "' to mock the function '" + functionName + "()'";
                        return ErrorCreator.createError(
                                MockConstants.TEST_PACKAGE_ID,
                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                StringUtils.fromString(detail),
                                null,
                                new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * Validates the resource path arguments.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError validatePathArgs(BObject caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(
                StringUtils.fromString(MockConstants.MOCK_OBJECT));
        String functionName = caseObj.getStringValue(StringUtils.fromString(MockConstants.FUNCTION_NAME)).toString();
        String[] pathParamPlaceHolder = getPathParamPlaceHolders(functionName);
        BArray argsList = caseObj.getArrayValue(StringUtils.fromString(MockConstants.PATH_ARGS));
        String functionPattern = getFunctionNameForResourcePath(functionName);
        String accessor = caseObj.getStringValue(StringUtils.fromString(MockConstants.ACCESSOR)).toString();
        functionPattern = MockConstants.RESOURCE_SEPARATOR + accessor +
                MockConstants.RESOURCE_SEPARATOR + functionPattern;
        for (ResourceMethodType attachedFunction : ((BClientType) genericMock.getType()).getResourceMethods()) {
            if (attachedFunction.getName().endsWith(functionPattern)) {
                int pathSegmentCount = (int) functionPattern.chars().filter(ch -> ch ==
                        MockConstants.PATH_PARAM_PLACEHOLDER.charAt(0)).count() -
                        (functionPattern.endsWith(MockConstants.REST_PARAM_PLACEHOLDER) ? 1 : 0);
                // validate the number of arguments provided
                if (argsList.size() > pathSegmentCount) {
                    String detail = "too many argument provided to mock the function '" + functionName + "()'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }

                // validate if each argument is compatible with the type given in the function signature
                int counter = 0;
                for (BIterator bIterator = argsList.getIterator(); bIterator.hasNext(); counter++) {
                    String detail = "incorrect type of path provided for '" + pathParamPlaceHolder[counter] +
                                "' to mock the function '" + functionName;
                    Type paramType = TypeUtils.getImpliedType(attachedFunction.getType().getParameters()[counter].type);
                    if (paramType instanceof UnionType unionType) {
                        Object arg = bIterator.next();
                        boolean isTypeAvailable = false;
                        List<Type> memberTypes = unionType.getMemberTypes();
                        for (Type memberType : memberTypes) {
                            if (TypeChecker.checkIsType(arg, memberType)) {
                                isTypeAvailable = true;
                                break;
                            }
                        }
                        if (!isTypeAvailable) {
                            return ErrorCreator.createError(
                                    MockConstants.TEST_PACKAGE_ID,
                                    MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                    StringUtils.fromString(detail),
                                    null,
                                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                        }
                    } else if (paramType instanceof ArrayType) {
                        Object arg = bIterator.next();
                        if (!(TypeChecker.getType(arg) instanceof ArrayType)) {
                            return ErrorCreator.createError(
                                    MockConstants.TEST_PACKAGE_ID,
                                    MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                    StringUtils.fromString(detail),
                                    null,
                                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                        }
                    } else if (!TypeChecker.checkIsType(bIterator.next(), paramType)) {
                        return ErrorCreator.createError(
                                MockConstants.TEST_PACKAGE_ID,
                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                StringUtils.fromString(detail),
                                null,
                                new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * Validates the resource function arguments.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError validateResourceArguments(BObject caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(
                StringUtils.fromString(MockConstants.MOCK_OBJECT));
        String functionName = caseObj.getStringValue(StringUtils.fromString(MockConstants.FUNCTION_NAME)).toString();
        BArray argsList = caseObj.getArrayValue(StringUtils.fromString(MockConstants.ARGS));
        String functionPattern = getFunctionNameForResourcePath(functionName);
        String accessor = caseObj.getStringValue(StringUtils.fromString(MockConstants.ACCESSOR)).toString();
        functionPattern = MockConstants.RESOURCE_SEPARATOR + accessor +
                MockConstants.RESOURCE_SEPARATOR + functionPattern;
        for (ResourceMethodType attachedFunction : ((BClientType) genericMock.getType()).getResourceMethods()) {
            if (attachedFunction.getName().endsWith(functionPattern)) {
                int pathSegmentCount = (int) functionPattern.chars().filter(ch -> ch ==
                        MockConstants.PATH_PARAM_PLACEHOLDER.charAt(0)).count() -
                        (functionPattern.endsWith(MockConstants.REST_PARAM_PLACEHOLDER) ? 1 : 0);
                // validate the number of arguments provided
                if (argsList.size() > attachedFunction.getType().getParameters().length - pathSegmentCount) {
                    String detail = "too many argument provided to mock the function '" + functionName + "()'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }

                // validate if each argument is compatible with the type given in the function signature
                int counter = 0;
                for (BIterator bIterator = argsList.getIterator(); bIterator.hasNext(); counter++) {
                    String detail = "incorrect type of argument provided at position '" + (counter + 1) + "' " +
                            "to mock the function '" + functionName;
                    Type paramType = TypeUtils.getImpliedType(attachedFunction.getType()
                            .getParameters()[counter + pathSegmentCount].type);
                    if (paramType instanceof UnionType unionType) {
                        Object arg = bIterator.next();
                        boolean isTypeAvailable = false;
                        List<Type> memberTypes = unionType.getMemberTypes();
                        for (Type memberType : memberTypes) {
                            if (TypeChecker.checkIsType(arg, memberType)) {
                                isTypeAvailable = true;
                                break;
                            }
                        }
                        if (!isTypeAvailable) {
                            return ErrorCreator.createError(
                                    MockConstants.TEST_PACKAGE_ID,
                                    MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                    StringUtils.fromString(detail),
                                    null,
                                    new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                        }
                    } else if (!TypeChecker.checkIsType(bIterator.next(), paramType)) {
                        return ErrorCreator.createError(
                                MockConstants.TEST_PACKAGE_ID,
                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                StringUtils.fromString(detail),
                                null,
                                new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * Registers the return value to the case provided.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError thenReturn(BObject caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj
                .get(StringUtils.fromString(MockConstants.MOCK_OBJECT));
        BObject mockObj = genericMock.getMockObj();
        Object returnVal = caseObj.get(StringUtils.fromString(MockConstants.RETURN_VALUE));
        String mockType = caseObj.getOriginalType().getName();
        String functionName;
        try {
            functionName = caseObj.getStringValue(StringUtils.fromString(MockConstants.FUNCTION_NAME)).toString();
        } catch (Exception e) {
            if (!e.getMessage().contains("No such field: functionName")) {
                throw e;
            }
            functionName = null;
        }
        ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(genericMock.getType());
        if (functionName != null) {

            // register return value for member function
            BArray args = caseObj.getArrayValue(StringUtils.fromString(MockConstants.ARGS));
            if (MockConstants.MEMBER_RESOURCE_FUNCTION_STUB.equals(mockType)) {
                BArray pathArgs = caseObj.getArrayValue(StringUtils.fromString(MockConstants.PATH_ARGS));
                String functionPattern = getFunctionNameForResourcePath(functionName);
                String accessor = caseObj.getStringValue(StringUtils.fromString(MockConstants.ACCESSOR)).toString();
                String resourceFunctionPattern = MockConstants.RESOURCE_SEPARATOR + accessor +
                        MockConstants.RESOURCE_SEPARATOR + functionPattern;
                if (!validateReturnValueForResourcePath(resourceFunctionPattern, returnVal,
                        ((BClientType) objectType).getResourceMethods())) {
                    String detail =
                            "return value provided does not match the return type of the resource path '" +
                                    functionName + "'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
                resourceFunctionPattern = replacePathPlaceHolders(resourceFunctionPattern, pathArgs);
                MockRegistry.getInstance().registerCase(mockObj, resourceFunctionPattern, args, returnVal);
            } else {
                if (!validateReturnValue(functionName, returnVal, objectType.getMethods())) {
                    String detail =
                            "return value provided does not match the return type of function '" + functionName + "()'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
                MockRegistry.getInstance().registerCase(mockObj, functionName, args, returnVal);
            }
        } else {
            // register return value for member field
            String fieldName = caseObj.getStringValue(StringUtils.fromString("fieldName")).toString();
            if (!validateFieldAccessIsPublic(objectType, fieldName)) {
                String detail = "member field should be public to be mocked. " +
                        "The provided field '" + fieldName + "' is not public";
                return ErrorCreator.createError(StringUtils.fromString(MockConstants.NON_PUBLIC_MEMBER_FIELD_ERROR),
                        StringUtils.fromString(detail));
            }
            if (!validateFieldValue(returnVal, objectType.getFields().get(fieldName).getFieldType())) {
                String detail = "return value provided does not match the type of '" + fieldName + "'";
                return ErrorCreator.createError(
                        MockConstants.TEST_PACKAGE_ID,
                        MockConstants.INVALID_MEMBER_FIELD_ERROR,
                        StringUtils.fromString(detail),
                        null,
                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
            }
            MockRegistry.getInstance().registerCase(mockObj, fieldName, null, returnVal);
        }
        return null;
    }

    private static String replacePathPlaceHolders(String functionPattern, BArray pathArgs) {
        int caretCount = (int) functionPattern.chars().filter(ch -> ch == MockConstants.PATH_PARAM_PLACEHOLDER
                .charAt(0)).count();
        Object[] args = pathArgs.getValues();
        BArray restArgs = null;
        StringBuilder newFuncName = new StringBuilder(functionPattern);
        if (functionPattern.endsWith(MockConstants.REST_PARAM_PLACEHOLDER)) {
            restArgs = (BArray) args[caretCount - 2];
        }
        if (restArgs != null) {
            newFuncName.setLength(0);
            String substring = functionPattern.substring(0, functionPattern.length() - 2);
            newFuncName.append(substring);
            for (int i = 0; i < restArgs.size(); i++) {
                Object arg = restArgs.get(i);
                if (arg != null) {
                    newFuncName.append(arg).append(MockConstants.RESOURCE_SEPARATOR);
                }
            }
            newFuncName.setLength(newFuncName.length() - 1);
            caretCount -= 2;
        }
        for (int i = 0; i < caretCount; i++) {
            if (args[i] != null) {
                newFuncName.replace(newFuncName.indexOf(MockConstants.PATH_PARAM_PLACEHOLDER),
                        newFuncName.indexOf(MockConstants.PATH_PARAM_PLACEHOLDER) + 1,
                        args[i].toString());
            }
        }
        return newFuncName.toString();
    }

    private static boolean validateFieldAccessIsPublic(ObjectType objectType, String fieldName) {
        return SymbolFlags.isFlagOn(objectType.getFields().get(fieldName).getFlags(), SymbolFlags.PUBLIC);
    }

    /**
     * Registers a sequence of return values to the case provided.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError thenReturnSequence(BObject caseObj) {
        String mockType = caseObj.getOriginalType().getName();
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj
                .get(StringUtils.fromString(MockConstants.MOCK_OBJECT));
        ObjectType objectType = (ObjectType) TypeUtils.getImpliedType(genericMock.getType());
        BObject mockObj = genericMock.getMockObj();
        String functionName = caseObj.getStringValue(StringUtils.fromString(MockConstants.FUNCTION_NAME)).toString();
        BArray returnValueSequence = caseObj.getArrayValue(StringUtils.fromString(MockConstants.RETURN_VALUE_SEQ));
        for (int i = 0; i < returnValueSequence.getValues().length; i++) {
            Object returnValue = returnValueSequence.getValues()[i];
            if (returnValue == null) {
                break;
            }
            if (MockConstants.MEMBER_RESOURCE_FUNCTION_STUB.equals(mockType)) {
                String functionPattern = getFunctionNameForResourcePath(functionName);
                String accessor = caseObj.getStringValue(StringUtils.fromString(MockConstants.ACCESSOR)).toString();
                String resourceFunctionPattern = MockConstants.RESOURCE_SEPARATOR + accessor +
                        MockConstants.RESOURCE_SEPARATOR + functionPattern;
                if (!validateReturnValueForResourcePath(resourceFunctionPattern, returnValue,
                        ((BClientType) objectType).getResourceMethods())) {
                    String detail = "return value provided at position '" + i
                            + "' does not match the return type of the resource path '" + functionName + "'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
                MockRegistry.getInstance().registerCase(mockObj, resourceFunctionPattern, null, returnValue, i + 1);
            } else {
                if (!validateReturnValue(functionName, returnValue,
                        ((ObjectType) TypeUtils.getImpliedType(genericMock.getType())).getMethods())) {
                    String detail = "return value provided at position '" + i
                            + "' does not match the return type of function '" + functionName + "()'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
                MockRegistry.getInstance().registerCase(mockObj, functionName, null, returnValue, i + 1);
            }
        }
        return null;
    }

    /**
     * Validates the function name provided when a default mock object is used.
     *
     * @param functionName function name
     * @param attachedFunctions functions available in the mocked type
     * @return whether the function name is valid
     */
    private static boolean validateFunctionName(String functionName, MethodType[] attachedFunctions) {
        for (MethodType attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the resource function name provided when a default mock object is used.
     *
     * @param resourcePath function name
     * @param attachedFunctions functions available in the mocked type
     * @return whether the function name is valid
     */
    private static boolean validateResourcePath(String resourcePath, ResourceMethodType[] attachedFunctions) {
        String functionPattern = getFunctionNameForResourcePath(resourcePath);
        for (ResourceMethodType attachedFunction : attachedFunctions) {
            // Function name for a resource function contains the accessor and `^` representing path params
            // Each accessor, path is separated by $
            // e.g. For the resource function => get [string path2]/posts/[string path1]()
            // functionName => $get$^$posts$^
            if (attachedFunction.getName().endsWith(functionPattern)) {
                return true;
            }
        }
        return false;
    }

    private static String getFunctionNameForResourcePath(String path) {
        String[] components = path.split(MockConstants.PATH_SEPARATOR);
        for (int i = 0; i < components.length; i++) {
            if (components[i].startsWith(MockConstants.REST_PARAMETER_INDICATOR)) {
                components[i] = MockConstants.REST_PARAM_PLACEHOLDER;
            } else if (components[i].startsWith(MockConstants.PATH_PARAM_INDICATOR)) {
                components[i] = MockConstants.PATH_PARAM_PLACEHOLDER;
            }
        }
        return String.join(MockConstants.RESOURCE_SEPARATOR, components);
    }

    private static String[] getPathParamPlaceHolders(String path) {
        List<String> placeHolderList = new ArrayList<>();
        String[] components = path.split(MockConstants.PATH_SEPARATOR);
        for (String component : components) {
            if (component.startsWith(MockConstants.REST_PARAMETER_INDICATOR)) {
                placeHolderList.add(component.substring(2));
            } else if (component.startsWith(MockConstants.PATH_PARAM_INDICATOR)) {
                placeHolderList.add(component.substring(1));
            }
        }
        return placeHolderList.toArray(new String[0]);
    }

    /**
     * Validates the field name provided when a default mock object is used.
     *
     * @param fieldName field name provided
     * @param fieldMap fields available in the mocked type
     * @return whether the field name is valid
     */
    private static boolean validateFieldName(String fieldName, Map<String, Field> fieldMap) {
        for (Map.Entry<String, Field> field : fieldMap.entrySet()) {
            if (field.getKey().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean validateParameterizedValue(Object returnVal, ParameterizedType functionReturnType) {
        return TypeChecker.checkIsType(returnVal, functionReturnType.getParamValueType());
    }

    private static boolean validateUnionValue(Object returnVal, UnionType functionReturnType) {
        List<Type> memberTypes = functionReturnType.getMemberTypes();
        for (Type memberType : memberTypes) {
            if (TypeUtils.getImpliedType(memberType).getTag() == TypeTags.PARAMETERIZED_TYPE_TAG) {
                if (validateParameterizedValue(returnVal, ((ParameterizedType) memberType))) {
                    return true;
                }
            } else {
                if (TypeChecker.checkIsType(returnVal, memberType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validates the provided return value when the function has stream as return type.
     *
     * @param returnVal return value provided
     * @param streamType stream return type of the attached function
     * @return whether the return value is valid
     */
    private static boolean validateStreamValue(Object returnVal, StreamType streamType) {
        Type sourceType = TypeUtils.getImpliedType(getType(returnVal));
        if (sourceType.getTag() == TypeTags.STREAM_TAG) {
            Type targetConstrainedType = TypeUtils.getImpliedType(streamType.getConstrainedType());
            Type targetCompletionType = TypeUtils.getImpliedType(streamType.getCompletionType());
            // Update the target stream constrained type if it is a parameterized type
            if (targetConstrainedType.getTag() == TypeTags.PARAMETERIZED_TYPE_TAG) {
                targetConstrainedType = ((ParameterizedType) targetConstrainedType).getParamValueType();
            }
            // Update the target stream completion type if it is a parameterized type
            if (targetCompletionType.getTag() == TypeTags.PARAMETERIZED_TYPE_TAG) {
                targetCompletionType = ((ParameterizedType) targetCompletionType).getParamValueType();
            }
            // The type matches only if both constrained and completion types are matching
            return TypeChecker.checkIsType(((StreamType) sourceType).getConstrainedType(), targetConstrainedType) &&
                    TypeChecker.checkIsType(((StreamType) sourceType).getCompletionType(), targetCompletionType);
        } else {
            return false;
        }
    }

    /**
     * Validates the function return value provided when a default mock object is used.
     *
     * @param functionName      function to validate against
     * @param returnVal         return value provided
     * @param attachedFunctions functions available in the mocked type
     * @return whether the return value is valid
     */
    private static boolean validateReturnValue(
            String functionName, Object returnVal, MethodType[] attachedFunctions) {
        for (MethodType attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {
                return isValidReturnValue(returnVal, attachedFunction);
            }
        }
        return false;
    }

    private static boolean validateReturnValueForResourcePath(
            String functionPattern, Object returnVal, MethodType[] attachedFunctions) {
        for (MethodType attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().endsWith(functionPattern)) {
                return isValidReturnValue(returnVal, attachedFunction);
            }
        }
        return false;
    }

    private static boolean isValidReturnValue(Object returnVal, MethodType attachedFunction) {
        Type functionReturnType = TypeUtils.getImpliedType(
                attachedFunction.getType().getReturnParameterType());
        switch (functionReturnType.getTag()) {
            case TypeTags.UNION_TAG:
                return validateUnionValue(returnVal, (UnionType) functionReturnType);
            case TypeTags.STREAM_TAG:
                return validateStreamValue(returnVal, (StreamType) functionReturnType);
            case TypeTags.PARAMETERIZED_TYPE_TAG:
                return validateParameterizedValue(returnVal, (ParameterizedType) functionReturnType);
            default:
                return TypeChecker.checkIsType(returnVal, functionReturnType);
        }
    }

    /**
     * Validates the field return value provided when a default mock object is used.
     *
     * @param returnVal  return value provided
     * @param fieldType type of the field
     * @return whether the return value is valid
     */
    private static boolean validateFieldValue(Object returnVal, Type fieldType) {
        return TypeChecker.checkIsType(returnVal, fieldType);
    }

    /**
     * Validates the signature of the function provided when a user-defined mock object is used.
     *
     * @param func function defined in mock object
     * @param attachedFunctions functions available in the mocked type
     * @return whether the function signature is valid
     */
    private static BError validateFunctionSignatures(MethodType func,
                                                     MethodType[] attachedFunctions) {
        String functionName = func.getName();
        Parameter[] parameters = func.getParameters();
        Type returnType = func.getType().getReturnParameterType();

        for (MethodType attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {

                // validate that the number of parameters are equal
                if (parameters.length != attachedFunction.getParameters().length) {
                    String detail = "incorrect number of parameters provided for function '" + functionName + "()'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                } else {
                    // validate the equivalence of the parameter types
                    for (int i = 0; i < parameters.length; i++) {
                        Type paramTypeAttachedFunc =
                                TypeUtils.getImpliedType(attachedFunction.getType().getParameters()[i].type);
                        Type paramType = TypeUtils.getImpliedType(parameters[i].type);
                        if (paramTypeAttachedFunc instanceof UnionType unionParamTypeAttachedFunc) {
                            if (!(paramType instanceof UnionType unionParamType)) {
                                String detail = "incompatible parameter type provided at position " + (i + 1) + " in" +
                                        " function '" + functionName + "()'. parameter should be of union type ";
                                return ErrorCreator.createError(
                                        MockConstants.TEST_PACKAGE_ID,
                                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                        StringUtils.fromString(detail),
                                        null,
                                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                            } else {
                                Type[] memberTypes = unionParamTypeAttachedFunc.getMemberTypes().toArray(new Type[0]);
                                Type[] providedTypes = unionParamType.getMemberTypes().toArray(new Type[0]);
                                for (int j = 0; j < memberTypes.length; j++) {
                                    if (!TypeChecker.checkIsType(providedTypes[j], memberTypes[j])) {
                                        BString detail = StringUtils
                                                .fromString("incompatible parameter type provided at position "
                                                        + (i + 1) + " in function '" + functionName + "()'");
                                        return ErrorCreator.createError(
                                                MockConstants.TEST_PACKAGE_ID,
                                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                                detail,
                                                null,
                                                new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                                    }
                                }

                            }
                        } else {
                            if (!TypeChecker.checkIsType(paramType,
                                    paramTypeAttachedFunc)) {
                                BString detail =
                                        StringUtils.fromString("incompatible parameter type provided at position "
                                                + (i + 1) + " in function '" + functionName + "()'");
                                return ErrorCreator.createError(
                                        MockConstants.TEST_PACKAGE_ID,
                                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                        detail,
                                        null,
                                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                            }
                        }
                    }
                }

                // validate the equivalence of the return types
                if (!TypeChecker.checkIsType(attachedFunction.getType().getReturnParameterType(), returnType)) {
                    String detail = "incompatible return type provided for function '" + functionName + "()'";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
                return null;
            }
        }
        String detail = "invalid function '" + functionName + "' provided";
        return ErrorCreator.createError(
                MockConstants.TEST_PACKAGE_ID,
                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                StringUtils.fromString(detail),
                null,
                new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    /**
     * Validates the field provided when a user-defined mock object is used.
     *
     * @param mockField field defined in mock object
     * @param fieldMap fields available in the mocked type
     * @return whether the field declaration is valid
     */
    private static BError validateField(Map.Entry<String, Field> mockField, Map<String, Field> fieldMap) {
        for (Map.Entry<String, Field> field : fieldMap.entrySet()) {
            if (field.getKey().equals(mockField.getKey())) {
                // Ignore type checking if the field is private
                if (SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.PRIVATE) ||
                        TypeChecker.checkIsType(field.getValue().getFieldType(), mockField.getValue().getFieldType())) {
                    return null;
                } else {
                    String detail = "incompatible field type '" + mockField.getValue().getFieldType() + "' provided " +
                            "for field " + mockField.getKey() + "";
                    return ErrorCreator.createError(
                            MockConstants.TEST_PACKAGE_ID,
                            MockConstants.INVALID_MEMBER_FIELD_ERROR,
                            StringUtils.fromString(detail),
                            null,
                            new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                }
            }
        }
        String detail = "invalid field '" + mockField.getKey() + "' provided";
        return ErrorCreator.createError(
                MockConstants.TEST_PACKAGE_ID,
                MockConstants.INVALID_MEMBER_FIELD_ERROR,
                StringUtils.fromString(detail),
                null,
                new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }
}
