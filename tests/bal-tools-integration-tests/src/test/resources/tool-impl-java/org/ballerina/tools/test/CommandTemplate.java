/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.tools.test;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolConfig;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import picocli.CommandLine;

import static org.ballerina.tools.test.CommandTemplate.TOOL_NAME;

@CommandLine.Command(name = TOOL_NAME, description = "Test command")
@ToolConfig(name = TOOL_NAME)
public class CommandTemplate implements BLauncherCmd, CodeGeneratorTool {
    static final String TOOL_NAME = "<TOOL_NAME>";

    @Override
    public void execute() {
        System.out.println(getVersion());
    }

    @Override
    public void execute(ToolContext toolContext) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                "TEST001", getVersion(), DiagnosticSeverity.INFO);
        toolContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
    }

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {

    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {

    }

    @Override
    public void setParentCmdParser(CommandLine commandLine) {

    }

    private String getVersion() {
        return TOOL_NAME + " <VERSION>";
    }

    private static class NullLocation implements Location {

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from(TOOL_NAME, from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
