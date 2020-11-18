/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;

import java.util.List;

import static org.ballerinalang.net.http.compiler.ResourceSignatureValidator.COMPULSORY_PARAM_COUNT;

/**
 * This class holds the resource signature parameters.
 *
 * @since 0.963.0
 */
public class SignatureParams {

    private HttpResource resource;
    private final List<Type> paramTypes;
    private Type entityBody;
    private List<Type> pathParamTypes;
    private int paramCount = COMPULSORY_PARAM_COUNT;

    SignatureParams(HttpResource resource) {
        this.resource = resource;
        this.paramTypes = resource.getParamTypes();
    }

    void validate() {
        if (resource.getEntityBodyAttributeValue() == null ||
                resource.getEntityBodyAttributeValue().isEmpty()) {
            validatePathParam(paramTypes.subList(COMPULSORY_PARAM_COUNT, paramTypes.size()));
        } else {
            int lastParamIndex = paramTypes.size() - 1;
            validatePathParam(paramTypes.subList(COMPULSORY_PARAM_COUNT, lastParamIndex));
            validateEntityBodyParam(paramTypes.get(lastParamIndex));
        }
    }

    private void validatePathParam(List<Type> paramDetails) {
        for (Type paramType : paramDetails) {
            int varTag = paramType.getTag();
            if (varTag != TypeTags.STRING_TAG && varTag != TypeTags.INT_TAG && varTag != TypeTags.BOOLEAN_TAG &&
                    varTag != TypeTags.FLOAT_TAG) {
                throw HttpUtil.createHttpError("incompatible resource signature parameter type",
                                               HttpErrorType.GENERIC_LISTENER_ERROR);
            }
            paramCount++;
        }
        this.pathParamTypes = paramDetails;
    }

    private void validateEntityBodyParam(Type entityBodyParamType) {
        int type = entityBodyParamType.getTag();
        if (type == TypeTags.RECORD_TYPE_TAG || type == TypeTags.JSON_TAG || type == TypeTags.XML_TAG ||
                type == TypeTags.STRING_TAG || (type == TypeTags.ARRAY_TAG && validArrayType(entityBodyParamType))) {
            this.entityBody = entityBodyParamType;
            paramCount++;
        } else {
            throw HttpUtil.createHttpError("incompatible entity-body type : " + entityBodyParamType.getName(),
                                           HttpErrorType.GENERIC_LISTENER_ERROR);
        }
    }

    /**
     * Check the validity of array type in data binding scenario.
     *
     * @param entityBodyParamType Represents resource parameter details
     * @return a boolean indicating the validity of the array type
     */
    private boolean validArrayType(Type entityBodyParamType) {
        return ((ArrayType) entityBodyParamType).getElementType().getTag() == TypeTags.BYTE_TAG ||
                ((ArrayType) entityBodyParamType).getElementType().getTag() == TypeTags.RECORD_TYPE_TAG;
    }

    Type getEntityBody() {
        return entityBody;
    }

    List<Type> getPathParamTypes() {
        return pathParamTypes;
    }

    int getParamCount() {
        return paramCount;
    }
}
