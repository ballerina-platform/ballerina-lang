/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.ws.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;

/**
 * Abstract class for WebSocket actions.
 */
public abstract class AbstractNativeWsAction extends AbstractNativeAction {

    public BStruct createWsConnectionStruct(Context context, Session session, String parentConnectionID) {

        //gather package details from natives
        PackageInfo wsConnectionPackageInfo = context.getProgramFile().getPackageInfo(Constants.PROTOCOL_PACKAGE_WS);
        StructInfo wsConnectionStructInfo =
                wsConnectionPackageInfo.getStructInfo(Constants.STRUCT_WEBSOCKET_CONNECTION);

        //create session struct
        BStructType structType = wsConnectionStructInfo.getType();
        BStruct wsConnection = new BStruct(structType);

        wsConnection.addNativeData(Constants.NATIVE_DATA_WEBSOCKET_SESSION, session);
        wsConnection.addNativeData(Constants.NATIVE_DATA_PARENT_CONNECTION_ID, parentConnectionID);
        return wsConnection;
    }

    public BStruct createWsErrorStruct(Context context, Throwable throwable) {

        //gather package details from natives
        PackageInfo wsErrorPackageInfo = context.getProgramFile().getPackageInfo(Constants.PROTOCOL_PACKAGE_WS);
        StructInfo wsConnectionStructInfo =
                wsErrorPackageInfo.getStructInfo(Constants.STRUCT_WEBSOCKET_ERROR);

        //create error struct
        BStructType structType = wsConnectionStructInfo.getType();
        BStruct wsError = new BStruct(structType);
        wsError.setStringField(0, throwable.getMessage());
        return wsError;
    }

    public String[] getSubProtocols(BRefType<BString[]> bSubProtocolsRefType) {
        BString[] bSubProtocols = bSubProtocolsRefType.value();
        String[] arr = Arrays.stream(bSubProtocols).map(BString::stringValue).toArray(String[]::new);
        return arr;
    }

    public Map<String, String> getCustomHeaders(BRefType<BMap<BString, BString>> bCustomHeaders) {
        Map<String, String> customHeaders = new HashMap<>();
        BMap<BString, BString> bHeadersMap = bCustomHeaders.value();
        bHeadersMap.keySet().forEach(
                key -> customHeaders.put(key.stringValue(), bHeadersMap.get(key).stringValue())
        );
        return customHeaders;
    }

    public String getUrlFromConnector(BConnector bConnector) {
        return bConnector.getStringField(0);
    }

    public String getClientServiceNameFromConnector(BConnector bConnector) {
        return bConnector.getStringField(1);
    }
}
