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
package org.wso2.ballerina.test.listner;

import org.testng.IExecutionListener;
import org.wso2.ballerina.test.context.Constant;
import org.wso2.ballerina.test.context.Server;
import org.wso2.ballerina.test.context.ServerInstance;
import org.wso2.ballerina.test.context.Utils;

import java.io.File;

public class TestExecutionListener implements IExecutionListener {

    private static Server newServer;

    @Override
    public void onExecutionStart() {
        //path of the zip file distribution
        String serverPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
        //path of the sample bal file directory
        String serviceSampleDir = System.getProperty(Constant.SYSTEM_PROP_BASE_DIR) + File.separator
                                  + Constant.SERVICE_RESOURCE_DIR;
        //list of sample bal files to be deploy
        String[] serviceFiles = Utils.listFiles(serviceSampleDir);
        newServer = new ServerInstance(serverPath, serviceFiles);
        try {
            newServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Server failed to start. " + e.getMessage());
        }
    }

    @Override
    public void onExecutionFinish() {
        if (newServer != null && newServer.isRunning()) {
            try {
                newServer.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException("Server failed to stop. " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("Server failed to stop. " + e.getMessage());
            }
        }
    }

    public static Server getServerInstance() {
        if(newServer == null || !newServer.isRunning()) {
            throw new RuntimeException("Server in not started Properly");
        }
        return newServer;
    }
}
