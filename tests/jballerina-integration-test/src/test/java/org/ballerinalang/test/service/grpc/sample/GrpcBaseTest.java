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

package org.ballerinalang.test.service.grpc.sample;

import org.apache.commons.text.StringEscapeUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

import java.io.File;
import java.nio.file.Paths;

/**
 * Base test class for GRPC integration test cases which starts/stops the grpc services as ballerina package before
 * and after tests are run.
 */
public class GrpcBaseTest extends BaseTest {
    protected static BServerInstance serverInstance;

    @BeforeGroups(value = "grpc-test", alwaysRun = true)
    public void start() throws BallerinaTestException {
        String privateKey = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                                                                   "private.key").toAbsolutePath().toString());
        String publicCert = StringEscapeUtils.escapeJava(Paths.get("src", "test", "resources", "certsAndKeys",
                                                                   "public.crt").toAbsolutePath().toString());
        int[] requiredPorts = new int[]{9091, 9092, 9093, 9094, 9095, 9096, 9097, 9098, 9099, 9100, 9101, 9102, 9103,
                9104, 9105, 9107, 9108, 9109, 9110, 9111};

        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "grpc").getAbsolutePath();
        String keyStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaKeystore.p12").toAbsolutePath()
                        .toString());
        String trustStore = StringEscapeUtils.escapeJava(
                Paths.get("src", "test", "resources", "certsAndKeys", "ballerinaTruststore.p12").toAbsolutePath()
                        .toString());
        String[] args = new String[] { "--certificate.key=" + privateKey, "--public.cert=" + publicCert,
                "--keystore=" + keyStore, "--truststore=" + trustStore };
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, "grpcservices", null, args, requiredPorts);
    }

    @AfterGroups(value = "grpc-test", alwaysRun = true)
    public void cleanup() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }
}
