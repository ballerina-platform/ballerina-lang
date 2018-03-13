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

package org.ballerinalang.net.http.actions.websocketconnector;

import org.ballerinalang.model.NativeCallableUnit;

/**
 * Abstract class for WebSocket actions.
 */
public abstract class AbstractNativeWsAction implements NativeCallableUnit {

//    public BStruct createWsConnectionStruct(WebSocketService wsService, Session session, String parentConnectionID) {
//        BStruct wsConnection = wsService.createConnectionStruct();
//        wsConnection.addNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_SESSION, session);
//        wsConnection.addNativeData(WebSocketConstants.NATIVE_DATA_PARENT_CONNECTION_ID, parentConnectionID);
//        return wsConnection;
//    }
//
//    public BStruct createWsErrorStruct(Context context, Throwable throwable) {
//
//        //gather package details from natives
//        PackageInfo wsErrorPackageInfo = context.getProgramFile().getPackageInfo(
//                WebSocketConstants.PROTOCOL_PACKAGE_WS);
//        StructInfo wsConnectionStructInfo =
//                wsErrorPackageInfo.getStructInfo(WebSocketConstants.STRUCT_WEBSOCKET_ERROR);
//
//        //create error struct
//        BStructType structType = wsConnectionStructInfo.getType();
//        BStruct wsError = new BStruct(structType);
//        wsError.setStringField(0, throwable.getMessage());
//        return wsError;
//    }
//
//    public String[] getSubProtocols(BRefType<BString[]> bSubProtocolsRefType) {
//        BString[] bSubProtocols = bSubProtocolsRefType.value();
//        return Arrays.stream(bSubProtocols).map(BString::stringValue).toArray(String[]::new);
//    }
//
//    public Map<String, String> getCustomHeaders(BRefType<BMap<BString, BString>> bCustomHeaders) {
//        Map<String, String> customHeaders = new HashMap<>();
//        BMap<BString, BString> bHeadersMap = bCustomHeaders.value();
//        bHeadersMap.keySet().forEach(
//                key -> customHeaders.put(key.stringValue(), bHeadersMap.get(key).stringValue())
//        );
//        return customHeaders;
//    }
//
//    public boolean isBlocking() {
//        return false;
//    }
//
//    public String getUrlFromConnector(BConnector bConnector) {
//        return bConnector.getStringField(0);
//    }
//
//    public String getClientServiceNameFromConnector(BConnector bConnector) {
//        return bConnector.getStringField(1);
//    }
}
