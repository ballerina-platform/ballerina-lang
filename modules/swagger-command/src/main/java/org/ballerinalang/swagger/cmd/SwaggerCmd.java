package org.ballerinalang.swagger.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.swagger.codegen.CodegenConstants;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;
import org.wso2.ballerina.swagger.code.generator.cmd.Generate;

import java.util.List;

@Parameters(commandNames = "swagger", commandDescription = "Generate connector/service using swagger definition")
public class SwaggerCmd implements BLauncherCmd {

    @Parameter(arity = 1, description = "arguments")
    private List<String> argList;

    @Parameter(names = {"-d", "--directory"},
            description = "where to write the generated files (current dir by default)")
    private String output = "";

    @Parameter(names = {"-p", "packageName"}, description = CodegenConstants.API_PACKAGE_DESC)
    private String apiPackage;

    @Override
    public void execute() {
        if (argList == null || argList.size() < 2) {
            throw LauncherUtils.createUsageException("Swagger action and a swagger file should be provided. " +
                    "Ex: ballerina swagger connector swagger_file");
        }
        String action = argList.get(0);
        Generate generate = new Generate();
        switch (action) {
            case "connector":
                generate.setSpec(argList.get(1));
                generate.setLang("ballerina-connector");
                generate.setOutput(output);
                generate.setApiPackage(apiPackage);
                generate.run();
                break;
            case "skeleton":
                generate.setSpec(argList.get(1));
                generate.setLang("ballerina-skeleton");
                generate.setOutput(output);
                generate.setApiPackage(apiPackage);
                generate.run();
                break;
            case "mock":
                generate.setSpec(argList.get(1));
                generate.setLang("ballerina-mock-service");
                generate.setOutput(output);
                generate.setApiPackage(apiPackage);
                generate.run();
                break;
            default:
                throw LauncherUtils.createUsageException("Only following actions[connector, skeleton] are supported" +
                        " in swagger command");
        }
    }

    @Override
    public String getName() {
        return "swagger";
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("This is Swagger generator command");
        stringBuilder.append("\n");
        stringBuilder.append("ballerina swagger [action] [swaggerFile] [options]");
        stringBuilder.append("\n");
        stringBuilder.append("\n");
    }
}
