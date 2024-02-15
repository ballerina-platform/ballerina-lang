package io.ballerina.projects.buildtools;

/**
 * {@code CodeGeneratorTool} represents a Ballerina build tool.
 *
 * @since 2201.9.0
 */
public interface CodeGeneratorTool {
    /**
     * Execute the command.
     *
     * @param  toolContext the {@link ToolContext} of the build tool
     */
    void execute(ToolContext toolContext);

    /**
     * Retrieve the tool name.
     *
     * @return the name of the tool.
     */
    String toolName();
}
