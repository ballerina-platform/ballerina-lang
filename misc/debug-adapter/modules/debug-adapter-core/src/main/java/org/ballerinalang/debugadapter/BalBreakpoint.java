/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugadapter;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Information about ballerina breakpoint (Properties).
 *
 * @since 2.0.0
 */
public class BalBreakpoint {

    private final Source source;
    private final int line;
    private String condition;
    private LogMessage logMessage;

    private static final String INTERPOLATION_REGEX = "\\$\\{[^}]*}";

    public BalBreakpoint(Source source, int line) {
        this.source = source;
        this.line = line;
    }

    public Integer getLine() {
        return line;
    }

    public Source getSource() {
        return source;
    }

    public Optional<String> getCondition() {
        return Optional.ofNullable(condition);
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Optional<LogMessage> getLogMessage() {
        return Optional.ofNullable(logMessage);
    }

    public void setLogMessage(String logMessage) {
        if (isTemplate(logMessage)) {
            this.logMessage = new TemplateLogMessage(logMessage);
        } else {
            this.logMessage = new PlainTextLogMessage(logMessage);
        }
    }

    public Breakpoint getAsDAPBreakpoint() {
        Breakpoint breakpoint = new Breakpoint();
        breakpoint.setLine(line);
        breakpoint.setSource(source);
        breakpoint.setVerified(true);
        return breakpoint;
    }

    private boolean isTemplate(String logMessage) {
        return logMessage != null && Pattern.compile(INTERPOLATION_REGEX).matcher(logMessage).find();
    }

    public static class LogMessage {

        protected String message;

        private LogMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public class PlainTextLogMessage extends LogMessage {

        private PlainTextLogMessage(String message) {
            super(message);
        }

    }

    public class TemplateLogMessage extends LogMessage {

        private final List<String> expressions = new ArrayList<>();

        private TemplateLogMessage(String message) {
            super(message);
            extractInterpolations();
        }

        private void extractInterpolations() {
            Pattern pattern = Pattern.compile(INTERPOLATION_REGEX);
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                String expression = matcher.group();
                // Removes '${' and '}' characters from the expression.
                expressions.add(expression.substring(2, expression.length() - 1));
            }
        }

        public List<String> getExpressions() {
            return expressions;
        }

        public String resolve(List<String> evaluationResults) {
            Pattern pattern = Pattern.compile(INTERPOLATION_REGEX);
            Matcher matcher = pattern.matcher(message);
            AtomicInteger index = new AtomicInteger();
            return matcher.replaceAll(matchResult -> evaluationResults.get(index.getAndIncrement()));
        }
    }
}
