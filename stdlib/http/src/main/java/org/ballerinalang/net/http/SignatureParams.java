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

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;

import java.util.List;

import static org.ballerinalang.net.http.compiler.ResourceSignatureValidator.COMPULSORY_PARAM_COUNT;

/**
 * This class holds the resource signature parameters.
 *
 * @since 0.963.0
 */
public class SignatureParams {

    private HttpResource resource;
    private final List<BType> paramTypes;
    private BType entityBody;
    private List<BType> pathParamTypes;
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

    private void validatePathParam(List<BType> paramDetails) {
        for (BType paramType : paramDetails) {
            int varTag = paramType.getTag();
            if (varTag != TypeTags.STRING_TAG && varTag != TypeTags.INT_TAG && varTag != TypeTags.BOOLEAN_TAG &&
                    varTag != TypeTags.FLOAT_TAG) {
                throw new BallerinaConnectorException("incompatible resource signature parameter type");
            }
            paramCount++;
        }
        this.pathParamTypes = paramDetails;
    }

    private void validateEntityBodyParam(BType entityBodyParamType) {
        int type = entityBodyParamType.getTag();
        if (type == TypeTags.RECORD_TYPE_TAG || type == TypeTags.JSON_TAG || type == TypeTags.XML_TAG ||
                type == TypeTags.STRING_TAG || (type == TypeTags.ARRAY_TAG && validArrayType(entityBodyParamType))) {
            this.entityBody = entityBodyParamType;
            paramCount++;
        } else {
            throw new BallerinaConnectorException("incompatible entity-body type : " +
                    entityBodyParamType.getName());
        }
    }

    /**
     * Check the validity of array type in data binding scenario.
     *
     * @param entityBodyParamType Represents resource parameter details
     * @return a boolean indicating the validity of the array type
     */
    private boolean validArrayType(BType entityBodyParamType) {
        return ((BArrayType) entityBodyParamType).getElementType().getTag() == TypeTags.BYTE_TAG ||
                ((BArrayType) entityBodyParamType).getElementType().getTag() == TypeTags.RECORD_TYPE_TAG;
    }

    BType getEntityBody() {
        return entityBody;
    }

    List<BType> getPathParamTypes() {
        return pathParamTypes;
    }

    int getParamCount() {
        return paramCount;
    }
}
