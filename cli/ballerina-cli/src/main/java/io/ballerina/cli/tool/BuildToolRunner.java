package io.ballerina.cli.tool;

import io.ballerina.projects.ToolContext;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;

public interface BuildToolRunner {
    void executeTool(ToolContext toolContext);

    String getToolName();

    List<Diagnostic> diagnostics();
}
