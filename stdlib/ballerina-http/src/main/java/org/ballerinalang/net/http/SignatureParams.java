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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.model.types.TypeTags;

import java.util.List;

import static org.ballerinalang.net.http.compiler.ResourceSignatureValidator.COMPULSORY_PARAM_COUNT;

/**
 * This class holds the resource signature parameters.
 *
 * @since 0.963.0
 */
public class SignatureParams {

    private HttpResource resource;
    private final List<ParamDetail> paramDetails;
    private ParamDetail entityBody;
    private List<ParamDetail> pathParams;
    private int paramCount = COMPULSORY_PARAM_COUNT;

    SignatureParams(HttpResource resource, List<ParamDetail> paramDetails) {
        this.resource = resource;
        this.paramDetails = paramDetails;
    }

    void validate() {
        if (resource.getEntityBodyAttributeValue() == null ||
                resource.getEntityBodyAttributeValue().isEmpty()) {
            validatePathParam(paramDetails.subList(COMPULSORY_PARAM_COUNT, paramDetails.size()));
        } else {
            int lastParamIndex = paramDetails.size() - 1;
            validatePathParam(paramDetails.subList(COMPULSORY_PARAM_COUNT, lastParamIndex));
            validateEntityBodyParam(paramDetails.get(lastParamIndex));
        }
    }

    private void validatePathParam(List<ParamDetail> paramDetails) {
        for (ParamDetail param : paramDetails) {
            int varTag = param.getVarType().getTag();
            if (varTag != TypeTags.STRING_TAG && varTag != TypeTags.INT_TAG && varTag != TypeTags.BOOLEAN_TAG &&
                    varTag != TypeTags.FLOAT_TAG) {
                throw new BallerinaConnectorException("incompatible resource signature parameter type");
            }
            paramCount++;
        }
        this.pathParams = paramDetails;
    }

    private void validateEntityBodyParam(ParamDetail entityBodyParam) {
        String entityBodyAttributeValue = resource.getEntityBodyAttributeValue();
        if (!entityBodyAttributeValue.equals(entityBodyParam.getVarName())) {
            throw new BallerinaConnectorException("expected '" + entityBodyAttributeValue +
                    "' as param name, but found '" + entityBodyParam.getVarName() + "'");
        }
        int type = entityBodyParam.getVarType().getTag();
        if (type == TypeTags.STRUCT_TAG || type == TypeTags.JSON_TAG || type == TypeTags.XML_TAG ||
                type == TypeTags.STRING_TAG || type == TypeTags.BLOB_TAG) {
            this.entityBody = entityBodyParam;
            paramCount++;
        } else {
            throw new BallerinaConnectorException("incompatible entity-body type : " +
                    entityBodyParam.getVarType().getName());
        }
    }

    ParamDetail getEntityBody() {
        return entityBody;
    }

    List<ParamDetail> getPathParams() {
        return pathParams;
    }

    int getParamCount() {
        return paramCount;
    }
}
