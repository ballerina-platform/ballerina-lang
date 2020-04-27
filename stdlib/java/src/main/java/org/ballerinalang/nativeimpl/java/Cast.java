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

package org.ballerinalang.nativeimpl.java;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BObject;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Map;

import static org.ballerinalang.jvm.BallerinaErrors.createError;
import static org.ballerinalang.jvm.values.api.BValueCreator.createObjectValue;

/**
 * This class contains the implementation of the "cast" Ballerina function in ballerina/java module.
 *
 * @since 1.2.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "java", functionName = "cast",
        args = {
                @Argument(name = "value", type = TypeKind.ANY),
                @Argument(name = "castType", type = TypeKind.TYPEDESC)
        },
        returnType = {
                @ReturnType(type = TypeKind.ANY),
                @ReturnType(type = TypeKind.ERROR)
        },
        isPublic = true
)
public class Cast {

    private static final String moduleName = "{ballerina/java}";
    private static final String annotationName = "@java:Binding";
    private static final String annotationType = "ballerina/java:Binding";
    private static final String classAttribute = "class";
    private static final String jObjField = "jObj";

    public static Object cast(Strand strand, Object value, TypedescValue castType) {
        BObject valueObj;
        String valueObjName;
        try {
            valueObj = (BObject) value;
            valueObjName = valueObj.getType().getName();
        } catch (Exception e) {
            return createError(StringUtils.fromString(moduleName + " " + e.getMessage()));
        }
        HandleValue handleObj;
        try {
            handleObj = (HandleValue) valueObj.get(jObjField);
        } catch (Exception e) {
            return createError(StringUtils.fromString(moduleName + " " + e.getMessage() + " in " + valueObjName));
        }
        Object jObj = handleObj.getValue();
        if (jObj != null) {
            BObjectType objType = valueObj.getType();
            try {
                if (objType != null) {
                    Map objAnnotation;
                    String objClass;
                    try {
                        objAnnotation = (Map) objType.getAnnotation(annotationType);
                        objClass = (String) objAnnotation.get(classAttribute);
                    } catch (Exception e) {
                        return createError(StringUtils.fromString(moduleName + " Error while retrieving details " +
                                "of the `" + annotationName + "` annotation from " + valueObjName + " object: " + e));
                    }
                    BType describingBType = castType.getDescribingType();
                    if (describingBType != null) {
                        String castObjClass;
                        BObjectType castObjType;
                        String castObjTypeName;
                        try {
                            BTypedescType describingType = (BTypedescType) describingBType;
                            castObjType = (BObjectType) describingType.getConstraint();
                            castObjTypeName = castObjType.getName();
                        } catch (Exception e) {
                            return createError(StringUtils.fromString(moduleName + " Error while processing the " +
                                    "typedesc parameter: " + e));
                        }
                        try {
                            Map castObjAnnotation = (Map) castObjType.getAnnotation(annotationType);
                            castObjClass = (String) castObjAnnotation.get(classAttribute);
                        } catch (Exception e) {
                            return createError(StringUtils.fromString(moduleName + " Error while retrieving " +
                                    "details of the `" + annotationName + "` annotation from " + castObjTypeName +
                                    " typedesc: " + e));
                        }
                        Class<?> objClassType = Class.forName(objClass);
                        Class<?> castObjClassType = Class.forName(castObjClass);
                        boolean isList = objClassType.isAssignableFrom(castObjClassType);
                        if (isList) {
                            return createObjectValue(objType.getPackage(), castObjType.getName(),
                                    new HandleValue(jObj));
                        } else {
                            return createError(StringUtils.fromString(moduleName + " Cannot cast " + valueObjName +
                                    " to " + castObjTypeName));
                        }
                    }
                }
            } catch (Exception e) {
                return createError(StringUtils.fromString(moduleName + " Error while casting " + valueObjName +
                        " object to the typedesc provided: " + e));
            }
        } else {
            return createError(StringUtils.fromString(moduleName + " Empty handle reference found for `"
                    + jObjField + "` field in " + valueObjName));
        }
        return createError(StringUtils.fromString(moduleName + " Error while casting the " + valueObjName +
                " object"));
    }
}
