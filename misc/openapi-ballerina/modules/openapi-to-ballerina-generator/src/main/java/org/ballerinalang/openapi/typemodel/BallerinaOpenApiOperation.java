/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.openapi.typemodel;

import java.util.List;

/**
 * This class contains the OpenApi Operation Type Object.
 */
public class BallerinaOpenApiOperation {
    private String opMethod;
    private String opName;
    private List<BallerinaOpenApiParameter> parameterList;
    private BallerinaOpenApiRequestBody requestBody;

    public BallerinaOpenApiRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(BallerinaOpenApiRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public String getOpMethod() {
        return opMethod;
    }

    public void setOpMethod(String opMethod) {
        this.opMethod = opMethod;
    }

    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    public List<BallerinaOpenApiParameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<BallerinaOpenApiParameter> parameterList) {
        this.parameterList = parameterList;
    }
}
