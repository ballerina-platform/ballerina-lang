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

package org.ballerinalang.debugadapter.breakpoint;

import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Source;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.ballerinalang.debugadapter.breakpoint.LogMessage.INTERPOLATION_REGEX;

/**
 * Holds Ballerina breakpoint related information.
 *
 * @since 2.0.0
 */
public class BalBreakpoint {

    private final int id;
    private final Source source;
    private final int line;
    private String condition;
    private LogMessage logMessage;
    private boolean isVerified;

    private static final AtomicInteger nextID = new AtomicInteger(0);

    public BalBreakpoint(Source source, int line) {
        this.id = nextID.getAndIncrement();
        this.source = source;
        this.line = line;
        this.isVerified = false;
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
        } else if (logMessage != null && !logMessage.isBlank()) {
            this.logMessage = new PlainTextLogMessage(logMessage);
        }
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Breakpoint getAsDAPBreakpoint() {
        Breakpoint breakpoint = new Breakpoint();
        breakpoint.setId(id);
        breakpoint.setLine(line);
        breakpoint.setSource(source);
        breakpoint.setVerified(isVerified);
        return breakpoint;
    }

    private boolean isTemplate(String logMessage) {
        return logMessage != null && Pattern.compile(INTERPOLATION_REGEX).matcher(logMessage).find();
    }
}
