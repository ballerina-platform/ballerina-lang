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
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationArrayValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationLiteralValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationRecordValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationValue;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.getJArrayTypeFromTypeName;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JType.getJTypeFromTypeName;

/**
 * JInterop related annotation processing methods.
 *
 * @since 1.2.0
 */
public class AnnotationProc {

    public static final String NAME_FIELD_NAME = "name";
    public static final String CLASS_FIELD_NAME = "class";
    public static final String FIELD_METHOD_FIELD_NAME = "method";
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

        return INTEROP_ANNOT_ORG.equals(annotAttach.packageID.orgName.value) &&
                INTEROP_ANNOT_MODULE.equals(annotAttach.packageID.name.value) &&
                isInteropAnnotationTag(annotAttach.annotTagRef.value);
    }

    private static InteropValidationRequest createJInteropValidationRequest(String annotTagRef,
                                                                            BIRAnnotationAttachment annotAttach,
                                                                            BIRFunction birFunc) {

        BIRAnnotationRecordValue annotRecValue = (BIRAnnotationRecordValue) annotAttach.annotValues.get(0);
        Map<String, BIRAnnotationValue> annotValueMap = annotRecValue.annotValueEntryMap;
        if (isMethodAnnotationTag(annotTagRef)) {
            return createJMethodValidationRequest(annotTagRef, annotValueMap, birFunc);
        } else {
            return createJFieldValidationRequest(annotTagRef, annotValueMap, birFunc);
        }
    }

    private static InteropValidationRequest createJMethodValidationRequest(String annotTagRef,
                                                                           Map<String, BIRAnnotationValue> annotValues,
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
                                                                          Map<String, BIRAnnotationValue> annotValues,
                                                                          BIRFunction birFunc) {

        return new InteropValidationRequest.FieldValidationRequest(
                getJFieldNameFromAnnot(annotValues.get(NAME_FIELD_NAME), birFunc),
                (String) getLiteralValueFromAnnotValue(annotValues.get(CLASS_FIELD_NAME)),
                birFunc.type, getFieldMethodFromAnnotTag(annotTagRef));
    }

    private static List<JType> buildParamTypeConstraints(BIRAnnotationValue annotValue) {

        if (annotValue instanceof BIRAnnotationArrayValue) {
            BIRAnnotationValue[] annotArrayElements = ((BIRAnnotationArrayValue) annotValue).annotArrayValue;
            List<JType> constraints = new ArrayList<>();
            for (BIRAnnotationValue annotArrayElement : annotArrayElements) {
                JType jType;
                if (annotArrayElement instanceof BIRAnnotationLiteralValue) {
                    jType = getJTypeFromTypeName((String) ((BIRAnnotationLiteralValue) annotArrayElement).value);
                } else if (annotArrayElement instanceof BIRAnnotationRecordValue) {
                    Map<String, BIRAnnotationValue> annotValueMap =
                            ((BIRAnnotationRecordValue) annotArrayElement).annotValueEntryMap;
                    String elementClass = (String) getLiteralValueFromAnnotValue(annotValueMap.get(CLASS_FIELD_NAME));
                    byte dimensions = ((Long) getLiteralValueFromAnnotValue(annotValueMap.get(DIMENSIONS_FIELD_NAME)))
                            .byteValue();
                    jType = getJArrayTypeFromTypeName(elementClass, dimensions);
                } else {
                    throw new BLangCompilerException(String.format("unexpected annotation value: %s",
                            annotArrayElement));
                }
                constraints.add(jType);
            }

            return constraints;
        }
        return null;
    }

    private static Object getLiteralValueFromAnnotValue(BIRAnnotationValue annotValue) {

        if (annotValue == null) {
            return null;
        } else if (annotValue instanceof BIRAnnotationLiteralValue) {
            return ((BIRAnnotationLiteralValue) annotValue).value;
        } else {
            throw new BLangCompilerException(String.format("unexpected annotation value, " +
                    "expected a literal value, found %s", annotValue));
        }
    }

    private static String getJMethodNameFromAnnot(String annotTagRef,
                                                  BIRAnnotationValue jNameValueEntry,
                                                  BIRFunction birFunc) {

        if (annotTagRef.equals(CONSTRUCTOR_ANNOT_TAG)) {
            return JVM_INIT_METHOD;
        } else {
            String methodName = (String) getLiteralValueFromAnnotValue(jNameValueEntry);
            return methodName != null ? methodName : birFunc.name.value;
        }
    }

    private static String getJFieldNameFromAnnot(BIRAnnotationValue jNameValueEntry,
                                                 BIRFunction birFunc) {

        String fieldName = (String) getLiteralValueFromAnnotValue(jNameValueEntry);
        return fieldName != null ? fieldName : birFunc.name.value;
    }
}
