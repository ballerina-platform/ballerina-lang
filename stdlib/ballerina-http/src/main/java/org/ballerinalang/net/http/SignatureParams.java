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

/**
 * This class holds the resource signature parameters.
 *
 * @since 0.963.0
 */
public class SignatureParams {

    private static final int compulsoryParamCount = 2;
    private HttpResource resource;
    private final List<ParamDetail> paramDetails;
    private ParamDetail entityBody;
    private List<ParamDetail> pathParams;
    private int paramCount = compulsoryParamCount;

    SignatureParams(HttpResource resource, List<ParamDetail> paramDetails) {
        this.resource = resource;
        this.paramDetails = paramDetails;
    }

    void validate() {
        if (paramDetails.size() < compulsoryParamCount) {
            throw new BallerinaConnectorException("resource signature parameter count should be >= 2");
        }
        if (!isValidResourceParam(paramDetails.get(0), HttpConstants.CONNECTION)) {
            throw new BallerinaConnectorException("first parameter should be of type - "
                    + HttpConstants.PROTOCOL_PACKAGE_HTTP + ":" + HttpConstants.CONNECTION);
        }
        if (!isValidResourceParam(paramDetails.get(1), HttpConstants.IN_REQUEST)) {
            throw new BallerinaConnectorException("second parameter should be of type - "
                    + HttpConstants.PROTOCOL_PACKAGE_HTTP + ":" + HttpConstants.IN_REQUEST);
        }
        if (paramDetails.size() == compulsoryParamCount) {
            return;
        }

        if (resource.getEntityBodyAttributeValue() == null) {
            validatePathParam(paramDetails.subList(compulsoryParamCount, paramDetails.size()));
        } else {
            int lastParamIndex = paramDetails.size() - 1;
            validatePathParam(paramDetails.subList(compulsoryParamCount, lastParamIndex));
            validateEntityBodyParam(paramDetails.get(lastParamIndex));
        }
    }

    private boolean isValidResourceParam(ParamDetail paramDetail, String varTypeName) {
        String packagePath = paramDetail.getVarType().getPackagePath();
        return packagePath != null && packagePath.equals(HttpConstants.PROTOCOL_PACKAGE_HTTP)
                && paramDetail.getVarType().getName().equals(varTypeName);
    }

    private void validatePathParam(List<ParamDetail> paramDetails) {
        for (ParamDetail param : paramDetails) {
            if (param.getVarType().getTag() != TypeTags.STRING_TAG) {
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
