/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.telemetry;

import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.command.executors.ReportFeatureUsageExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;

import java.util.List;
import java.util.Optional;

/**
 * A utility for telemetry related tasks.
 *
 * @since 2.0.0
 */
public final class TelemetryUtil {

    private static final String ARG_FEATURE_NAME = "feature.Name";
    private static final String ARG_FEATURE_CLASS = "feature.Class";
    private static final String ARG_FEATURE_TITLE = "feature.Title";

    private TelemetryUtil() {
    }

    /**
     * Sends the provided telemetry event to LS client.
     *
     * @param serverContext LS context
     * @param event         Telemetry event
     */
    public static void sendTelemetryEvent(LanguageServerContext serverContext, LSTelemetryEvent event) {
        LSClientLogger clientLogger = LSClientLogger.getInstance(serverContext);
        clientLogger.telemetryEvent(event);
    }

    /**
     * Get a {@link LSFeatureUsageTelemetryEvent} from command arguments. Intended to be used with the command which
     * is executed when a quick fix code action is executed.
     *
     * @param arguments Command arguments
     * @return Optional telemetry event
     */
    public static Optional<LSFeatureUsageTelemetryEvent> featureUsageEventFromCommandArgs(
            List<CommandArgument> arguments) {
        String featureName = null;
        String featureClass = null;
        String featureTitle = null;
        for (CommandArgument argument : arguments) {
            switch (argument.key()) {
                case ARG_FEATURE_NAME:
                    featureName = argument.valueAs(String.class);
                    break;
                case ARG_FEATURE_CLASS:
                    featureClass = argument.valueAs(String.class);
                    break;
                case ARG_FEATURE_TITLE:
                    featureTitle = argument.valueAs(String.class);
                    break;
                default:
                    break;
            }
        }

        LSFeatureUsageTelemetryEvent telemetryEvent = null;
        if (featureName != null) {
            telemetryEvent = LSFeatureUsageTelemetryEvent.from(featureName, featureClass, featureTitle);
        }

        return Optional.ofNullable(telemetryEvent);
    }

    /**
     * Get a {@link LSFeatureUsageTelemetryEvent} for a command executor. This will filter the executions of
     * {@link ReportFeatureUsageExecutor} which is used by quick fix code actions to send usage stats.
     *
     * @param commandExecutor Command executor
     * @return Optional telemetry event
     */
    public static Optional<LSFeatureUsageTelemetryEvent> featureUsageEventFromCommandExecutor(
            LSCommandExecutor commandExecutor) {
        LSFeatureUsageTelemetryEvent telemetryEvent = null;
        if (!ReportFeatureUsageExecutor.COMMAND.equals(commandExecutor.getCommand())) {
            telemetryEvent = LSFeatureUsageTelemetryEvent.from(commandExecutor.getCommand(),
                    commandExecutor.getClass().getName(), null);
        }

        return Optional.ofNullable(telemetryEvent);
    }

    /**
     * Adds the command to report usage statistics into the provided code action.
     *
     * @param codeAction Code action
     */
    public static void addReportFeatureUsageCommandToCodeAction(CodeAction codeAction,
                                                                LSCodeActionProvider codeActionProvider) {
        if (codeAction.getCommand() == null) {
            CommandArgument nameArg = CommandArgument.from(ARG_FEATURE_NAME, codeActionProvider.getName());
            CommandArgument classArg = CommandArgument.from(ARG_FEATURE_CLASS, codeActionProvider.getClass().getName());
            CommandArgument titleArg = CommandArgument.from(ARG_FEATURE_TITLE, codeAction.getTitle());
            List<Object> args = List.of(nameArg, classArg, titleArg);
            codeAction.setCommand(new Command(CommandConstants.REPORT_USAGE_STATISTICS_COMMAND_TITLE,
                    ReportFeatureUsageExecutor.COMMAND, args));
        }
    }
}
