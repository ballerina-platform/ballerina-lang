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
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.listener;

import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.Server;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.TestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * TestNg listener to start and stop a server before all server connector classes are executed for integration test.
 * This class should be registered in testng.xml under listener section.
 */
public class ServerConnectorExecutionListener implements IExecutionListener {
    
    private static final Logger log = LoggerFactory.getLogger(ServerConnectorExecutionListener.class);
    private static Server connectorServer;
    private static final String HTTP2_ENABLED_NETTY_CONF = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "http2" + File.separator + "conf" + File.separator + "netty-transports.yml";
    private static final String SERVER_NETTY_CONF_PATH = File.separator + "bre" + File.separator + "conf" + File
            .separator + "netty-transports.yml";

    @Override
    public void onExecutionStart() {
        //path of the zip file distribution
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        connectorServer = new ServerInstance(serverZipPath, TestConstant.SERVER_CONNECTOR_TEST_PORT) {
            //config the service files need to be deployed
            @Override
            protected void configServer() throws IOException {
                //path of the sample bal file directory
                String serviceSampleDir = this.getServerHome() + File.separator + Constant.SERVICE_SAMPLE_DIR;
                //list of sample bal files to be deploy
                String[] serviceFilesArr = listSamples(serviceSampleDir);
                setArguments(serviceFilesArr);
                // copy http2 enabled netty-transports.yml file
                copyFile(new File(HTTP2_ENABLED_NETTY_CONF), new File(this.getServerHome() + SERVER_NETTY_CONF_PATH));
            }
        };
        try {
            connectorServer.start();
        } catch (Exception e) {
            log.error("Server failed to start. " + e.getMessage(), e);
            throw new RuntimeException("Server failed to start. " + e.getMessage(), e);
        }
    }

    @Override
    public void onExecutionFinish() {
        if (connectorServer != null && connectorServer.isRunning()) {
            try {
                connectorServer.stop();
            } catch (Exception e) {
                log.error("Server failed to stop. " + e.getMessage(), e);
                throw new RuntimeException("Server failed to stop. " + e.getMessage(), e);
            }
        }
    }

    /**
     * To het the server instance started by listener.
     *
     * @return up and running server instance.
     */
    public static Server getServerInstance() {
        if (connectorServer == null || !connectorServer.isRunning()) {
            throw new RuntimeException("Server in not started Properly");
        }
        return connectorServer;
    }

    /**
     * Used to copy conf file
     *
     * @param source source file
     * @param dest   destination file
     * @throws IOException throws when file copying failed
     */
    private void copyFile(File source, File dest)
            throws IOException {
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * List given samples ballerina services.
     *
     * @param sampleDir sample directory
     * @return String arrays of file absolute paths
     */
    protected static String[] listSamples(String sampleDir) {
        String[] sampleFiles = TestExecutionListener.listSamples(sampleDir);
        //TODO Add custom samples here
        return sampleFiles;
    }
}
