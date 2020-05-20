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
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.tools.example.debug.expr.ExpressionParser;
import com.sun.tools.example.debug.expr.ParseException;
import com.sun.tools.jdi.SocketAttachingConnector;
import org.ballerinalang.debugadapter.exeption.JBalDebugEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Debug process related low-level task executor through JDI.
 */
public class DebugExecutionManager {

    private VirtualMachine attachedVm;
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
        AttachingConnector ac = Bootstrap.virtualMachineManager().attachingConnectors().stream()
                .filter(c -> c instanceof SocketAttachingConnector).findFirst().orElseThrow(() ->
                        new RuntimeException("Unable to locate SocketAttachingConnector"));
        Map<String, Connector.Argument> connectorArgs = ac.defaultArguments();
        if (!hostName.isEmpty()) {
            connectorArgs.get("hostname").setValue(hostName);
        }
        connectorArgs.get("port").setValue(port);
        LOGGER.info(String.format("Debugger is attaching to: %s:%s", hostName, port));
        attachedVm = ac.attach(connectorArgs);
        return attachedVm;
    }

    /**
     * Evaluates a given expression w.r.t. the provided debug state(stack frame).
     */
    public Optional<Value> evaluate(final StackFrame f, String expression) throws JBalDebugEvaluationException {
        try {
            ExpressionParser.GetFrame frameGetter = () -> f;
            return Optional.ofNullable(ExpressionParser.evaluate(expression, attachedVm, frameGetter));
        } catch (ParseException | InvocationException | InvalidTypeException | ClassNotLoadedException |
                IncompatibleThreadStateException e) {
            // Todo - Handling errors more specifically
            String message = String.format("Failed to execute the expression: \"%s\", due to:c%s", expression,
                    e.getMessage());
            LOGGER.error(message, e);
            throw new JBalDebugEvaluationException(message, e);
        }
    }
}
