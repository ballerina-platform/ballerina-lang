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
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluatorBuilder;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Debug process related low-level task executor through JDI.
 */
public class DebugExecutionManager {

    private VirtualMachine attachedVm;
    private static final String SOCKET_CONNECTOR_NAME = "com.sun.jdi.SocketAttach";
    private static final String CONNECTOR_ARGS_HOST = "hostname";
    private static final String CONNECTOR_ARGS_PORT = "port";
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugExecutionManager.class);

    public DebugExecutionManager() {
        attachedVm = null;
    }

    public boolean isActive() {
        return attachedVm != null;
    }

    /**
     * Attaches to an existing JVM using an SocketAttachingConnector and returns the attached VM instance.
     */
    public VirtualMachine attach(String port) throws IOException, IllegalConnectorArgumentsException {
        return attach("", port);
    }

    /**
     * Attaches to an existing JVM using an SocketAttachingConnector and returns the attached VM instance.
     */
    public VirtualMachine attach(String hostName, String port) throws IOException, IllegalConnectorArgumentsException {
        if (port == null || port.isEmpty()) {
            throw new IllegalConnectorArgumentsException("Port is not defined.", "port");
        }

        AttachingConnector socketAttachingConnector = Bootstrap.virtualMachineManager().attachingConnectors().stream()
                .filter(ac -> ac.name().equals(SOCKET_CONNECTOR_NAME))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to locate SocketAttachingConnector"));

        Map<String, Connector.Argument> connectorArgs = socketAttachingConnector.defaultArguments();
        if (!hostName.isEmpty()) {
            connectorArgs.get(CONNECTOR_ARGS_HOST).setValue(hostName);
        }
        connectorArgs.get(CONNECTOR_ARGS_PORT).setValue(port);
        LOGGER.info(String.format("Debugger is attaching to: %s:%s", hostName, port));
        attachedVm = socketAttachingConnector.attach(connectorArgs);
        return attachedVm;
    }

    /**
     * Evaluates a given ballerina expression w.r.t. the provided debug state(stack frame).
     */
    public Value evaluate(SuspendedContext context, String expression) {
        try {
            EvaluatorBuilder evalBuilder = new EvaluatorBuilder(context);
            Evaluator evaluator = evalBuilder.build(expression);
            return evaluator.evaluate().getJdiValue();
        } catch (EvaluationException e) {
            return attachedVm.mirrorOf(e.getMessage());
        } catch (Exception e) {
            String message = EvaluationExceptionKind.PREFIX + "internal error";
            LOGGER.error(message, e);
            return attachedVm.mirrorOf(message);
        }
    }
}
