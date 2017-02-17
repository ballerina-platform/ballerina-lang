package org.ballerinalang.containers.docker.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.containers.docker.cmd.validator.DockerHostValidator;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.impl.DefaultBallerinaDockerClient;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;

/**
 * Ballerina Command to support Docker based packaging Ballerina packages.
 */
@Parameters(commandNames = "docker", commandDescription = "Dockerize Ballerina programs")
public class DockerCmd implements BLauncherCmd {

    private static final String DEFAULT_DOCKER_IMAGE_VERSION = "latest";
    private static final String BALLERINA_MAIN_PACKAGE_EXTENTION = "bmz";
    private static final String BALLERINA_SERVICE_PACKAGE_EXTENTION = "bsz";
    private static final String DEFAULT_DOCKER_HOST = "localhost";

    private static PrintStream outStream = System.err;
    private JCommander parentCmdParser = null;

    @Parameter(arity = 1, description = "builds the given package with all the dependencies")
    private List<String> packagePath;

    @Parameter(names = {"--tag", "-t"})
    private String dockerImageName;

    @Parameter(names = {"--host", "-H"}, validateWith = DockerHostValidator.class)
    private String dockerHost;

    @Parameter(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    @Override
    public void execute() {
            if (helpFlag) {
                LauncherUtils.printCommandUsageInfo(parentCmdParser, "service", outStream);
                return;
            }
        if (packagePath == null || packagePath.size() == 0) {
            throw LauncherUtils.createUsageException("no ballerina package is provided\n");
        }

        if (dockerHost == null) {
            dockerHost = DEFAULT_DOCKER_HOST;
            outStream.println("\nballerina: docker host URL is not provided. Using default docker host 'localhost'");
        }

        // extract the package name and extension
        String packageCompletePath = packagePath.get(0);
        String packageName = FilenameUtils.getBaseName(packageCompletePath);
        String packageExtention = FilenameUtils.getExtension(packageCompletePath);

        if (dockerImageName == null) {
            dockerImageName = packageName;
            outStream.println("ballerina: docker tag is not provided. Using " + packageName + ":"
                    + DEFAULT_DOCKER_IMAGE_VERSION + " as the tag");
        }

        String imageName = dockerImageName.split(":")[0];
        String imageVersion;
        if (dockerImageName.contains(":")) {
            imageVersion = dockerImageName.split(":")[1];
        } else {
            imageVersion = DEFAULT_DOCKER_IMAGE_VERSION;
        }

        switch (packageExtention) {
            case BALLERINA_SERVICE_PACKAGE_EXTENTION:
                outStream.println("provided service package -- TODO remove this msg");
                try {
                    new DefaultBallerinaDockerClient().createServiceImage("pkg1", null, Paths.get(packageCompletePath),
                            imageName, imageVersion);
                } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                    outStream.println("Error : " + e.getMessage());
                }
                break;

            case BALLERINA_MAIN_PACKAGE_EXTENTION:
                outStream.println("provided main package -- TODO remove this msg");
                try {
                    new DefaultBallerinaDockerClient().createMainImage("pkg1", null, Paths.get(packageCompletePath),
                            imageName, imageVersion);
                } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                    outStream.println("Error : " + e.getMessage());
                }
                break;

            default:
                throw LauncherUtils.createUsageException("Invalid package extention\n");
        }

        String dockerHostString = "";  // TODO
        outStream.println(
                "\nYou can run the docker image as follows => docker run --name <container-name> -d " +
                        imageName + ":" + imageVersion + "\n");
        outStream.println("Find the docker container IP using      " +
                "=> docker inspect <container-name> | grep IPAddress");
        outStream.println("Ballerina service will be running in http://<container-ip>:9090 \n");

        // TODO for main, run will be [docker run --name saj6 -it helloworld]

    }

    @Override
    public String getName() {
        return "docker";
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("ballerina docker <package-file-path> [--tag | -t <image-name>] [--host | -h <hostURL>]\n");
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {

    }
}
