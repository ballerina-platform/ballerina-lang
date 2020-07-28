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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;

import java.util.List;
import java.util.Map;

/**
 * Class that contains inter-op function related to mocking.
 */
public class ObjectMock {

    /**
     * Returns a generic mock object pretending to be the type of the provided typedesc.
     *
     * @param typedescValue typedesc of the mock obejct
     * @param objectValue mock object to impersonate the type
     * @return mock object of provided type
     */
    public static ObjectValue mock(TypedescValue typedescValue, ObjectValue objectValue) {
        if (!objectValue.getType().getName().contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            // handle user-defined mock object
            if (objectValue.getType().getAttachedFunctions().length == 0 &&
                    objectValue.getType().getFields().size() == 0) {
                String detail = "mock object type '" + objectValue.getType().getName()
                        + "' should have at least one member function or field declared.";
                throw BallerinaErrors.createDistinctError(
                        MockConstants.INVALID_MOCK_OBJECT_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
            } else {
                for (AttachedFunction attachedFunction : objectValue.getType().getAttachedFunctions()) {
                    ErrorValue errorValue = validateFunctionSignatures(attachedFunction,
                            ((BObjectType) typedescValue.getDescribingType()).getAttachedFunctions());
                    if (errorValue != null) {
                        throw  errorValue;
                    }
                }
                for (Map.Entry<String, BField> field : objectValue.getType().getFields().entrySet()) {
                    ErrorValue errorValue = validateField(field,
                            ((BObjectType) typedescValue.getDescribingType()).getFields());
                    if (errorValue != null) {
                        throw  errorValue;
                    }
                }
            }
        }
        // handle default mock generation
        BObjectType bObjectType = (BObjectType) typedescValue.getDescribingType();
        return new GenericMockObjectValue(bObjectType, objectValue);
    }

    /**
     * Validates the object provided to register mock cases.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static ErrorValue validatePreparedObj(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj;
        ObjectValue mockObj = genericMock.getMockObj();
        String objectType = mockObj.getType().getName();
        if (!objectType.contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            String detail = "cases cannot be registered to user-defined object type '"
                            + genericMock.getType().getName() + "'";
            return BallerinaErrors.createDistinctError(
                    MockConstants.INVALID_MOCK_OBJECT_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
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

    public static ErrorValue validateFunctionName(String functionName, ObjectValue mockObject) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) mockObject;
        if (!validateFunctionName(functionName, genericMock.getType().getAttachedFunctions())) {
            String detail = "invalid function name '" + functionName + " ' provided";
            return BallerinaErrors.createDistinctError(
                    MockConstants.FUNCTION_NOT_FOUND_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
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
    public static ErrorValue validateFieldName(String fieldName, ObjectValue mockObject) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) mockObject;
        if (!validateFieldName(fieldName, genericMock.getType().getFields())) {
            String detail = "invalid field name '" + fieldName + "' provided";
            return BallerinaErrors.createDistinctError(
                    MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
        }
        return null;
    }

    /**
     * Validates the member field name provided to register mock cases.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static ErrorValue validateArguments(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(
                StringUtils.fromString("mockObject"));
        String functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();
        ArrayValue argsList = caseObj.getArrayValue(StringUtils.fromString("args"));

        for (AttachedFunction attachedFunction : genericMock.getType().getAttachedFunctions()) {
            if (attachedFunction.getName().equals(functionName)) {

                // validate the number of arguments provided
                if (argsList.size() > attachedFunction.type.getParameterType().length) {
                    String detail = "too many argument provided to mock the function " + functionName + "()";
                    return BallerinaErrors.createDistinctError(
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
                }

                // validate if each argument is compatible with the type given in the function signature
                int i = 0;
                for (IteratorValue it = argsList.getIterator(); it.hasNext(); i++) {
                    if (attachedFunction.type.getParameterType()[i] instanceof BUnionType) {
                        Object arg = it.next();
                        boolean isTypeAvailable = false;
                        List<BType> memberTypes =
                                ((BUnionType) attachedFunction.type.getParameterType()[i]).getMemberTypes();
                        for (BType memberType : memberTypes) {
                            if (TypeChecker.checkIsType(arg, memberType)) {
                                isTypeAvailable = true;
                                break;
                            }
                        }
                        if (!isTypeAvailable) {
                            String detail =
                                    "incorrect type of argument provided at position '" + (i + 1) +
                                            "' to mock the function " + functionName + "()";
                            return BallerinaErrors.createDistinctError(
                                    MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                    MockConstants.TEST_PACKAGE_ID, detail);
                        }
                    } else if (!TypeChecker.checkIsType(it.next(), attachedFunction.type.getParameterType()[i])) {
                        String detail =
                                "incorrect type of argument provided at position '" + (i + 1)
                                        + "' to mock the function " + functionName + "()";
                        return BallerinaErrors.createDistinctError(
                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
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
    public static ErrorValue thenReturn(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj
                .get(StringUtils.fromString("mockObject"));
        ObjectValue mockObj = genericMock.getMockObj();
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
            ArrayValue args = caseObj.getArrayValue(StringUtils.fromString("args"));
            if (!validateReturnValue(functionName, returnVal, genericMock.getType().getAttachedFunctions())) {
                String detail =
                        "return value provided does not match the return type of function " + functionName + "()";
                return BallerinaErrors.createDistinctError(
                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
            }
            MockRegistry.getInstance().registerCase(mockObj, functionName, args, returnVal);
        } else {

            // register return value for member field
            String fieldName = caseObj.getStringValue(StringUtils.fromString("fieldName")).toString();

            if (!validateFieldValue(returnVal,
                    genericMock.getType().getFields().get(fieldName).getFieldType())) {
                String detail = "return value provided does not match the type of '" + fieldName + "'";
                return BallerinaErrors.createDistinctError(
                        MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
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
    public static ErrorValue thenReturnSequence(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj
                .get(StringUtils.fromString("mockObject"));
        ObjectValue mockObj = genericMock.getMockObj();
        String functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();
        ArrayValue returnVals = caseObj.getArrayValue(StringUtils.fromString("returnValueSeq"));

        for (int i = 0; i < returnVals.getValues().length; i++) {
            if (returnVals.getValues()[i] == null) {
                break;
            }
            if (!validateReturnValue(functionName, returnVals.getValues()[i],
                    genericMock.getType().getAttachedFunctions())) {
                String details = "return value provided at position '" + i
                        + "' does not match the return type of function " + functionName + "()";
                return BallerinaErrors.createDistinctError(
                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID, details);
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
    private static boolean validateFunctionName(String functionName, AttachedFunction[] attachedFunctions) {
        for (AttachedFunction attachedFunction : attachedFunctions) {
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
    private static boolean validateFieldName(String fieldName, Map<String, BField> fieldMap) {
        for (Map.Entry<String, BField> field : fieldMap.entrySet()) {
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
            String functionName, Object returnVal, AttachedFunction[] attachedFunctions) {

        for (AttachedFunction attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {
                if (attachedFunction.type.getReturnParameterType() instanceof BUnionType) {
                    List<BType> memberTypes =
                            ((BUnionType) attachedFunction.type.getReturnParameterType()).getMemberTypes();
                    for (BType memberType : memberTypes) {
                        if (TypeChecker.checkIsType(returnVal, memberType)) {
                            return true;
                        }
                    }
                } else {
                    return TypeChecker.checkIsType(returnVal, attachedFunction.type.getReturnParameterType());
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
    private static boolean validateFieldValue(Object returnVal, BType fieldType) {
        return TypeChecker.checkIsType(returnVal, fieldType);
    }

    /**
     * Validates the signature of the function provided when a user-defined mock object is used.
     *
     * @param func function defined in mock object
     * @param attachedFunctions functions available in the mocked type
     * @return whether the function signature is valid
     */
    private static ErrorValue validateFunctionSignatures(AttachedFunction func, AttachedFunction[] attachedFunctions) {
        String functionName = func.getName();
        BType[] paramTypes = func.getParameterType();
        BType returnType = func.type.getReturnParameterType();

        for (AttachedFunction attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {

                // validate that the number of parameters are equal
                if (paramTypes.length != attachedFunction.getParameterType().length) {
                    String detail = "incorrect number of parameters provided for function " + functionName + "()";
                    return BallerinaErrors.createDistinctError(
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
                } else {
                    // validate the equivalence of the parameter types
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (attachedFunction.getParameterType()[i] instanceof BUnionType) {
                            if (!(paramTypes[i] instanceof BUnionType)) {
                                String detail = "incompatible parameter type provided at position " + (i + 1) + " in" +
                                        " function " + functionName + "(). parameter should be of union type ";
                                return BallerinaErrors.createDistinctError(
                                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                        MockConstants.TEST_PACKAGE_ID, detail);
                            } else {
                                BType[] memberTypes = ((BUnionType) attachedFunction.getParameterType()[i])
                                        .getMemberTypes().toArray(new BType[0]);
                                BType[] providedTypes = ((BUnionType) paramTypes[i])
                                        .getMemberTypes().toArray(new BType[0]);
                                for (int j = 0; j < memberTypes.length; j++) {
                                    if (!TypeChecker.checkIsType(providedTypes[j], memberTypes[j])) {
                                        String detail = "incompatible parameter type provided at position "
                                                        + (i + 1) + " in function " + functionName + "()";
                                        return BallerinaErrors.createDistinctError(
                                                MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                                MockConstants.TEST_PACKAGE_ID, detail);
                                    }
                                }

                            }
                        } else {
                            if (!TypeChecker.checkIsType(paramTypes[i], attachedFunction.getParameterType()[i])) {
                                String detail =
                                        "incompatible parameter type provided at position "
                                                + (i + 1) + " in function " + functionName + "()";
                                return BallerinaErrors.createDistinctError(
                                        MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR,
                                        MockConstants.TEST_PACKAGE_ID, detail);
                            }
                        }
                    }
                }

                // validate the equivalence of the return types
                if (!TypeChecker.checkIsType(returnType, attachedFunction.type.getReturnParameterType())) {
                    String detail = "incompatible return type provided for function " + functionName + "()";
                    return BallerinaErrors.createDistinctError(
                            MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
                }
                return null;
            }
        }
        String detail = "invalid function '" + functionName + "' provided";
        return BallerinaErrors.createDistinctError(
                MockConstants.FUNCTION_NOT_FOUND_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
    }

    /**
     * Validates the field provided when a user-defined mock object is used.
     *
     * @param mockField field defined in mock object
     * @param fieldMap fields available in the mocked type
     * @return whether the field declaration is valid
     */
    private static ErrorValue validateField(Map.Entry<String, BField> mockField, Map<String, BField> fieldMap) {
        for (Map.Entry<String, BField> field : fieldMap.entrySet()) {
            if (field.getKey().equals(mockField.getKey())) {
                if (TypeChecker.checkIsType(field.getValue().getFieldType(), mockField.getValue().getFieldType())) {
                    return null;
                } else {
                    String detail = "incompatible field type '" + mockField.getValue().getFieldType() + "' provided " +
                            "for field " + mockField.getKey() + "";
                    return BallerinaErrors.createDistinctError(
                            MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
                }
            }
        }
        String detail = "invalid field '" + mockField.getKey() + "' provided";
        return BallerinaErrors.createDistinctError(
                MockConstants.INVALID_MEMBER_FIELD_ERROR, MockConstants.TEST_PACKAGE_ID, detail);
    }
}
