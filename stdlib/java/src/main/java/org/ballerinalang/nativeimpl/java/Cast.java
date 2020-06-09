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
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.api.BObject;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.BallerinaErrors.createError;
import static org.ballerinalang.jvm.values.api.BValueCreator.createObjectValue;

/**
 * This class contains the implementation of the "cast" Ballerina function in ballerina/java module.
 *
 * @since 1.2.5
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "java", version = "0.9.0", functionName = "cast",
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
    private static final String annotationType = "ballerina/java:0.9.0:Binding";
    private static final String classAttribute = "class";
    private static final String jObjField = "jObj";

    public static Object cast(Strand strand, ObjectValue value, TypedescValue castType) {
        HandleValue handleObj;
        BObjectType objType = value.getType();
        String valueObjName = objType.getName();
        handleObj = (HandleValue) value.get(StringUtils.fromString(jObjField));
        Object jObj = handleObj.getValue();
        if (jObj == null) {
            return createError(StringUtils.fromString(moduleName + " Empty handle reference found for `"
                    + jObjField + "` field in `" + valueObjName + "`"));
        }
        try {
            MapValue objAnnotation;
            BString objClass;
            try {
                objAnnotation = (MapValue) objType.getAnnotation(StringUtils.fromString(annotationType));
                objClass = objAnnotation.getStringValue(StringUtils.fromString(classAttribute));
            } catch (Exception e) {
                return createError(StringUtils.fromString(moduleName + " Error while retrieving details of the `" +
                        annotationName + "` annotation from `" + valueObjName + "` object: " + e));
            }
            BType describingBType = castType.getDescribingType();
            BString castObjClass;
            BObjectType castObjType;
            String castObjTypeName;
            try {
                BTypedescType describingType = (BTypedescType) describingBType;
                castObjType = (BObjectType) describingType.getConstraint();
                castObjTypeName = castObjType.getName();
                BField objField = castObjType.getFields().get(jObjField);
                if (objField == null) {
                    return createError(StringUtils.fromString(moduleName + " Handle reference field `" + jObjField +
                            "` not found in the typedesc object"));
                }
            } catch (Exception e) {
                return createError(StringUtils.fromString(moduleName + " Error while processing the typedesc " +
                        "parameter: " + e));
            }
            try {
                MapValue castObjAnnotation = (MapValue) castObjType.getAnnotation(
                        StringUtils.fromString(annotationType));
                castObjClass = (BString) castObjAnnotation.getStringValue(StringUtils.fromString(classAttribute));
            } catch (Exception e) {
                return createError(StringUtils.fromString(moduleName + " Error while retrieving details of the `" +
                        annotationName + "` annotation from `" + castObjTypeName + "` typedesc: " + e));
            }
            Class<?> objClassType = Class.forName(objClass.getValue());
            Class<?> castObjClassType = Class.forName(castObjClass.getValue());
            boolean isList = objClassType.isAssignableFrom(castObjClassType);
            if (isList) {
                BObject bObject;
                try {
                    bObject = createObjectValue(objType.getPackage(), castObjType.getName(), new HandleValue(jObj));
                } catch (Exception e) {
                    return createError(StringUtils.fromString(moduleName + " Error while initializing the new " +
                            "object from `" + castObjTypeName + "` type: " + e));
                }
                return bObject;
            } else {
                return createError(StringUtils.fromString(moduleName + " Cannot cast `" + valueObjName + "` to `" +
                        castObjTypeName + "`"));
            }
        } catch (Exception e) {
            return createError(StringUtils.fromString(moduleName + " Error while casting `" + valueObjName +
                    "` object to the typedesc provided: " + e));
        }
    }
}
