/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.debugger.test.utils;

import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Ballerina debug point (breakpoint/debug hit point) representation used for integration test scenarios.
 */
public class BallerinaTestDebugPoint {

    private final URI filePathUri;
    private final int line;
    private final String condition;
    private final String logMessage;

    public BallerinaTestDebugPoint(Path filePath, int line) {
        this(filePath, line, null, null);
    }

    public BallerinaTestDebugPoint(URI filePathUri, int line) {
        this(filePathUri, line, null, null);
    }

    public BallerinaTestDebugPoint(Path filePath, int line, String condition, String logMessage) {
        this(filePath.toAbsolutePath().toUri(), line, condition, logMessage);
    }

    public BallerinaTestDebugPoint(URI filePathUri, int line, String condition, String logMessage) {
        this.filePathUri = filePathUri;
        this.line = line;
        this.condition = condition;
        this.logMessage = logMessage;
    }

    public URI getSourceURI() {
        return filePathUri;
    }

    public Source getSource() {
        Source source = new Source();
        String fileSeparatorRegEx = File.separatorChar == '\\' ? "\\\\" : File.separator;
        String[] paths = Paths.get(filePathUri).toAbsolutePath().toString().split(fileSeparatorRegEx);
        source.setPath(Paths.get(filePathUri).toAbsolutePath().toString());
        source.setName(paths[paths.length - 1]);
        return source;
    }

    public SourceBreakpoint getDAPBreakPoint() {
        SourceBreakpoint breakpoint = new SourceBreakpoint();
        breakpoint.setLine(line);
        breakpoint.setColumn(0);
        breakpoint.setCondition(condition);
        breakpoint.setLogMessage(logMessage);
        return breakpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BallerinaTestDebugPoint other = (BallerinaTestDebugPoint) o;
        return filePathUri.equals(other.filePathUri) && line == other.line;
    }

    @Override
    public int hashCode() {
        return 7 * line + filePathUri.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Ballerina test breakpoint at line: %d, in '%s'", line, filePathUri);
    }
}
