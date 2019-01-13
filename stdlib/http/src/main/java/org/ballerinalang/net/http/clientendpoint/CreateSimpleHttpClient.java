/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.clientendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;

import static org.ballerinalang.net.http.HttpConstants.HTTP_CLIENT;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;
import static org.ballerinalang.net.http.HttpUtil.getHttpClientConnector;

/**
 * Initialization of client endpoint.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "createSimpleHttpClient",
        args = {@Argument(name = "uri", type = TypeKind.STRING),
                @Argument(name = "client", type = TypeKind.OBJECT, structType = "Client")},
        isPublic = true
)
public class CreateSimpleHttpClient extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> configBStruct =
                (BMap<String, BValue>) context.getRefArgument(HttpConstants.CLIENT_ENDPOINT_CONFIG_INDEX);
        Struct clientEndpointConfig = BLangConnectorSPIUtil.toStruct(configBStruct);
        String urlString = context.getStringArgument(HttpConstants.CLIENT_ENDPOINT_URL_INDEX);
        HttpClientConnector httpClientConnector = getHttpClientConnector(clientEndpointConfig, urlString);
        BMap<String, BValue> httpClient = BLangConnectorSPIUtil.createBStruct(context.getProgramFile(),
                                                                                      HTTP_PACKAGE_PATH,
                                                                                      HTTP_CLIENT, urlString,
                                                                                      clientEndpointConfig);
        httpClient.addNativeData(HttpConstants.HTTP_CLIENT, httpClientConnector);
        httpClient.addNativeData(HttpConstants.CLIENT_ENDPOINT_CONFIG, clientEndpointConfig);
        configBStruct.addNativeData(HttpConstants.HTTP_CLIENT, httpClientConnector);
        context.setReturnValues((httpClient));
    }
}
