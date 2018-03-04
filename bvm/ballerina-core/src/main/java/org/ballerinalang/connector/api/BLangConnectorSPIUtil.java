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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.connector.api;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.BServiceType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Utils for accessing runtime information for Ballerina Connector SPI.
 *
 * @since 0.965.0
 */
public final class BLangConnectorSPIUtil {

    /**
     * Get ConnectorEndPoint struct.
     *
     * @param context current invocation context.
     * @return ConnectorEndPoint struct.
     */
    public static Struct getConnectorEndpointStruct(Context context) {
        BValue result = context.getControlStack().getCurrentFrame().getRefRegs()[1];
        if (result == null || result.getType().getTag() != TypeTags.STRUCT_TAG) {
            throw new BallerinaException("Can't get connector endpoint struct");
        }
        return ConnectorSPIModelHelper.createStruct((BStruct) result);
    }

    /**
     * Returns Service registered.
     *
     * Note: Call this util method when service is required, in register server connector SPI function.
     *
     * @param context invocation Context
     * @return register service.
     */
    public static Service getServiceRegisted(Context context) {
        BValue result = context.getControlStack().getCurrentFrame().getRefRegs()[1];
        if (result == null || result.getType().getTag() != TypeTags.TYPE_TAG
                || ((BTypeValue) result).value().getTag() != TypeTags.SERVICE_TAG) {
            throw new BallerinaException("Can't get service reference");
        }
        final BServiceType serviceType = (BServiceType) ((BTypeValue) result).value();
        final ProgramFile programFile = context.getProgramFile();
        final ServiceInfo serviceInfo = programFile.getPackageInfo(serviceType.getPackagePath())
                .getServiceInfo(serviceType.getName());
        return ConnectorSPIModelHelper.createService(programFile, serviceInfo);
    }

}
