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
package org.ballerinalang.bindgen.components;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

import static org.ballerinalang.bindgen.utils.BindgenConstants.ACCESS_FIELD;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ACCESS_FIELD_INTEROP_TYPE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MUTATE_FIELD;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MUTATE_FIELD_INTEROP_TYPE;
import static org.ballerinalang.bindgen.utils.BindgenUtils.balType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isStaticField;

/**
 * Class for storing details pertaining to a specific Java field used for Ballerina bridge code generation.
 */
public class JField {

    private String fieldName;
    private String returnType;
    private String interopType;
    private String fieldMethodName;

    private Boolean isStatic;
    private Boolean isSetter = false;
    private Boolean hasReturn = false;

    JField(Field f, String fieldKind) {

        this.returnType = balType(f.getType().getSimpleName());
        this.isStatic = isStaticField(f);
        this.fieldName = f.getName();
        if (fieldKind.equals(ACCESS_FIELD)) {
            this.fieldMethodName = "get" + StringUtils.capitalize(this.fieldName);
            interopType = ACCESS_FIELD_INTEROP_TYPE;
            this.hasReturn = true;
        } else if (fieldKind.equals(MUTATE_FIELD)) {
            this.fieldMethodName = "set" + StringUtils.capitalize(this.fieldName);
            interopType = MUTATE_FIELD_INTEROP_TYPE;
            this.isSetter = true;
        }
    }
}
