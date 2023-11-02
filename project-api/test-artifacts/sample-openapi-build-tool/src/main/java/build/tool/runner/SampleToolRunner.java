package build.tool.runner;

import build.tool.runner.diagnostics.ToolDiagnostic;
import io.ballerina.cli.tool.BuildToolRunner;
import io.ballerina.projects.ToolContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SampleToolRunner implements BuildToolRunner {
    List<Diagnostic> diagnostics = new ArrayList<>();

    @Override
    public void executeTool(ToolContext toolContext) {
        Path absFilePath = toolContext.packageInstance().project().sourceRoot().resolve(toolContext.filePath());
        System.out.println(absFilePath.toFile().exists());
        if (!absFilePath.toFile().exists()) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo("001", "The provided filePath does not exist", DiagnosticSeverity.ERROR);
            diagnostics.add(new ToolDiagnostic(diagnosticInfo, diagnosticInfo.messageFormat()));
        }
        System.out.println("Running sample build tool: " + toolContext.toolType());
        System.out.println("Cache created at: " + toolContext.cachePath());
    }

    @Override
    public String getToolName() {
        return "openapi";
    }

    @Override
    public List<Diagnostic> diagnostics() {
        return this.diagnostics;
    }
}
