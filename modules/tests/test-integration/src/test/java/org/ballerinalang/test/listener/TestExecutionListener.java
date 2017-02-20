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
package org.ballerinalang.test.listener;

import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.Server;
import org.ballerinalang.test.context.ServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;

import java.io.File;
import java.util.List;

/**
 * TestNg listener to start and stop a server before all test classes are executed for integration test.
 * This class should be registered in testng.xml under listener section.
 */
public class TestExecutionListener implements IExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(TestExecutionListener.class);

    private static Server newServer;

    /**
     * This method will execute before all the test classes are executed and this will start a server
     * with sample service files deployed.
     *
     */
    @Override
    public void onExecutionStart() {
        //path of the zip file distribution
        String serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        newServer = new ServerInstance(serverZipPath) {
            //config the service files need to be deployed
            @Override
            protected void configServer() {
                //path of the sample bal file directory
                String serviceSampleDir = this.getServerHome() + File.separator + Constant.SERVICE_SAMPLE_DIR;
                //list of sample bal files to be deploy
                String[] serviceFilesArr = listSamples(serviceSampleDir);
                setArguments(serviceFilesArr);
            }
        };
        try {
            newServer.start();
        } catch (Exception e) {
            log.error("Server failed to start. " + e.getMessage(), e);
            throw new RuntimeException("Server failed to start. " + e.getMessage(), e);
        }
    }

    /**
     * This method will execute after all the test classes are executed and this will stop the server
     * started by start method.
     *
     */
    @Override
    public void onExecutionFinish() {
        if (newServer != null && newServer.isRunning()) {
            try {
                newServer.stop();
            } catch (Exception e) {
                log.error("Server failed to stop. " + e.getMessage(), e);
                throw new RuntimeException("Server failed to stop. " + e.getMessage(), e);
            }
        }
    }

    /**
     * To het the server instance started by listener.
     * @return up and running server instance.
     */
    public static Server getServerInstance() {
        if (newServer == null || !newServer.isRunning()) {
            throw new RuntimeException("Server in not started Properly");
        }
        return newServer;
    }

    /**
     * List the file in a given directory.
     *
     * @param path of the directory
     * @param list   collection of files found
     * @return String arrays of file absolute paths
     */
    private static String[] listFiles(String path, List<String> list) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        if (list.size() > 100) {
            //returning the search when it comes to 100 files
            log.warn("Sample file deployment restricted to 100 files");
            return list.toArray(new String[]{});
        }
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    log.info("Searching service ballerina files in " + file.getPath());
                    listFiles(file.getAbsolutePath(), list);
                } else {
                    if (file.getPath().endsWith(Constant.SERVICE_FILE_EXTENSION)) {
                        log.info("Adding file " + file.getPath());
                        list.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return list.toArray(new String[]{});
    }

    /**
     * List given samples ballerina services.
     *
     * @param sampleDir sample directory
     * @return String arrays of file absolute paths
     */
    private static String[] listSamples(String sampleDir) {
        String[] sampleFiles = {
                sampleDir + File.separator + "echoService" + File.separator + "echoService.bal",
                sampleDir + File.separator + "helloWorldService" + File.separator + "helloWorldService.bal",
                sampleDir + File.separator + "passthroughService" + File.separator + "passthroughService.bsz",
                sampleDir + File.separator + "restfulService" + File.separator + "ecommerceService.bsz",
                sampleDir + File.separator + "routingServices" + File.separator + "routingServices.bsz",
                sampleDir + File.separator + "serviceChaining" + File.separator + "ATMLocatorService.bsz"
        };
        return sampleFiles;
    }
}
