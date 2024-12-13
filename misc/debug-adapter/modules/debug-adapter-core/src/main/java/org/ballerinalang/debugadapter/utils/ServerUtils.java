/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
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
package org.ballerinalang.debugadapter.utils;

import com.sun.jdi.ThreadReference;
import org.ballerinalang.debugadapter.ExecutionContext;
import org.ballerinalang.debugadapter.breakpoint.BalBreakpoint;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.services.GenericEndpoint;

import java.net.ServerSocket;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;

/**
 * Ballerina debug server related utility functions.
 *
 * @since 2201.11.0
 */
public class ServerUtils {

    private static final String FAST_RUN_NOTIFICATION_NAME = "startFastRun";

    /**
     * Checks whether the debug server should run in no-debug mode.
     *
     * @param context debug context
     * @return true if the debug mode is no-debug mode
     */
    public static boolean isNoDebugMode(ExecutionContext context) {
        try {
            ClientConfigHolder confHolder = context.getAdapter().getClientConfigHolder();
            return confHolder instanceof ClientLaunchConfigHolder launchConfHolder && launchConfHolder.isNoDebugMode();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the fast-run mode is enabled in the connected debug client.
     *
     * @param context debug context
     * @return true if the debug mode is fast-run mode
     */
    public static boolean isFastRunEnabled(ExecutionContext context) {
        try {
            Optional<ClientConfigHolder.ExtendedClientCapabilities> extendedCapabilities =
                    context.getAdapter().getClientConfigHolder().getExtendedCapabilities();
            return extendedCapabilities
                    .map(ClientConfigHolder.ExtendedClientCapabilities::supportsFastRun)
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates whether the given DAP thread reference represents a ballerina strand.
     *
     * @param threadReference DAP thread reference
     * @return true if the given DAP thread reference represents a ballerina strand
     */
    public static boolean isBalStrand(ThreadReference threadReference) {
        // Todo - Refactor to use thread proxy implementation
        try {
            return isBalStackFrame(threadReference.frames().getFirst());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates whether the given DAP stack frame represents a ballerina call stack frame.
     *
     * @param frame DAP stack frame
     * @return true if the given DAP stack frame represents a ballerina call stack frame
     */
    public static boolean isBalStackFrame(com.sun.jdi.StackFrame frame) {
        // Todo - Refactor to use stack frame proxy implementation
        try {
            return frame.location().sourceName().endsWith(BAL_FILE_EXT);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates a given ballerina stack frame for its source information.
     *
     * @param stackFrame ballerina stack frame
     * @return true if it's a valid ballerina frame
     */
    public static boolean isValidFrame(StackFrame stackFrame) {
        return stackFrame != null && stackFrame.getSource() != null && stackFrame.getLine() > 0;
    }

    /**
     * Converts a given DAP source breakpoint instance to a ballerina breakpoint instance.
     *
     * @param context  debug context
     * @param sourceBp source breakpoint
     * @param source   source
     * @return ballerina breakpoint
     */
    public static BalBreakpoint toBalBreakpoint(ExecutionContext context, SourceBreakpoint sourceBp, Source source) {
        BalBreakpoint breakpoint = new BalBreakpoint(source, sourceBp.getLine());
        breakpoint.setCondition(sourceBp.getCondition());
        breakpoint.setLogMessage(sourceBp.getLogMessage());
        // If the debug client doesn't support breakpoint verification, mark the breakpoint as verified by default.
        if (supportsBreakpointVerification(context)) {
            breakpoint.setSupportsVerification(true);
        }

        return breakpoint;
    }

    /**
     * Checks whether the connected debug client supports breakpoint verification.
     *
     * @param context debug context
     * @return true if the connected debug client supports breakpoint verification
     */
    public static boolean supportsBreakpointVerification(ExecutionContext context) {
        ClientConfigHolder configHolder = context.getAdapter().getClientConfigHolder();

        return Objects.nonNull(configHolder) && configHolder.getExtendedCapabilities()
                .map(ClientConfigHolder.ExtendedClientCapabilities::supportsBreakpointVerification)
                .orElse(false);
    }

    /**
     * Sends a custom notification to the debug client to trigger a fast-run.
     *
     * @param context debug context
     * @param port    port number
     */
    public static void sendFastRunNotification(ExecutionContext context, int port) {
        Endpoint endPoint = new GenericEndpoint(context.getClient());
        ClientConfigHolder configs = context.getAdapter().getClientConfigHolder();
        Map<String, String> envVarMap = ((ClientLaunchConfigHolder) configs).getEnv().orElse(Map.of());
        String[] programArgs = ((ClientLaunchConfigHolder) configs).getProgramArgs().toArray(new String[0]);

        FastRunArgs args = new FastRunArgs(port, envVarMap, programArgs);
        endPoint.notify(FAST_RUN_NOTIFICATION_NAME, args);
    }

    /**
     * Finds an available port.
     *
     * @return available port number.
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (Exception e) {
            throw new IllegalStateException("Could not find a free TCP/IP port to start debugging", e);
        }
    }

    /**
     * Represents the arguments of the 'startFastRun' notification.
     *
     * @param debugPort   debug port number
     * @param env         environment variables
     * @param programArgs program arguments
     */
    public record FastRunArgs(int debugPort, Map<String, String> env, String[] programArgs) {

    }
}
