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
 * Debug process related low-level task executioner through JDI.
 */
public class DebugExecutionManager {

    private VirtualMachine attachedVm;
    private final String hostName;
    private final String portName;
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugExecutionManager.class);

    public DebugExecutionManager(String portName) {
        this("", portName);
    }

    public DebugExecutionManager(String hostName, String portName) {
        this.hostName = hostName;
        this.portName = portName;
    }

    /**
     * Attaches to an existing JVM using an SocketAttachingConnector and returns the attached VM instance.
     */
    public VirtualMachine attach() throws IOException, IllegalConnectorArgumentsException {

        AttachingConnector ac = Bootstrap.virtualMachineManager().attachingConnectors().stream()
                .filter(c -> c instanceof SocketAttachingConnector).findFirst().orElseThrow(() ->
                        new RuntimeException("Unable to locate SocketAttachingConnector"));

        Map<String, Connector.Argument> connectorArgs = ac.defaultArguments();
        if (!hostName.isEmpty()) {
            connectorArgs.get("hostname").setValue(this.hostName);
        }
        connectorArgs.get("port").setValue(portName);
        LOGGER.info("Debugger is attaching to: " + this.hostName + ":" + this.portName);
        attachedVm = ac.attach(connectorArgs);
        return attachedVm;
    }

    /**
     * Evaluates an expression for a given stack frame(state).
     */
    public Optional<Value> evaluate(final StackFrame f, String expr) throws JBalDebugEvaluationException {
        try {
            ExpressionParser.GetFrame frameGetter = () -> f;
            return Optional.ofNullable(ExpressionParser.evaluate(expr, attachedVm, frameGetter));
        } catch (ParseException | InvocationException | InvalidTypeException | ClassNotLoadedException |
                IncompatibleThreadStateException e) {
            // Todo - Handling errors more specifically
            String errorMsg = "Failed to execute the expression: \"" + expr + "\", due to: " + e.getMessage();
            LOGGER.error(errorMsg, e);
            throw new JBalDebugEvaluationException(errorMsg, e);
        }
    }
}
