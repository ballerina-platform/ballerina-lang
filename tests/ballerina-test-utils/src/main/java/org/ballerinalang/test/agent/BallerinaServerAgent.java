/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import org.ballerinalang.test.agent.server.WebServer;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class hold the server information and manage the server instance.
 *
 * @since 0.982.0
 */
public final class BallerinaServerAgent {

    private static final PrintStream OUT_STREAM = System.err;

    /**
     * Argument name for exist status.
     */
    private static final String EXIT_STATUS = "exitStatus";

    private static final int DEFAULT_EXIT_STATUS = 0;

    /**
     * Argument name for waiting period if graceful shutdown fails.
     */
    private static final String EXIT_TIMEOUT = "timeout";

    /**
     * Default exit timeout in milliseconds, -1 or 0 means wait forever.
     */
    private static final long DEFAULT_EXIT_TIMEOUT = -1;

    /**
     * Argument name for kill status.
     */
    private static final String KILL_STATUS = "killStatus";

    private static final int DEFAULT_KILL_STATUS = 1;

    private static final String AGENT_HOST = "host";

    private static final String DEFAULT_AGENT_HOST = "localhost";

    private static final String AGENT_PORT = "port";

    private static final int DEFAULT_AGENT_PORT = -1;

    private static long timeout = DEFAULT_EXIT_TIMEOUT;
    private static int exitStatus = DEFAULT_EXIT_STATUS;
    private static int killStatus = DEFAULT_KILL_STATUS;
    private static String agentHost = DEFAULT_AGENT_HOST;
    private static int agentPort = DEFAULT_AGENT_PORT;

    private BallerinaServerAgent() {
    }

    /**
     * This method will be called before invoking ballerina Main method.
     *
     * @param args              to be passed with required details
     * @param instrumentation   instance for any instrumentation purposes
     */
    public static void premain(String args, Instrumentation instrumentation) {
        OUT_STREAM.println("*******************************************************");
        OUT_STREAM.println("Initializing Ballerina server agent with arguments - " + args);
        OUT_STREAM.println("*******************************************************");

        Map<String, String> inputArgs = decodeAgentArgs(args);

        timeout = getValue(EXIT_TIMEOUT, DEFAULT_EXIT_TIMEOUT, inputArgs);
        exitStatus = (int) getValue(EXIT_STATUS, DEFAULT_EXIT_STATUS, inputArgs);
        killStatus = (int) getValue(KILL_STATUS, DEFAULT_KILL_STATUS, inputArgs);
        agentHost = getValue(AGENT_HOST, DEFAULT_AGENT_HOST, inputArgs);
        agentPort = (int) getValue(AGENT_PORT, DEFAULT_AGENT_PORT, inputArgs);

        OUT_STREAM.println("timeout - " + timeout + " exitStatus - " + exitStatus
                + " killStatus - " + killStatus + " host - " + agentHost + " port - " + agentPort);

        if (agentPort == -1) {
            throw new RuntimeException("Invalid agent port - " + agentPort);
        }
        ClassFileTransformer transformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (JvmConstants.BAL_RUNTIME.equals(className)) {
                    try {
                        ClassPool cp = ClassPool.getDefault();
                        CtClass cc = cp.get("io.ballerina.runtime.internal.BalRuntime");
                        cc.addField(CtField.make("boolean agentStarted;", cc));
                        CtConstructor constructor = cc.getConstructor(JvmSignatures.INIT_RUNTIME);
                        constructor.insertBeforeBody("if (!agentStarted) {" +
                                "org.ballerinalang.test.agent.BallerinaServerAgent.startAgentServer();" +
                                "agentStarted = true;" +
                                " }");
                        byte[] byteCode = cc.toBytecode();
                        cc.detach();
                        return byteCode;
                    } catch (Throwable ex) {
                        OUT_STREAM.println("Error injecting the start agent code to the server, error - "
                                + ex.getMessage());
                    }
                }
                return new byte[]{};
            }
        };

        //TODO find a way to fail the process if this instrumentation is not successful
        instrumentation.addTransformer(transformer);
    }

    /**
     * Start the agent server for managing the server.
     */
    public static void startAgentServer() {
        OUT_STREAM.println("Starting Ballerina agent on host - " + agentHost + ", port - " + agentPort);
        Thread.startVirtualThread(() -> {
            try {
                WebServer ws = new WebServer(agentHost, agentPort);

                // Post endpoint to check the status TODO we may be able to remove this
                ws.post("/status", () -> OUT_STREAM.println("status check"));

                // Post endpoint to shut down the server
                ws.post("/shutdown", BallerinaServerAgent::shutdownServer);

                // Post endpoint to kill the server
                ws.post("/kill", BallerinaServerAgent::killServer);

                // Start the server
                ws.start();
            } catch (Throwable e) {
                OUT_STREAM.println("Error initializing agent server, error - " + e.getMessage());
            }
        });
        OUT_STREAM.println("Ballerina agent started on host - " + agentHost + ", port - " + agentPort);
    }

    private static void shutdownServer() {
        OUT_STREAM.println("Shutting down Ballerina server with agent port - " + agentPort);
        Thread.startVirtualThread(() -> Runtime.getRuntime().exit(exitStatus));

        if (timeout <= 0) {
            return;
        }
        Thread.startVirtualThread(() -> {
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            while (endTime - startTime < timeout) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    //ignore
                }
                endTime = System.currentTimeMillis();
                if (endTime - startTime >= timeout) {
                    Runtime.getRuntime().halt(killStatus);
                }
            }
        });
    }

    private static void killServer() {
        OUT_STREAM.println("Killing Ballerina server with agent port - " + agentPort);
        Thread.startVirtualThread(() -> Runtime.getRuntime().halt(killStatus));
    }

    private static Map<String, String> decodeAgentArgs(final String agentArgs) {
        if (agentArgs == null) {
            return Collections.emptyMap();
        }

        String[] splitted = agentArgs.split(",");
        Map<String, String> result = new HashMap<>();

        for (String argString : splitted) {
            String[] argParts = argString.split("=");
            String key = argParts[0];
            if (argParts.length == 1) {
                result.put(key, null);
                continue;
            }
            result.put(key, argParts[1]);
        }

        return result;
    }

    private static long getValue(String argName, long defaultValue, Map<String, String> args) {
        String value = args.get(argName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    private static String getValue(String argName, String defaultValue, Map<String, String> args) {
        String value = args.get(argName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

}
