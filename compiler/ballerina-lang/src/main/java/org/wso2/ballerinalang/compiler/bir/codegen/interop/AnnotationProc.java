/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.bir.codegen.Nilable;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationArrayValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationLiteralValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationRecordValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunctionParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//import ballerina/bir;
//import ballerina/jvm;
//import ballerina/io;

public class AnnotationProc {
    public static final NAME_FIELD_NAME ="name";
    public static final CLASS_FIELD_NAME ="class";
    public static final FIELD_METHOD_FIELD_NAME ="method";
    public static final PARAM_TYPES_FIELD_NAME ="paramTypes";
    public static final String DIMENSIONS_FIELD_NAME = "dimensions";

    static @Nilable
    InteropValidationRequest getInteropAnnotValue(BIRFunction birFunc) {
        var optionalAnnotAttach = getInteropAnnotAttachment(birFunc);
        if (optionalAnnotAttach == null) {
            return null;
        }

        var annotAttach = (BIRAnnotationAttachment) optionalAnnotAttach;
        var annotTagRef = (InteropAnnotTag) annotAttach.annotTagRef.value;
        return createJInteropValidationRequest(annotTagRef, annotAttach, birFunc);
    }

    static @Nilable
    BIRAnnotationAttachment getInteropAnnotAttachment(BIRFunction birFunc) {
        for (T optionalAnnotAttach : birFunc.annotAttachments) {
            if (isInteropAnnotAttachment((BIRAnnotationAttachment) optionalAnnotAttach)) {
                return optionalAnnotAttach;
            }
        }
        return null;
    }

    static boolean isInteropAnnotAttachment(BIRAnnotationAttachment annotAttach) {
        return annotAttach.moduleId.org == = INTEROP_ANNOT_ORG &&
                annotAttach.moduleId.name == = INTEROP_ANNOT_MODULE &&
                annotAttach.annotTagRef.value instanceof InteropAnnotTag;
    }

    static InteropValidationRequest createJInteropValidationRequest(InteropAnnotTag annotTagRef,
                                                                    BIRAnnotationAttachment annotAttach,
                                                                    BIRFunction birFunc) {

        var annotRecValue = (BIRAnnotationRecordValue) annotAttach.annotValues.get(0);
        Map<String, BIRAnnotationValue> annotValueMap = annotRecValue.annotValueMap;
        if (annotTagRef instanceof MethodAnnotTag) {
            return createJMethodValidationRequest(annotTagRef, annotValueMap, birFunc);
        } else {
            return createJFieldValidationRequest(annotTagRef, annotValueMap, birFunc);
        }
    }

    static MethodValidationRequest createJMethodValidationRequest(MethodAnnotTag annotTagRef,
                                                                  Map<String, BIRAnnotationValue> annotValueMap,
                                                                  BIRFunction birFunc) {

        MethodValidationRequest valRequest = new MethodValidationRequest(
                name:getJMethodNameFromAnnot(annotTagRef, annotValueMap.get(NAME_FIELD_NAME), birFunc),
                klass:(String) getLiteralValueFromAnnotValue(annotValueMap.get(CLASS_FIELD_NAME)),
                kind:getMethodKindFromAnnotTag(annotTagRef),
                bFuncType:birFunc.type
    );

        var paramTypeConstraints = buildParamTypeConstraints(annotValueMap.get(PARAM_TYPES_FIELD_NAME), birFunc, annotTagRef);
        if !(paramTypeConstraints == null) {
            valRequest["paramTypeConstraints"] = paramTypeConstraints;
        }
        return valRequest;
    }

    static FieldValidationRequest createJFieldValidationRequest(FieldAnnotTag annotTagRef,
                                                                Map<String, BIRAnnotationValue> annotValueMap,
                                                                BIRFunction birFunc) {

        return {
                name:getJFieldNameFromAnnot(annotValueMap.get(NAME_FIELD_NAME), birFunc),
                klass:(String) getLiteralValueFromAnnotValue(annotValueMap.get(CLASS_FIELD_NAME)),
                method:getFieldMethodFromAnnotTag(annotTagRef),
                bFuncType:birFunc.type
    };
    }

    static @Nilable
    List<JType> buildParamTypeConstraints(@Nilable BIRAnnotationValue annotValue,
                                          BIRFunction birFunc,
                                          MethodAnnotTag annotTagRef) {
        // creating the constraints using the existing function parameter types first.
        Map<BIRFunctionParameter, List<BIRBasicBlock>> birFuncParams = birFunc.parameters;
        int birFuncParamCount = birFunc.parameters.size();

        if (annotValue instanceof BIRAnnotationArrayValue) {
            @Nilable List<BIRAnnotationValue> annotArrayElements = annotValue.annotValueArray;
            @Nilable List<JType> constraints = new ArrayList<>();
            for (BIRAnnotationValue annotArrayElement : annotArrayElements) {
                JType jType;
                if (annotArrayElement instanceof BIRAnnotationLiteralValue) {
                    jType = getJTypeFromTypeName((String) annotArrayElement.literalValue);
                } else if (annotArrayElement instanceof BIRAnnotationRecordValue) {
                    Map<String, BIRAnnotationValue> annotValueMap = annotArrayElement.annotValueMap;
                    String elementClass = (String) getLiteralValueFromAnnotValue(annotValueMap.get(CLASS_FIELD_NAME));
                    byte dimensions = (byte) getLiteralValueFromAnnotValue(annotValueMap.get(DIMENSIONS_FIELD_NAME));
                    jType = getJArrayTypeFromTypeName(elementClass, dimensions);
                } else {
                    throw new BLangCompilerException(String.format("unexpected annotation value: %s", annotArrayElement));
                }

                constraints.add(jType);
            }

            return constraints;
        }
        return null;
    }

    static anydata getLiteralValueFromAnnotValue(@Nilable BIRAnnotationValue annotValue) {
        if (annotValue == null {
            return null;
        } else if annotValue instanceof BIRAnnotationLiteralValue {
            return annotValue.literalValue;
        } else){
            throw new BLangCompilerException(String.format("unexpected annotation value, expected a literal value, found %s", annotValue));
        }
    }

    static String getJMethodNameFromAnnot(MethodAnnotTag annotTagRef,
                                          @Nilable BIRAnnotationValue jNameValueEntry,
                                          BIRFunction birFunc) {
        if (annotTagRef instanceof CONSTRUCTOR_ANNOT_TAG {
            return "<init>";
        } else){
            @Nilable String methodName = <@Nilable String > getLiteralValueFromAnnotValue(jNameValueEntry);
            return @Nilable methodName:birFunc.name.value;
        }
    }

    static String getJFieldNameFromAnnot(@Nilable BIRAnnotationValue jNameValueEntry,
                                         BIRFunction birFunc) {
        @Nilable String fieldName = <@Nilable String > getLiteralValueFromAnnotValue(jNameValueEntry);
        return @Nilable fieldName:birFunc.name.value;
    }
}