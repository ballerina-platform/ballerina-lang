/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.mockerina;

import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Keep a registry of {@code {@link BallerinaFile}} instances.
 * This is required to modify the runtime behavior.
 */
public class MockerinaRegistry {
    private static Map<BallerinaFile, BallerinaFile> bFiles = new HashMap<>();
    private static Map<String, Service> services = new HashMap<>(); //todo remove?
    private static final MockerinaRegistry instance = new MockerinaRegistry();

    public static MockerinaRegistry getInstance() {
        return instance;
    }

    public void addBallerinaFile(BallerinaFile originalBFile, BallerinaFile derivativeBFile) {
        bFiles.put(originalBFile, derivativeBFile);
    }

    public BallerinaFile[] getOriginalBallerinaFiles() {
        return bFiles.keySet().toArray(new BallerinaFile[bFiles.size()]);
    }

    public BallerinaFile getDerivativeBallerinaFile(BallerinaFile bFile) {
        return bFiles.get(bFile);
    }

    public BallerinaFile resolve(Service service) {
        return bFiles.keySet().stream().filter(ballerinaFile ->
                Arrays.stream(ballerinaFile.getServices()).anyMatch(s -> s.equals(service)))
                .findAny().orElse(null);
    }

    public void addService(Service service) {
        services.put(service.getName(), service);
    }

    public Service getService(String serviceName) {
        return services.get(serviceName);
    }
}
