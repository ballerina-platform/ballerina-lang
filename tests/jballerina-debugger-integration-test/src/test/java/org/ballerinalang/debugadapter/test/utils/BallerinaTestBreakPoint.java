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
package org.ballerinalang.debugadapter.test.utils;

import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;

import java.io.File;

/**
 * Ballerina breakpoint representation used for integration test scenarios.
 */
public class BallerinaTestBreakPoint {
    String filePath;
    long line;

    public BallerinaTestBreakPoint(String filePath, long line) {
        this.filePath = filePath;
        this.line = line;
    }

    public Source getSource() {
        Source source = new Source();
        String fileSeparatorRegEx = File.separatorChar == '\\' ? "\\\\" : File.separator;
        String[] paths = filePath.split(fileSeparatorRegEx);
        source.setPath(filePath);
        source.setName(paths[paths.length - 1]);
        return source;
    }

    public SourceBreakpoint getDAPBreakPoint() {
        SourceBreakpoint breakpoint = new SourceBreakpoint();
        breakpoint.setLine(line);
        breakpoint.setColumn(0L);
        return breakpoint;
    }
}