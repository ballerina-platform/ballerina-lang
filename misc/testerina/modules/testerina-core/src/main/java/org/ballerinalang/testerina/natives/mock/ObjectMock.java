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

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.util.exceptions.BLangRuntimeException;

import java.util.List;
import java.util.Map;

/**
 * Class that contains inter-op function related to mocking.
 */
public class ObjectMock {

    /**
     * Returns a generic mock object pretending to be the type of the provided typedesc.
     *
     * @param bTypedesc typedesc of the mock obejct
     * @param objectValue mock object to impersonate the type
     * @return mock object of provided type
     */
    public static BObject mock(BTypedesc bTypedesc, BObject objectValue) {
        if (!objectValue.getType().getName().contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            // handle user-defined mock object
            if (objectValue.getType().getAttachedFunctions().length == 0 &&
                    objectValue.getType().getFields().size() == 0) {
                String detail = "mock object type '" + objectValue.getType().getName()
                        + "' should have at least one member function or field declared.";
                throw ErrorCreator.createDistinctError(
                        MockConstants.INVALID_MOCK_OBJECT_ERROR, MockConstants.TEST_PACKAGE_ID,
                        StringUtils.fromString(detail));
            } else {
                for (AttachedFunctionType attachedFunction : objectValue.getType().getAttachedFunctions()) {
                    BError error = validateFunctionSignatures(attachedFunction,
                            ((ObjectType) bTypedesc.getDescribingType()).getAttachedFunctions());
                    if (error != null) {
                        throw  error;
                    }
                }
                for (Map.Entry<String, Field> field : objectValue.getType().getFields().entrySet()) {
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
        if (!validateFunctionName(functionName, genericMock.getType().getAttachedFunctions())) {
            String detail = "invalid function name '" + functionName + " ' provided";
            return ErrorCreator.createDistinctError(
                    MockConstants.FUNCTION_NOT_FOUND_ERROR, MockConstants.TEST_PACKAGE_ID,
                    StringUtils.fromString(detail));
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
        if (!validateFieldName(fieldName, genericMock.getType().getFields())) {
            String detail = "invalid field name '" + fieldName + "' provided";
            return ErrorCreator.createDistinctError(
                    MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID,
                    StringUtils.fromString(detail));
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

        for (AttachedFunctionType attachedFunction : genericMock.getType().getAttachedFunctions()) {
            if (attachedFunction.getName().equals(functionName)) {

                // validate the number of arguments provided
                if (argsList.size() > attachedFunction.getType().getParameterTypes().length) {
                    String detail = "too many argument provided to mock the function " + functionName + "()";
                    return ErrorCreator.createDistinctError(
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID,
                            StringUtils.fromString(detail));
                }

                // validate if each argument is compatible with the type given in the function signature
                int i = 0;
                for (BIterator it = argsList.getIterator(); it.hasNext(); i++) {
                    if (attachedFunction.getType().getParameterTypes()[i] instanceof UnionType) {
                        Object arg = it.next();
                        boolean isTypeAvailable = false;
                        List<Type> memberTypes =
                                ((UnionType) attachedFunction.getType().getParameterTypes()[i]).getMemberTypes();
                        for (Type memberType : memberTypes) {
                            if (TypeChecker.checkIsType(arg, memberType)) {
                                isTypeAvailable = true;
                                break;
                            }
                        }
                        if (!isTypeAvailable) {
                            String detail =
                                    "incorrect type of argument provided at position '" + (i + 1) +
                                            "' to mock the function " + functionName + "()";
                            return ErrorCreator.createDistinctError(
                                    MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                    MockConstants.TEST_PACKAGE_ID, StringUtils.fromString(detail));
                        }
                    } else if (!TypeChecker.checkIsType(it.next(), attachedFunction.getType().getParameterTypes()[i])) {
                        String detail =
                                "incorrect type of argument provided at position '" + (i + 1)
                                        + "' to mock the function " + functionName + "()";
                        return ErrorCreator.createDistinctError(
                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID,
                                StringUtils.fromString(detail));
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
        } catch (BLangRuntimeException e) {
            if (!e.getMessage().contains("No such field or method: functionName")) {
                throw e;
            }
            functionName = null;
        }
        if (functionName != null) {
            // register return value for member function
            BArray args = caseObj.getArrayValue(StringUtils.fromString("args"));
            if (!validateReturnValue(functionName, returnVal, genericMock.getType().getAttachedFunctions())) {
                String detail =
                        "return value provided does not match the return type of function " + functionName + "()";
                return ErrorCreator.createDistinctError(
                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID,
                        StringUtils.fromString(detail));
            }
            MockRegistry.getInstance().registerCase(mockObj, functionName, args, returnVal);
        } else {

            // register return value for member field
            String fieldName = caseObj.getStringValue(StringUtils.fromString("fieldName")).toString();

            if (!validateFieldValue(returnVal,
                    genericMock.getType().getFields().get(fieldName).getFieldType())) {
                String detail = "return value provided does not match the type of '" + fieldName + "'";
                return ErrorCreator.createDistinctError(
                        MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID,
                        StringUtils.fromString(detail));
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
                    genericMock.getType().getAttachedFunctions())) {
                String details = "return value provided at position '" + i
                        + "' does not match the return type of function " + functionName + "()";
                return ErrorCreator.createDistinctError(
                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID,
                        StringUtils.fromString(details));
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
    private static boolean validateFunctionName(String functionName, AttachedFunctionType[] attachedFunctions) {
        for (AttachedFunctionType attachedFunction : attachedFunctions) {
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

    /**
     * Validates the function return value provided when a default mock object is used.
     *
     * @param functionName function to validate against
     * @param returnVal return value provided
     * @param attachedFunctions functions available in the mocked type
     * @return whether the return value is valid
     */
    private static boolean validateReturnValue(
            String functionName, Object returnVal, AttachedFunctionType[] attachedFunctions) {

        for (AttachedFunctionType attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {
                if (attachedFunction.getType().getReturnParameterType() instanceof UnionType) {
                    List<Type> memberTypes =
                            ((UnionType) attachedFunction.getType().getReturnParameterType()).getMemberTypes();
                    for (Type memberType : memberTypes) {
                        if (TypeChecker.checkIsType(returnVal, memberType)) {
                            return true;
                        }
                    }
                } else {
                    return TypeChecker.checkIsType(returnVal, attachedFunction.getType().getReturnParameterType());
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
    private static BError validateFunctionSignatures(AttachedFunctionType func,
                                                     AttachedFunctionType[] attachedFunctions) {
        String functionName = func.getName();
        Type[] paramTypes = func.getParameterTypes();
        Type returnType = func.getType().getReturnParameterType();

        for (AttachedFunctionType attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {

                // validate that the number of parameters are equal
                if (paramTypes.length != attachedFunction.getParameterTypes().length) {
                    String detail = "incorrect number of parameters provided for function " + functionName + "()";
                    return ErrorCreator.createDistinctError(
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID,
                            StringUtils.fromString(detail));
                } else {
                    // validate the equivalence of the parameter types
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (attachedFunction.getParameterTypes()[i] instanceof UnionType) {
                            if (!(paramTypes[i] instanceof UnionType)) {
                                String detail = "incompatible parameter type provided at position " + (i + 1) + " in" +
                                        " function " + functionName + "(). parameter should be of union type ";
                                return ErrorCreator.createDistinctError(
                                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                        MockConstants.TEST_PACKAGE_ID, StringUtils.fromString(detail));
                            } else {
                                Type[] memberTypes = ((UnionType) attachedFunction.getParameterTypes()[i])
                                        .getMemberTypes().toArray(new Type[0]);
                                Type[] providedTypes = ((UnionType) paramTypes[i])
                                        .getMemberTypes().toArray(new Type[0]);
                                for (int j = 0; j < memberTypes.length; j++) {
                                    if (!TypeChecker.checkIsType(providedTypes[j], memberTypes[j])) {
                                        BString detail = StringUtils
                                                .fromString("incompatible parameter type provided at position "
                                                        + (i + 1) + " in function " + functionName + "()");
                                        return ErrorCreator.createDistinctError(
                                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                                MockConstants.TEST_PACKAGE_ID, detail);
                                    }
                                }

                            }
                        } else {
                            if (!TypeChecker.checkIsType(paramTypes[i], attachedFunction.getParameterTypes()[i])) {
                                BString detail =
                                        StringUtils.fromString("incompatible parameter type provided at position "
                                                + (i + 1) + " in function " + functionName + "()");
                                return ErrorCreator.createDistinctError(
                                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                        MockConstants.TEST_PACKAGE_ID, detail);
                            }
                        }
                    }
                }

                // validate the equivalence of the return types
                if (!TypeChecker.checkIsType(returnType, attachedFunction.getType().getReturnParameterType())) {
                    String detail = "incompatible return type provided for function " + functionName + "()";
                    return ErrorCreator.createDistinctError(
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID,
                            StringUtils.fromString(detail));
                }
                return null;
            }
        }
        String detail = "invalid function '" + functionName + "' provided";
        return ErrorCreator.createDistinctError(MockConstants.FUNCTION_NOT_FOUND_ERROR, MockConstants.TEST_PACKAGE_ID,
                                                StringUtils.fromString(detail));
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
                    return ErrorCreator.createDistinctError(
                            MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID,
                            StringUtils.fromString(detail));
                }
            }
        }
        String detail = "invalid field '" + mockField.getKey() + "' provided";
        return ErrorCreator.createDistinctError(
                MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID,
                StringUtils.fromString(detail));
    }
}
