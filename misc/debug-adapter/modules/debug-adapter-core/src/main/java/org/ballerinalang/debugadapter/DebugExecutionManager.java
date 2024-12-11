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
import java.util.Objects;
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
    private static final String VALUE_UNKNOWN = "unknown";
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

    public String getRemoteVMAddress() {
        String host = getHost().orElse(VALUE_UNKNOWN);
        String port = getPort().map(String::valueOf).orElse(VALUE_UNKNOWN);
        return String.format("%s:%s", host, port);
    }

    /**
     * Attaches to an existing JVM using an SocketAttachingConnector and returns the attached VM instance.
     */
    public VirtualMachine attach(String hostName, int port) throws IOException, IllegalConnectorArgumentsException {
        // Default retry configuration
        return attach(hostName, port, 50, 100); // 100 attempts, 50ms between attempts
    }

    public VirtualMachine attach(String hostName, int port, long retryIntervalMs, int maxAttempts)
            throws IOException, IllegalConnectorArgumentsException {

        AttachingConnector attachingConnector = Bootstrap.virtualMachineManager().attachingConnectors().stream()
                .filter(ac -> ac.name().equals(SOCKET_CONNECTOR_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to locate SocketAttachingConnector"));

        hostName = Objects.isNull(hostName) || hostName.isBlank() ? LOCAL_HOST : hostName;
        Map<String, Connector.Argument> connectorArgs = attachingConnector.defaultArguments();
        connectorArgs.get(CONNECTOR_ARGS_HOST).setValue(hostName);
        connectorArgs.get(CONNECTOR_ARGS_PORT).setValue(String.valueOf(port));

        return attachWithRetries(attachingConnector, connectorArgs, retryIntervalMs, maxAttempts);
    }

    private VirtualMachine attachWithRetries(AttachingConnector connector, Map<String, Connector.Argument> args,
                                             long retryIntervalMs, int maxAttempts)
            throws IOException, IllegalConnectorArgumentsException {

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                attachedVm = connector.attach(args);
                this.host = args.get(CONNECTOR_ARGS_HOST).value();
                this.port = Integer.parseInt(args.get(CONNECTOR_ARGS_PORT).value());
                if (server.getClientConfigHolder().getKind() == ClientConfigHolder.ClientConfigKind.ATTACH_CONFIG) {
                    server.getOutputLogger().sendDebugServerOutput(
                            String.format("Connected to the target VM, address: '%s:%s'", host, port)
                    );
                }

                return attachedVm;
            } catch (IOException e) {
                LOGGER.debug(String.format("Attachment attempt %d/%d failed: %s", attempt, maxAttempts, e.getMessage()));
                try {
                    Thread.sleep(retryIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Attachment interrupted", e);
                }
            }
        }

        throw new IOException("Failed to attach to the target VM.");
    }

    public void reset() {
        attachedVm = null;
        host = null;
        port = null;
    }
}
