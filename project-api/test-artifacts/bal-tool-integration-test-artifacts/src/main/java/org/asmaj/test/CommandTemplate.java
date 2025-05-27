package org.asmaj.test;

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

import static org.asmaj.test.CommandTemplate.TOOL_NAME;

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
