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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVA agent which will be attached to Ballerina run time and trigger the shutdown sequence.
 *
 * @since 0.982.0
 */
public class BallerinaExitAgent {
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

    private BallerinaExitAgent() {
    }

    /**
     * Method which will be called when agent installs.
     *
     * @param agentArgs arguments for the agent.
     */
    public static void agentmain(final String agentArgs) {
        Map<String, String> args = decodeAgentArgs(agentArgs);
        final long timeout = getValue(EXIT_TIMEOUT, DEFAULT_EXIT_TIMEOUT, args);
        final int exitStatus = (int) getValue(EXIT_STATUS, DEFAULT_EXIT_STATUS, args);
        final int killStatus = (int) getValue(KILL_STATUS, DEFAULT_KILL_STATUS, args);

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
}
