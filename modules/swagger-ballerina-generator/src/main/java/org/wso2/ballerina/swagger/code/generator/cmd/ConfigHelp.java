package org.wso2.ballerina.swagger.code.generator.cmd;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenConfigLoader;
import org.wso2.ballerina.swagger.code.generator.BallerinaConnectorCodeGenerator;

@Command(name = "config-help", description = "Config help for chosen lang")
public class ConfigHelp implements Runnable {

    @Option(name = {"-l", "--lang"}, title = "language", required = true,
            description = "language to get config help for")
    private String lang;

    @Override
    public void run() {
        System.out.println();
        CodegenConfig config = new BallerinaConnectorCodeGenerator();

        System.out.println("CONFIG OPTIONS");
        for (CliOption langCliOption : config.cliOptions()) {
            System.out.println("\t" + langCliOption.getOpt());
            System.out.println("\t    " + langCliOption.getOptionHelp().replaceAll("\n", "\n\t    "));
            System.out.println();
        }
    }
}
