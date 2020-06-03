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
package org.ballerinalang.bindgen.model;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

import static org.ballerinalang.bindgen.utils.BindgenConstants.ACCESS_FIELD;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ACCESS_FIELD_INTEROP_TYPE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MUTATE_FIELD;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MUTATE_FIELD_INTEROP_TYPE;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaHandleType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaParamType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isStaticField;

/**
 * Class for storing details pertaining to a specific Java field used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JField {

    private String fieldName;
    private String fieldType;
    private String interopType;
    private String externalType;
    private String fieldMethodName;

    private boolean isArray;
    private boolean isStatic;
    private boolean isString;
    private boolean isObject = true;
    private boolean isSetter = false;
    private boolean returnError = false;

    private JParameter fieldObj;

    JField(Field field, String fieldKind) {
        Class type = field.getType();
        fieldType = getBallerinaParamType(type);
        externalType = getBallerinaHandleType(type);
        isStatic = isStaticField(field);
        fieldName = field.getName();
        fieldObj = new JParameter(type);
        fieldObj.setHasNext(false);

        if (type.isPrimitive() || type.equals(String.class)) {
            isObject = false;
        }
        if (fieldType.equals(BALLERINA_STRING)) {
            isString = true;
        }
        if (type.isArray()) {
            isArray = true;
            returnError = true;
            if (!type.getComponentType().isPrimitive()) {
                isObject = false;
            }
        }

        if (fieldKind.equals(ACCESS_FIELD)) {
            fieldMethodName = "get" + StringUtils.capitalize(fieldName);
            interopType = ACCESS_FIELD_INTEROP_TYPE;
        } else if (fieldKind.equals(MUTATE_FIELD)) {
            fieldMethodName = "set" + StringUtils.capitalize(fieldName);
            interopType = MUTATE_FIELD_INTEROP_TYPE;
            isSetter = true;
        }
    }

    public boolean isString() {
        return isString;
    }

    public String getExternalType() {
        return externalType;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isSetter() {
        return isSetter;
    }
}
