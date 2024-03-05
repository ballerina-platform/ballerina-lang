package io.ballerina.projects.buildtools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a CodeGeneratorTool class with ToolConfig to provide the command name and the list of subcommands.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToolConfig {

    /**
     * Set the command name.
     * @return the name of the command
     */
    String name();

    /**
     * Set the subcommands belonging to the command
     * @return Array of subcommands
     */
    Class<? extends CodeGeneratorTool>[] subcommands() default {};
}
