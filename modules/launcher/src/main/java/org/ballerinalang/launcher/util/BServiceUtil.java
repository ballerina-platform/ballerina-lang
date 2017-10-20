/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.launcher.util;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;

/**
 * {@code BServiceRunHelper} is responsible for initializing an environment for a particular ballerina file.
 */
public class BServiceUtil {

    public static CompileResult setupProgramFile(Object obj, String sourcePath, String pkgPath) {
        // Initialize server connectors before starting the test cases
        ServerConnectorRegistry.getInstance().initServerConnectors();

        try {
            CompileResult compileResult;
            if (pkgPath == null) {
                compileResult = BCompileUtil.compile(sourcePath);
            } else {
                compileResult = BCompileUtil.compile(obj, sourcePath, pkgPath);
            }
            ProgramFile programFile = compileResult.getProgFile();
            BLangProgramRunner.runService(programFile);
            return compileResult;
        } catch (Exception e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
    }

    public static CompileResult setupProgramFile(Object obj, String sourcePath) {
        return setupProgramFile(obj, sourcePath, null);
    }

    public static void cleanup(CompileResult compileResult) {
        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getEntryPackage();
        for (ServiceInfo serviceInfo : packageInfo.getServiceInfoEntries()) {
            ServerConnectorRegistry.getInstance().unRegisterService(serviceInfo);
        }
    }

}
