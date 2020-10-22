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
import javassist.CtField;
import javassist.CtMethod;
import org.ballerinalang.test.agent.server.WebServer;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class hold the server information and manage the a server instance.
 *
 * @since 0.982.0
 */
public class BallerinaServerAgent {
    private static PrintStream outStream = System.err;

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

    public BallerinaServerAgent() {

    }

    /**
     * This method will be called before invoking ballerina Main method.
     *
     * @param args              to be passed with required details
     * @param instrumentation   instance for any instrumentation purposes
     */
    public static void premain(String args, Instrumentation instrumentation) {
        outStream.println("*******************************************************");
        outStream.println("Initializing Ballerina server agent with arguments - " + args);
        outStream.println("*******************************************************");

        Map<String, String> inputArgs = decodeAgentArgs(args);

        timeout = getValue(EXIT_TIMEOUT, DEFAULT_EXIT_TIMEOUT, inputArgs);
        exitStatus = (int) getValue(EXIT_STATUS, DEFAULT_EXIT_STATUS, inputArgs);
        killStatus = (int) getValue(KILL_STATUS, DEFAULT_KILL_STATUS, inputArgs);
        agentHost = getValue(AGENT_HOST, DEFAULT_AGENT_HOST, inputArgs);
        agentPort = (int) getValue(AGENT_PORT, DEFAULT_AGENT_PORT, inputArgs);

        outStream.println("timeout - " + timeout + " exitStatus - " + exitStatus
                + " killStatus - " + killStatus + " host - " + agentHost + " port - " + agentPort);

        if (agentPort == -1) {
            throw new RuntimeException("Invalid agent port - " + agentPort);
        }

        ClassFileTransformer transformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if ("io/ballerina/runtime/scheduling/Scheduler".equals(className)) {
                    try {
                        ClassPool cp = ClassPool.getDefault();
                        CtClass cc = cp.get("io.ballerina.runtime.scheduling.Scheduler");
                        cc.addField(CtField.make("boolean agentStarted;", cc));

                        CtMethod m = cc.getDeclaredMethod("start");
                        m.insertBefore("if (!agentStarted && immortal) {" +
                                "org.ballerinalang.test.agent.BallerinaServerAgent.startAgentServer();" +
                                "agentStarted = true;" +
                                " }");
                        byte[] byteCode = cc.toBytecode();
                        cc.detach();
                        return byteCode;
                    } catch (Throwable ex) {
                        outStream.println("Error injecting the start agent code to the server, error - "
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
        outStream.println("Starting Ballerina agent on host - " + agentHost + ", port - " + agentPort);
        new Thread(() -> {
            try {
                WebServer ws = new WebServer(agentHost, agentPort);

                // Post endpoint to check the status TODO we may be able to remove this
                ws.post("/status", () -> outStream.println("status check"));

                // Post endpoint to shutdown the server
                ws.post("/shutdown", BallerinaServerAgent::shutdownServer);

                // Post endpoint to kill the server
                ws.post("/kill", BallerinaServerAgent::killServer);

                // Start the server
                ws.start();
            } catch (Throwable e) {
                outStream.println("Error initializing agent server, error - " + e.getMessage());
            }
        }).start();
        outStream.println("Ballerina agent started on host - " + agentHost + ", port - " + agentPort);
    }

    private static void shutdownServer() {
        outStream.println("Shutting down Ballerina server with agent port - " + agentPort);
        new Thread(() -> Runtime.getRuntime().exit(exitStatus)).start();

        if (timeout <= 0) {
            return;
        }
        Thread killThread = new Thread(() -> {
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
        killThread.setDaemon(true);
        killThread.start();
    }

    private static void killServer() {
        outStream.println("Killing Ballerina server with agent port - " + agentPort);
        Thread killThread = new Thread(() -> Runtime.getRuntime().halt(killStatus));
        killThread.setDaemon(true);
        killThread.start();
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
