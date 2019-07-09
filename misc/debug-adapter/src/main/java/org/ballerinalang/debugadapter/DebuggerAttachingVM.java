/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

/**
 * Attach to a already running JVM.
 */
public class DebuggerAttachingVM {
    private int port;
    private VirtualMachine vm;
    private PrintStream out = System.out;

    public DebuggerAttachingVM(int port) {
        this.port = port;
    }
    public VirtualMachine initialize() throws IOException, IllegalConnectorArgumentsException {
        AttachingConnector ac = Bootstrap.virtualMachineManager().attachingConnectors()
                .stream()
                .filter(c -> c.name().equals("com.sun.jdi.SocketAttach"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to locate ProcessAttachingConnector"));

        Map<String, Connector.Argument> defaultArgs = ac.defaultArguments();
        Connector.IntegerArgument arg = (Connector.IntegerArgument) defaultArgs
                .get("port");

        arg.setValue(this.port);
        defaultArgs.put("port", arg);

        out.println("Debugger is attaching to: " + this.port);
        vm = ac.attach(defaultArgs);
        return vm;
    }
}
