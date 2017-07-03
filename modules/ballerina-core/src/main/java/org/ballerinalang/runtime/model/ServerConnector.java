/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.runtime.model;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.runtime.config.InterceptorConfig;
import org.ballerinalang.runtime.config.ServerConnectorConfig;
import org.ballerinalang.runtime.config.ServiceInterceptorConfig;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ServerConnector} represents runtime information about a server connector.
 *
 * @since 0.89
 */
public class ServerConnector {

    private String name;
    private String protocol;

    boolean enableInterceptors;
    Path interceptorDeploymentPath;
    List<ServiceInterceptor> serviceInterceptorList = new ArrayList<>();

    // Cache values.
    Map<String, ProgramFile> interceptorProgramFileMap = new HashMap<>();

    protected ServerConnector(String name, String protocol) {
        this.name = name;
        this.protocol = protocol;
    }

    /**
     * Initialize all the
     */
    public void init() {
        // TODO : Add actual server connector initialization here.
        // Init Interceptors.
        for (ProgramFile programFile : interceptorProgramFileMap.values()) {
            Context bContext = new Context(programFile);
            bContext.disableNonBlocking = true;
            // TODO : Fix main init, when Packerina comes. For now let's init main package.
            PackageInfo mainPkgInfo = programFile.getPackageInfo(programFile.getMainPackageName());
            BLangFunctions.invokeFunction(programFile, mainPkgInfo, mainPkgInfo.getInitFunctionInfo(), bContext);
            if (bContext.getError() != null) {
                String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
                throw new BLangRuntimeException("error: " + stackTraceStr);
            }
        }
    }

    /* Getters. */

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isEnableInterceptors() {
        return enableInterceptors;
    }

    public List<ServiceInterceptor> getServiceInterceptorList() {
        return serviceInterceptorList;
    }

    /**
     * Builds a {@link ServerConnector}
     *
     * @since 0.89
     */
    public static class ServerConnectorBuilder {

        private ServerConnectorConfig serverConnectorConfig;

        public ServerConnectorBuilder(ServerConnectorConfig serverConnectorConfig) {
            this.serverConnectorConfig = serverConnectorConfig;
        }

        public ServerConnector build() {
            final String serverConnectorName = serverConnectorConfig.getName();
            final String protocolName = serverConnectorConfig.getProtocol();
            InterceptorConfig interceptorConfig = serverConnectorConfig.getInterceptors();

            ServerConnector serverConnector = new ServerConnector(serverConnectorName, protocolName);
            // Interceptor related configuration.
            serverConnector.enableInterceptors = interceptorConfig.isEnable();
            if (serverConnector.enableInterceptors) {
                final String deploymentDirectory = interceptorConfig.getDeploymentDirectory();
                if (deploymentDirectory == null) {
                    throw new BLangRuntimeException("invalid interceptor deployment directory for server connector" +
                            serverConnectorName);
                }
                File interceptorDeploymentDirectory = new File(deploymentDirectory);
                if (!interceptorDeploymentDirectory.exists()) {
                    throw new BLangRuntimeException("interceptor deployment directory " +
                            deploymentDirectory + " doesn't exist for server connector " +
                            serverConnectorName);
                }
                serverConnector.interceptorDeploymentPath = interceptorDeploymentDirectory.toPath();

                // Building Request interceptorConfig
                for (ServiceInterceptorConfig config : interceptorConfig.getServiceInterceptors()) {
                    ProgramFile programFile;
                    if (serverConnector.interceptorProgramFileMap.containsKey(config.getArchiveName())) {
                        programFile = serverConnector.interceptorProgramFileMap.get(config.getArchiveName());
                    } else {
                        BLangProgramLoader bLangProgramLoader = new BLangProgramLoader();
                        // Assumed all archives are semantically valid, before archived.
                        bLangProgramLoader.disableSemanticAnalyzer();
                        Path ballerinaArchive = Paths.get(serverConnector.interceptorDeploymentPath.toString(),
                                config.getArchiveName());
                        programFile = bLangProgramLoader.loadMainProgramFile(serverConnector
                                .interceptorDeploymentPath, ballerinaArchive);
                        serverConnector.interceptorProgramFileMap.put(config.getArchiveName(), programFile);
                    }
                    ServiceInterceptor serviceInterceptor = new ServiceInterceptor.ServiceInterceptorBuilder
                            (programFile, config.getPackageName()).build();
                    serverConnector.serviceInterceptorList.add(serviceInterceptor);
                }
            }

            return serverConnector;
        }
    }
}
