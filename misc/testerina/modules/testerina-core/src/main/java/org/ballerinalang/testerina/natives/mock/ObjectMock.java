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
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.ParameterizedType;
import io.ballerina.runtime.api.types.StreamType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BClientType;
import io.ballerina.runtime.internal.values.MapValueImpl;

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
    public static BObject mock(BTypedesc bTypedesc, BObject objectValue) {
        ObjectType objectValueType = (ObjectType) TypeUtils.getReferredType(objectValue.getType());
        if (!objectValueType.getName().contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            // handle user-defined mock object
            if (objectValueType.getMethods().length == 0 &&
                    objectValueType.getFields().size() == 0 &&
                    (objectValueType instanceof BClientType &&
                            ((BClientType) objectValueType).getResourceMethods().length == 0)) {
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
                if (objectValueType instanceof BClientType) {
                    for (MethodType attachedFunction : ((BClientType) objectValueType).getResourceMethods()) {
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
                ((ObjectType) TypeUtils.getReferredType(genericMock.getType())).getMethods())) {
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
                ((ObjectType) TypeUtils.getReferredType(genericMock.getType())).getFields())) {
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
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(
                StringUtils.fromString("mockObject"));
        String functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();
        BArray argsList = caseObj.getArrayValue(StringUtils.fromString("args"));

        for (MethodType attachedFunction :
                ((ObjectType) TypeUtils.getReferredType(genericMock.getType())).getMethods()) {
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
                    Type paramType = TypeUtils.getReferredType(attachedFunction.getType().getParameters()[i].type);
                    if (paramType instanceof UnionType) {
                        Object arg = it.next();
                        boolean isTypeAvailable = false;
                        List<Type> memberTypes = ((UnionType) paramType).getMemberTypes();
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
     * Registers the return value to the case provided.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError thenReturn(BObject caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj
                .get(StringUtils.fromString("mockObject"));
        BObject mockObj = genericMock.getMockObj();
        Object returnVal = caseObj.get(StringUtils.fromString("returnValue"));
        String functionName;
        try {
            functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();
        } catch (Exception e) {
            if (!e.getMessage().contains("No such field: functionName")) {
                throw e;
            }
            functionName = null;
        }
        ObjectType objectType = (ObjectType) TypeUtils.getReferredType(genericMock.getType());
        if (functionName != null) {
            // register return value for member function
            BArray args = caseObj.getArrayValue(StringUtils.fromString("args"));
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
        } else {

            // register return value for member field
            String fieldName = caseObj.getStringValue(StringUtils.fromString("fieldName")).toString();

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

    /**
     * Registers a sequence of return values to the case provided.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static BError thenReturnSequence(BObject caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj
                .get(StringUtils.fromString("mockObject"));
        BObject mockObj = genericMock.getMockObj();
        String functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();
        BArray returnVals = caseObj.getArrayValue(StringUtils.fromString("returnValueSeq"));

        for (int i = 0; i < returnVals.getValues().length; i++) {
            if (returnVals.getValues()[i] == null) {
                break;
            }
            if (!validateReturnValue(functionName, returnVals.getValues()[i],
                    ((ObjectType) TypeUtils.getReferredType(genericMock.getType())).getMethods())) {
                String detail = "return value provided at position '" + i
                        + "' does not match the return type of function '" + functionName + "()'";
                return ErrorCreator.createError(
                        MockConstants.TEST_PACKAGE_ID,
                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                        StringUtils.fromString(detail),
                        null,
                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
            }
            MockRegistry.getInstance().registerCase(mockObj, functionName, null, returnVals.getValues()[i], i + 1);
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
            if (memberType.getTag() == TypeTags.PARAMETERIZED_TYPE_TAG) {
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
        Type sourceType = TypeUtils.getReferredType(getType(returnVal));
        if (sourceType.getTag() == TypeTags.STREAM_TAG) {
            Type targetConstrainedType = streamType.getConstrainedType();
            Type targetCompletionType = streamType.getCompletionType();
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
                Type functionReturnType = attachedFunction.getType().getReturnParameterType();
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
        }
        return false;
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
                                TypeUtils.getReferredType(attachedFunction.getType().getParameters()[i].type);
                        Type paramType = TypeUtils.getReferredType(parameters[i].type);
                        if (paramTypeAttachedFunc instanceof UnionType) {
                            if (!(paramType instanceof UnionType)) {
                                String detail = "incompatible parameter type provided at position " + (i + 1) + " in" +
                                        " function '" + functionName + "()'. parameter should be of union type ";
                                return ErrorCreator.createError(
                                        MockConstants.TEST_PACKAGE_ID,
                                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                        StringUtils.fromString(detail),
                                        null,
                                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
                            } else {
                                Type[] memberTypes =
                                        ((UnionType) paramTypeAttachedFunc).getMemberTypes().toArray(new Type[0]);
                                Type[] providedTypes = ((UnionType) paramType).getMemberTypes().toArray(new Type[0]);
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
                if (TypeChecker.checkIsType(field.getValue().getFieldType(), mockField.getValue().getFieldType())) {
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
