/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.connector.api;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;

/**
 * {@code ConnectorUtil} Utilities related to connector processing.
 *
 * @since 0.94
 */
@Deprecated
public class ConnectorUtils extends ConnectorSPIModelHelper {

    /**
     * This method is used to create a struct given the context and required struct details.
     *
     * @param context to get program file.
     * @param packagePath of the struct.
     * @param structName of the struct.
     * @return created struct.
     * @throws BallerinaConnectorException if an error occurs
     */
    public static BMap<String, BValue> createAndGetStruct(Context context, String packagePath, String structName)
            throws BallerinaConnectorException {
        PackageInfo packageInfo = context.getProgramFile()
                .getPackageInfo(packagePath);
        if (packageInfo == null) {
            throw new BallerinaConnectorException("module - " + packagePath + " does not exist");
        }
        StructureTypeInfo structureInfo = packageInfo.getStructInfo(structName);
        BStructureType structType = structureInfo.getType();
        BMap<String, BValue> bStruct = new BMap<>(structType);

        return bStruct;
    }

    /**
     * This method can be used to access the {@code BallerinaServerConnector} object which is at
     * Ballerina level using programme file.
     *
     * @param programFile Program file.
     * @param protocolPkgPath Package of the registered protocol of the server connector.
     * @return BallerinaServerConnector which matches to the given protocol package.
     */
    @Deprecated
    public static BallerinaServerConnector getBallerinaServerConnector(ProgramFile programFile,
                                                                       String protocolPkgPath) {
        return programFile.getServerConnectorRegistry().getBallerinaServerConnector(protocolPkgPath);
    }

    /**
     * This method can be used to access the {@code BallerinaServerConnector} object which is at
     * Ballerina level using context of the programme.
     *
     * @param context {@link Context} of the programme.
     * @param protocolPkgPath Package of the registered protocol of the server connector.
     * @return BallerinaServerConnector which matches to the given protocol package.
     */
    @Deprecated
    public static BallerinaServerConnector getBallerinaServerConnector(Context context, String protocolPkgPath) {
        ProgramFile programFile = context.getProgramFile();
        return getBallerinaServerConnector(programFile, protocolPkgPath);
    }
}
