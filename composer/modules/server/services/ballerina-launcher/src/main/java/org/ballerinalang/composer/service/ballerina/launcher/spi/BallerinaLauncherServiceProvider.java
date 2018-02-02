/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.composer.service.ballerina.launcher.spi;

import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ComposerServiceProvider;
import org.ballerinalang.composer.server.spi.annotation.ComposerSPIServiceProvider;
import org.ballerinalang.composer.service.ballerina.launcher.service.BallerinaLauncherService;
import org.ballerinalang.composer.service.ballerina.launcher.service.LauncherConstants;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Map;

/**
 * Service Provider for Micro service for ballerina launcher.
 */
@ComposerSPIServiceProvider
public class BallerinaLauncherServiceProvider implements ComposerServiceProvider {
    @Override
    public ComposerService createService(ServerConfig config) {
        // TODO Move Logic to a config provider
        // populate debugger launch path
        String debuggerPath = "ws://" + config.getHost() + ":" + getFreePort() + "/debug";
        if (config.getCustomConfigs() != null &&
                config.getCustomConfigs().containsKey("debugger")) {
            Map<String, String> debuggerConfig = config.getCustomConfigs().get("debugger");
            if (debuggerConfig.containsKey("path")) {
                debuggerPath = debuggerConfig.get("path");
            }
        }
        config.setDebuggerPath(debuggerPath);
        return new BallerinaLauncherService(config);
    }

    public static int getFreePort() {
        for (int i = LauncherConstants.MIN_PORT_NUMBER; i < LauncherConstants.MAX_PORT_NUMBER; i++) {
            if (isPortAvailable(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks to see if a specific port is available.
     * TODO Move free port finding logic to a single util class
     * @param port the port number to check for availability
     * @return <tt>true</tt> if the port is available, or <tt>false</tt> if not
     * @throws IllegalArgumentException is thrown if the port number is out of range
     */
    private static boolean isPortAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
}
