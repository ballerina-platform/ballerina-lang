package org.wso2.ballerina.swagger.code.generator;

import io.airlift.airline.Cli;
import io.airlift.airline.Help;
import org.wso2.ballerina.swagger.code.generator.cmd.ConfigHelp;
import org.wso2.ballerina.swagger.code.generator.cmd.Generate;
import org.wso2.ballerina.swagger.code.generator.cmd.Meta;
import org.wso2.ballerina.swagger.code.generator.cmd.Version;
import org.wso2.ballerina.swagger.code.generator.cmd.Langs;

/**
 * Swagger to Ballerina connector driver class.
 */
public class SwaggerCodegen {


    public static void main(String[] args) {
        String version = Version.readVersionFromResources();
        @SuppressWarnings("unchecked")
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("swagger-codegen-cli")
                .withDescription(String.format(
                        "Swagger code generator CLI (version %s). More info on swagger.io",
                        version))
                .withDefaultCommand(Langs.class)
                .withCommands(
                        Generate.class,
                        Meta.class,
                        Langs.class,
                        Help.class,
                        ConfigHelp.class,
                        Version.class
                );

        builder.build().parse(args).run();
    }
}
