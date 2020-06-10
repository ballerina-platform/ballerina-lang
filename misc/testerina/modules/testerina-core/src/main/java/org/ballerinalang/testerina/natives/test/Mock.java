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
package org.ballerinalang.testerina.natives.test;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BNullType;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;

import java.util.List;
import java.util.Map;

/**
 * Class that contains inter-op function related to mocking.
 */
public class Mock {

    /**
     * Returns a generic mock object pretending to be the type of the provided typedesc.
     *
     * @param typedescValue typedesc of the mock obejct
     * @param objectValue mock object to impersonate the type
     * @return mock object of provided type
     */
    public static Object mock(TypedescValue typedescValue, ObjectValue objectValue) {
        if (!objectValue.getType().getName().contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            // handle user-defined mock object
            if (objectValue.getType().getAttachedFunctions().length == 0 &&
                    objectValue.getType().getFields().size() == 0) {
                String detail = "Mock object type '" + objectValue.getType().getName()
                        + "' should have at least one member function or field declared.";
                return BallerinaErrors.createError(
                        StringUtils.fromString(MockConstants.INVALID_MOCK_OBJECT_ERROR),
                        StringUtils.fromString(detail));
            } else {
                for (AttachedFunction attachedFunction : objectValue.getType().getAttachedFunctions()) {
                    ErrorValue errorValue = validateFunctionSignatures(attachedFunction,
                            ((BObjectType) typedescValue.getDescribingType()).getAttachedFunctions());
                    if (errorValue != null) {
                        return errorValue;
                    }
                }
                for (Map.Entry<String, BField> field : objectValue.getType().getFields().entrySet()) {
                    ErrorValue errorValue = validateField(field,
                            ((BObjectType) typedescValue.getDescribingType()).getFields());
                    if (errorValue != null) {
                        return errorValue;
                    }
                }
            }
        }
        // handle default mock generation
        BObjectType bObjectType = (BObjectType) typedescValue.getDescribingType();
        return new GenericMockObjectValue(bObjectType, objectValue);
    }

    /**
     * Registers the return value to the case provided.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static ErrorValue thenReturn(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.get(StringUtils.fromString("prepareObj"));
        ObjectValue mockObj = genericMock.getMockObj();
        String functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();

        if (!"".equals(functionName)) { // register return value for member function
            ArrayValue args = caseObj.getArrayValue(StringUtils.fromString("args"));
            Object returnVal = caseObj.get(StringUtils.fromString("returnVal"));
            if (!validateReturnType(functionName, returnVal, genericMock.getType().getAttachedFunctions())) {
                String detail = "invalid return value found.";
                return BallerinaErrors
                        .createError(StringUtils.fromString(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR),
                                StringUtils.fromString(detail));
            }
            MockRegistry.getInstance().registerCase(mockObj, functionName, args, returnVal);
        } else { // register return value for member field
            String fieldName = caseObj.getStringValue(StringUtils.fromString("fieldName")).toString();
            Object returnVal = caseObj.get(StringUtils.fromString("returnVal"));

            if (!validateReturnType(returnVal,
                    genericMock.getType().getFields().get(fieldName).getFieldType().getValueClass())) {
                String detail = "invalid return value found.";
                return BallerinaErrors
                        .createError(StringUtils.fromString(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR),
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
    public static ErrorValue thenReturnSequence(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.get(StringUtils.fromString("prepareObj"));
        ObjectValue mockObj = genericMock.getMockObj();
        String functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();
        ArrayValue returnVals = caseObj.getArrayValue(StringUtils.fromString("returnValSeq"));

        for (int i = 0; i < returnVals.getValues().length; i++) {
            if (returnVals.getValues()[i] == null) {
                break;
            }
            if (!validateReturnType(functionName, returnVals.getValues()[i],
                    genericMock.getType().getAttachedFunctions())) {
                String details = "invalid return value found.";
                return BallerinaErrors
                        .createError(StringUtils.fromString(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR),
                                StringUtils.fromString(details));
            }
            MockRegistry.getInstance().registerCase(mockObj, functionName, null, returnVals.getValues()[i], i + 1);
        }
        return null;
    }

    private static boolean validateReturnType(
            String functionName, Object returnVal, AttachedFunction[] attachedFunctions) {
        if (returnVal != null) {
            // TODO: validate return value against the return type of function
            return true;
        } else {
            for (AttachedFunction attachedFunction : attachedFunctions) {
                if (attachedFunction.getName().equals(functionName)) {
                    if (attachedFunction.type.getReturnParameterType() instanceof BUnionType) {
                        List<BType> memberTypes =
                                ((BUnionType) attachedFunction.type.getReturnParameterType()).getMemberTypes();
                        for (BType memberType : memberTypes) {
                            if (memberType instanceof BNullType) {
                                return true;
                            }
                        }
                        return false;
                    } else {
                        return attachedFunction.type.getReturnParameterType() instanceof BNullType;
                    }
                }
            }
        }
        return false;
    }

    // TODO: validate field return value with return type
    private static boolean validateReturnType(Object returnVal, Class<Object> clazz) {
        return true;
    }

    /**
     * Validates the object provided to register mock cases.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static ErrorValue validatePrepareObj(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj;
        ObjectValue mockObj = genericMock.getMockObj();
        String objectType = mockObj.getType().getName();
        if (!objectType.contains(MockConstants.DEFAULT_MOCK_OBJ_ANON)) {
            String detail =
                    "Cases cannot be registered to user-defined object type '" + genericMock.getType().getName() + "'.";
            return BallerinaErrors.createError(StringUtils.fromString(MockConstants.INVALID_MOCK_OBJECT_ERROR),
                    StringUtils.fromString(detail));
        }
        return null;
    }

    /**
     * Validates the member function name provided to register mock cases.
     *
     * @param caseObj ballerina object that contains information about the case to register
     * @return an optional error if a validation fails
     */
    public static ErrorValue validateFunctionName(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(
                StringUtils.fromString("prepareObj"));
        String functionName = caseObj.getStringValue(StringUtils.fromString("functionName")).toString();
        if (!validateFunctionName(functionName, genericMock.getType().getAttachedFunctions())) {
            String detail = "invalid function name '" + functionName + " 'passed to register cases for type '"
                            + genericMock.getType().getName() + "'.";
            return BallerinaErrors.createError(StringUtils.fromString(MockConstants.FUNCTION_NOT_FOUND_ERROR),
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
    public static ErrorValue validateFieldName(ObjectValue caseObj) {
        GenericMockObjectValue genericMock = (GenericMockObjectValue) caseObj.getObjectValue(StringUtils.fromString(
                "prepareObj"));
        String fieldName = caseObj.getStringValue(StringUtils.fromString("fieldName")).toString();
        if (!validateFieldName(fieldName, genericMock.getType().getFields())) {
            String detail = "invalid field name '" + fieldName + " 'passed to register cases for type '"
                            + genericMock.getType().getName() + "'.";
            return BallerinaErrors
                    .createError(StringUtils.fromString(MockConstants.INVALID_MEMBER_FIELD_ERROR),
                            StringUtils.fromString(detail));
        }
        return null;
    }

    private static boolean validateFunctionName(String functionName, AttachedFunction[] attachedFunctions) {
        for (AttachedFunction attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {
                return true;
            }
        }
        return false;
    }

    private static ErrorValue validateFunctionSignatures(AttachedFunction func, AttachedFunction[] attachedFunctions) {
        String functionName = func.getName();
        BType[] paramTypes = func.getParameterType();
        BType returnType = func.type.getReturnParameterType();

        for (AttachedFunction attachedFunction : attachedFunctions) {
            if (attachedFunction.getName().equals(functionName)) {
                if (paramTypes.length != attachedFunction.getParameterType().length) {
                    String details = "incompatible number of parameters found in function '" + functionName + "'.";
                    return BallerinaErrors.createError(
                            StringUtils.fromString(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR),
                            StringUtils.fromString(details));
                } else {
                    for (int i = 0; i < paramTypes.length; i++) {
                        boolean isParamTypeMatching =
                                paramTypes[i].getName().equals(attachedFunction.getParameterType()[i].getName());
                        if (!isParamTypeMatching) {
                            String details =
                                    "incompatible parameter type '" + paramTypes[i].getName() + "' found at position "
                                            + i + " in function '" + functionName + "'.";
                            return BallerinaErrors.createError(
                                    StringUtils.fromString(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR),
                                    StringUtils.fromString(details));
                        }
                    }
                }

                if (!returnType.equals(attachedFunction.type.getReturnParameterType())) {
                    String details = "incompatible return type '" + returnType.getName() + "' found in function '"
                                    + functionName + "'.";
                    return BallerinaErrors.createError(
                            StringUtils.fromString(MockConstants.FUNCTION_SIGNATURE_MISMATCH_ERROR),
                            StringUtils.fromString(details));
                }
                return null;
            }
        }
        String details = "invalid function '" + functionName + "' found.";
        return BallerinaErrors.createError(
                StringUtils.fromString(MockConstants.FUNCTION_NOT_FOUND_ERROR), StringUtils.fromString(details));
    }

    private static ErrorValue validateField(Map.Entry<String, BField> mockField, Map<String, BField> fieldMap) {
        for (Map.Entry<String, BField> field : fieldMap.entrySet()) {
            if (field.getKey().equals(mockField.getKey())) {
                if (field.getValue().getFieldType().equals(mockField.getValue().getFieldType())) {
                    return null;
                } else {
                    String details = "incompatible field type '" + mockField.getValue().getFieldType() + "' provided " +
                            "to field " + mockField.getKey() + ".";
                    return BallerinaErrors.createError(
                            StringUtils.fromString(MockConstants.INVALID_MEMBER_FIELD_ERROR),
                            StringUtils.fromString(details));
                }
            }
        }
        String details = "invalid field '" + mockField.getKey() + "' found.";
        return BallerinaErrors.createError(
                StringUtils.fromString(MockConstants.INVALID_MEMBER_FIELD_ERROR), StringUtils.fromString(details));
    }

    private static boolean validateFieldName(String mockField, Map<String, BField> fieldMap) {
        for (Map.Entry<String, BField> field : fieldMap.entrySet()) {
            if (field.getKey().equals(mockField)) {
                return true;
            }
        }
        return false;
    }
}
