/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Debug process related low-level task executor through JDI.
 */
public class DebugExecutionManager {

    private String host;
    private Integer port;
    private VirtualMachine attachedVm;
    private final JBallerinaDebugServer server;

    public static final String LOCAL_HOST = "localhost";
    private static final String SOCKET_CONNECTOR_NAME = "com.sun.jdi.SocketAttach";
    private static final String CONNECTOR_ARGS_HOST = "hostname";
    private static final String CONNECTOR_ARGS_PORT = "port";
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugExecutionManager.class);

    DebugExecutionManager(JBallerinaDebugServer server) {
        this.server = server;
    }

    public boolean isActive() {
        return attachedVm != null;
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    /**
     * Attaches to an existing JVM using an SocketAttachingConnector and returns the attached VM instance.
     */
    public VirtualMachine attach(String hostName, int port) throws IOException, IllegalConnectorArgumentsException {
        AttachingConnector socketAttachingConnector = Bootstrap.virtualMachineManager().attachingConnectors().stream()
                .filter(ac -> ac.name().equals(SOCKET_CONNECTOR_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to locate SocketAttachingConnector"));

        Map<String, Connector.Argument> connectorArgs = socketAttachingConnector.defaultArguments();
        if (!hostName.isEmpty()) {
            connectorArgs.get(CONNECTOR_ARGS_HOST).setValue(hostName);
        }
        connectorArgs.get(CONNECTOR_ARGS_PORT).setValue(String.valueOf(port));
        LOGGER.info(String.format("Debugger is attaching to: %s:%d", hostName, port));

        attachedVm = socketAttachingConnector.attach(connectorArgs);
        this.host = !hostName.isEmpty() ? hostName : LOCAL_HOST;
        this.port = port;

        // Todo - enable for launch-mode after implementing debug server client logger
        if (server.getClientConfigHolder().getKind() == ClientConfigHolder.ClientConfigKind.ATTACH_CONFIG) {
            server.getOutputLogger().sendDebugServerOutput((String.format("Connected to the target VM, address: " +
                    "'%s:%s'", host, port)));
        }
        return attachedVm;
    }
}
