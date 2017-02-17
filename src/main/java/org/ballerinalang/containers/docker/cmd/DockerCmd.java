package org.ballerinalang.containers.docker.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.containers.docker.cmd.validator.DockerHostValidator;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.impl.DefaultBallerinaDockerClient;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;

/**
 * Ballerina Command to support Docker based packaging Ballerina packages.
 */
@Parameters(commandNames = "docker", commandDescription = "Dockerize Ballerina program")
public class DockerCmd implements BLauncherCmd {

    private static final String DEFAULT_DOCKER_IMAGE_VERSION = "latest";
    private static final String BALLERINA_MAIN_PACKAGE_EXTENTION = "bmz";
    private static final String BALLERINA_SERVICE_PACKAGE_EXTENTION = "bsz";
    private static final String DEFAULT_DOCKER_HOST = "localhost";

    private static PrintStream outStream = System.err;

    @Parameter(arity = 1, description = "builds the given package with all the dependencies")
    private List<String> packagePath;

    @Parameter(names = { "--tag", "-t" }, description = " Docker image name. <image-name>:version")
    private String dockerImageName;

    @Parameter(names = { "--host", "-h" }, validateWith = DockerHostValidator.class, description = " Docker Host. http://127.0.0.1:2375 ")
    private String dockerHost;

    @Override
    public void execute() {
        if (packagePath == null || packagePath.size() == 0) {
            throw LauncherUtils.createUsageException("no ballerina package is provided\n");
        }

        // extract the package name and extension
        String packageCompletePath = packagePath.get(0);
        String packageName = FilenameUtils.getBaseName(packageCompletePath);
        String packageExtention = FilenameUtils.getExtension(packageCompletePath);

        if (dockerImageName == null) {
            dockerImageName = packageName;
        }

        String imageName = dockerImageName.split(":")[0];
        String imageVersion;
        if (dockerImageName.contains(":")) {
            imageVersion = dockerImageName.split(":")[1];
        } else {
            imageVersion = DEFAULT_DOCKER_IMAGE_VERSION;
        }
        String dockerHostToPrint = dockerHost != null ? dockerHost : DEFAULT_DOCKER_HOST;
        String choice = confirm(imageName, imageVersion, dockerHostToPrint);

        if (choice.equals("N")) {
            outStream.println("Exiting..\n");
            return;
        }

        switch (packageExtention) {
        case BALLERINA_SERVICE_PACKAGE_EXTENTION:
            outStream.println("provided service package -- TODO remove this msg");
            try {
                new DefaultBallerinaDockerClient().createServiceImage("pkg1", dockerHost,
                        Paths.get(packageCompletePath), imageName, imageVersion);
                printServiceImageSuccessMessage(imageName.toLowerCase(), imageVersion);
            } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                outStream.println("Error : " + e.getMessage());
                return;
            }
            break;

        case BALLERINA_MAIN_PACKAGE_EXTENTION:
            outStream.println("provided main package -- TODO remove this msg");
            try {
                new DefaultBallerinaDockerClient().createMainImage("pkg1", dockerHost, Paths.get(packageCompletePath),
                        imageName, imageVersion);
                printMainImageSuccessMessage(imageName.toLowerCase(), imageVersion);
            } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                outStream.println("Error : " + e.getMessage());
                return;
            }
            break;

        default:
            throw LauncherUtils.createUsageException("Invalid package extention\n");
        }

    }

    private String confirm(String imageName, String imageVersion, String dockerHostToPrint) {
        Console c = System.console();
        String choice = c.readLine("\n Building docker image [" + imageName.toLowerCase() + ":" + imageVersion + "] "
                + " in docker host [" + dockerHostToPrint + "]. Confirm ? (Y/N) : ");

        boolean noMatch;
        do {

            if (choice.equals("Y") || choice.equals("N")) {
                noMatch = false;
            } else {
                noMatch = true;
            }

            if (noMatch) {
                choice = c.readLine(" Please enter Y or N : ");
            } else {
                break;
            }
        } while (noMatch);
        return choice;
    }

    private void printServiceImageSuccessMessage(String imageName, String imageVersion) {
        outStream.println("\nYou can run the docker image as follows => docker run --name <container-name> -d "
                + imageName.toLowerCase() + ":" + imageVersion + "\n");
        outStream.println(
                "Find the docker container IP using      " + "=> docker inspect <container-name> | grep IPAddress");
        outStream.println("Ballerina service will be running in http://<container-ip>:9090 \n");
        outStream.println("Sample request would be [curl -X GET http://<container-ip>:9090/hello ]\n");

    }

    private void printMainImageSuccessMessage(String imageName, String imageVersion) {
        outStream.println("\nYou can run the docker image as follows => docker run --name <container-name> -it "
                + imageName.toLowerCase() + ":" + imageVersion + "\n");
    }

    @Override
    public String getName() {
        return "docker";
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("ballerina docker <package-file-path> [--tag | -t <image-name>] [--host | -h <docker-hostURL>]\n");
    }
}
