/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.bir.codegen.model.JType;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.CONSTRUCTOR_ANNOT_TAG;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.INTEROP_ANNOT_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.INTEROP_ANNOT_ORG;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.getFieldMethodFromAnnotTag;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.getMethodKindFromAnnotTag;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.isInteropAnnotationTag;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.isMethodAnnotationTag;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JType.getJArrayTypeFromTypeName;
import static org.wso2.ballerinalang.compiler.bir.codegen.model.JType.getJTypeFromTypeName;

/**
 * JInterop related annotation processing methods.
 *
 * @since 1.2.0
 */
public class AnnotationProc {

    public static final String NAME_FIELD_NAME = "name";
    public static final String CLASS_FIELD_NAME = "class";
    public static final String PARAM_TYPES_FIELD_NAME = "paramTypes";
    public static final String DIMENSIONS_FIELD_NAME = "dimensions";

    static InteropValidationRequest getInteropAnnotValue(BIRFunction birFunc) {

        BIRAnnotationAttachment annotAttach = getInteropAnnotAttachment(birFunc);
        if (annotAttach == null) {
            return null;
        }

        String annotTagRef = annotAttach.annotTagRef.value;
        return createJInteropValidationRequest(annotTagRef, annotAttach, birFunc);
    }

    private static BIRAnnotationAttachment getInteropAnnotAttachment(BIRFunction birFunc) {

        for (BIRAnnotationAttachment annotationAttachment : birFunc.annotAttachments) {
            if (isInteropAnnotAttachment(annotationAttachment)) {
                return annotationAttachment;
            }
        }
        return null;
    }

    private static boolean isInteropAnnotAttachment(BIRAnnotationAttachment annotAttach) {

        return INTEROP_ANNOT_ORG.equals(annotAttach.annotPkgId.orgName.value) &&
                INTEROP_ANNOT_MODULE.equals(annotAttach.annotPkgId.name.value) &&
                isInteropAnnotationTag(annotAttach.annotTagRef.value);
    }

    private static InteropValidationRequest createJInteropValidationRequest(String annotTagRef,
                                                                            BIRAnnotationAttachment annotAttach,
                                                                            BIRFunction birFunc) {

        BIRNode.ConstValue annotRecValue = ((BIRNode.BIRConstAnnotationAttachment) annotAttach).annotValue;
        Map<String, BIRNode.ConstValue> annotValueMap = (Map<String, BIRNode.ConstValue>) annotRecValue.value;
        if (isMethodAnnotationTag(annotTagRef)) {
            return createJMethodValidationRequest(annotTagRef, annotValueMap, birFunc);
        } else {
            return createJFieldValidationRequest(annotTagRef, annotValueMap, birFunc);
        }
    }

    private static InteropValidationRequest createJMethodValidationRequest(String annotTagRef,
                                                                           Map<String, BIRNode.ConstValue> annotValues,
                                                                           BIRFunction birFunc) {

        InteropValidationRequest.MethodValidationRequest valRequest =
                new InteropValidationRequest.MethodValidationRequest(getJMethodNameFromAnnot(annotTagRef,
                        annotValues.get(NAME_FIELD_NAME), birFunc),
                        (String) getLiteralValueFromAnnotValue(annotValues.get(CLASS_FIELD_NAME)),
                        birFunc.type,
                        getMethodKindFromAnnotTag(annotTagRef)
                );

        List<JType> paramTypeConstraints = buildParamTypeConstraints(annotValues.get(PARAM_TYPES_FIELD_NAME));
        if (!(paramTypeConstraints == null)) {
            valRequest.paramTypeConstraints = paramTypeConstraints;
        }
        if (birFunc.receiver != null) {
            valRequest.receiverType = birFunc.receiver.type;
        }
        return valRequest;
    }

    private static InteropValidationRequest createJFieldValidationRequest(String annotTagRef,
                                                                          Map<String, BIRNode.ConstValue> annotValues,
                                                                          BIRFunction birFunc) {

        return new InteropValidationRequest.FieldValidationRequest(
                getJFieldNameFromAnnot(annotValues.get(NAME_FIELD_NAME), birFunc),
                (String) getLiteralValueFromAnnotValue(annotValues.get(CLASS_FIELD_NAME)),
                birFunc.type, getFieldMethodFromAnnotTag(annotTagRef));
    }

    private static List<JType> buildParamTypeConstraints(BIRNode.ConstValue annotValue) {
        if (annotValue == null) {
            return null;
        }

        Object value = annotValue.value;

        BIRNode.ConstValue[] annotArrayElements = (BIRNode.ConstValue[]) value;
        List<JType> constraints = new ArrayList<>(annotArrayElements.length);
        for (BIRNode.ConstValue annotArrayElement : annotArrayElements) {
            JType jType;
            Object elementValue = annotArrayElement.value;
            if (elementValue instanceof String s) {
                jType = getJTypeFromTypeName(s);
            } else if (elementValue instanceof Map<?, ?> annotValueMap) {
                String elementClass = (String) getLiteralValueFromAnnotValue(
                        (BIRNode.ConstValue) annotValueMap.get(CLASS_FIELD_NAME));
                byte dimensions = ((Long) getLiteralValueFromAnnotValue(
                        (BIRNode.ConstValue) annotValueMap.get(DIMENSIONS_FIELD_NAME))).byteValue();
                jType = getJArrayTypeFromTypeName(elementClass, dimensions);
            } else {
                throw new BLangCompilerException("unexpected annotation value: " + annotArrayElement);
            }
            constraints.add(jType);
        }

        return constraints;
    }

    private static Object getLiteralValueFromAnnotValue(BIRNode.ConstValue annotValue) {
        if (annotValue == null) {
            return null;
        }

        Object value = annotValue.value;
        if (value instanceof String || value instanceof Long) {
            return value;
        } else {
            throw new BLangCompilerException("unexpected annotation value, " +
                    "expected a literal value, found " + annotValue);
        }
    }

    private static String getJMethodNameFromAnnot(String annotTagRef,
                                                  BIRNode.ConstValue jNameValueEntry,
                                                  BIRFunction birFunc) {

        if (annotTagRef.equals(CONSTRUCTOR_ANNOT_TAG)) {
            return JVM_INIT_METHOD;
        } else {
            String methodName = (String) getLiteralValueFromAnnotValue(jNameValueEntry);
            return methodName != null ? methodName : birFunc.name.value;
        }
    }

    private static String getJFieldNameFromAnnot(BIRNode.ConstValue jNameValueEntry,
                                                 BIRFunction birFunc) {

        String fieldName = (String) getLiteralValueFromAnnotValue(jNameValueEntry);
        return fieldName != null ? fieldName : birFunc.name.value;
    }
}
