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

package org.ballerinalang.langlib.java;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypedescType;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import static io.ballerina.runtime.api.ErrorCreator.createError;
import static io.ballerina.runtime.api.ValueCreator.createHandleValue;
import static io.ballerina.runtime.api.ValueCreator.createObjectValue;

/**
 * This class contains the implementation of the "cast" Ballerina function in ballerina/java module.
 *
 * @since 1.2.5
 */
public class Cast {

    private static final String moduleName = "{ballerina/java}";
    private static final String annotationName = "@java:Binding";
    private static final String annotationType = "ballerina/java:0.9.0:Binding";
    private static final String classAttribute = "class";
    private static final String jObjField = "jObj";

    public static Object cast(BObject value, BTypedesc castType) {
        BHandle handleObj;
        ObjectType objType = value.getType();
        String valueObjName = objType.getName();
        handleObj = (BHandle) value.get(StringUtils.fromString(jObjField));
        Object jObj = handleObj.getValue();
        if (jObj == null) {
            return createError(StringUtils.fromString(moduleName + " Empty handle reference found for `"
                    + jObjField + "` field in `" + valueObjName + "`"));
        }
        try {
            BMap objAnnotation;
            BString objClass;
            try {
                objAnnotation = (BMap) objType.getAnnotation(StringUtils.fromString(annotationType));
                objClass = objAnnotation.getStringValue(StringUtils.fromString(classAttribute));
            } catch (Exception e) {
                return createError(StringUtils.fromString(moduleName + " Error while retrieving details of the `" +
                        annotationName + "` annotation from `" + valueObjName + "` object: " + e));
            }
            Type describingBType = castType.getDescribingType();
            BString castObjClass;
            ObjectType castObjType;
            String castObjTypeName;
            try {
                TypedescType describingType = (TypedescType) describingBType;
                castObjType = (ObjectType) describingType.getConstraint();
                castObjTypeName = castObjType.getName();
                Field objField = castObjType.getFields().get(jObjField);
                if (objField == null) {
                    return createError(StringUtils.fromString(moduleName + " Handle reference field `" + jObjField +
                            "` not found in the typedesc object"));
                }
            } catch (Exception e) {
                return createError(StringUtils.fromString(moduleName + " Error while processing the typedesc " +
                        "parameter: " + e));
            }
            try {
                BMap castObjAnnotation = (BMap) castObjType.getAnnotation(
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
                    bObject = createObjectValue(objType.getPackage(), castObjType.getName(), createHandleValue(jObj));
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
