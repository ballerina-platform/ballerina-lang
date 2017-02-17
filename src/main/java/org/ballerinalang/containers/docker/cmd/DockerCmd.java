package org.ballerinalang.containers.docker.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.containers.docker.BallerinaDockerClient;
import org.ballerinalang.containers.docker.cmd.validator.DockerHostValidator;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.impl.DefaultBallerinaDockerClient;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ballerina Command to support Docker based packaging Ballerina packages.
 */
@Parameters(commandNames = "docker", commandDescription = "Dockerize Ballerina programs")
public class DockerCmd implements BLauncherCmd {

    private static final String DEFAULT_DOCKER_IMAGE_VERSION = "latest";
    private static final String BALLERINA_MAIN_PACKAGE_EXTENSION = "bmz";
    private static final String BALLERINA_SERVICE_PACKAGE_EXTENSION = "bsz";
    private static final String DEFAULT_DOCKER_HOST = "localhost";

    private static PrintStream outStream = System.err;
//    private JCommander parentCmdParser = null;

    @Parameter(arity = 1, description = "builds the given package with all the dependencies")
    private List<String> packagePathNames;

    @Parameter(names = {"--tag", "-t"}, description = " Docker image name. <image-name>:version")
    private String dockerImageName;

    @Parameter(names = {"--host", "-H"}, validateWith = DockerHostValidator.class,
            description = " Docker Host. http://127.0.0.1:2375 ")
    private String dockerHost;

    @Parameter(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    /**
     * Temporary usage printer
     */
    private static void printCommandUsageInfo() {
        outStream.println("Dockerize Ballerina programs");
        outStream.println();
        outStream.println("Usage:");
        outStream.println();
        outStream.println("ballerina docker <package-file-path> [--tag | -t <image-name>] [--host | -H <hostURL>] " +
                "--help | -h");
        outStream.println();
        outStream.println("Flags:");
        outStream.println("\t--tag, -t");
        outStream.println("\t--host, -H");
        outStream.println("\t--help, -h");
        outStream.println();
    }

    @Override
    public void execute() {
        if (helpFlag) {
            printCommandUsageInfo();
            return;
        }

        // temporary fix until https://github.com/ballerinalang/ballerina/issues/1937 gets fixed.
        Path path = Paths.get(System.getProperty("java.io.tmpdir"));
        if (!Files.exists(path)) {
            System.setProperty("java.io.tmpdir", "/tmp");
        }
        
        if (packagePathNames == null || packagePathNames.size() == 0) {
            throw LauncherUtils.createUsageException("no ballerina package is provided\n");
        }

        // extract the package name and extension
        String packageCompletePath = packagePathNames.get(0);
        String packageName = FilenameUtils.getBaseName(packageCompletePath);
        String packageExtension = FilenameUtils.getExtension(packageCompletePath);
        List<Path> packagePaths = new ArrayList<>();
        packagePaths.add(Paths.get(packageCompletePath));
        String imageName;
        String imageVersion;

        if (dockerImageName != null) {
            dockerImageName = dockerImageName.toLowerCase(Locale.getDefault());
            if (dockerImageName.contains(":")) {
                String[] imageNameParts = dockerImageName.split(":");
                imageName = imageNameParts[0];
                imageVersion = imageNameParts[1];
            } else {
                imageName = dockerImageName;
                imageVersion = DEFAULT_DOCKER_IMAGE_VERSION;
            }
        } else {
            imageName = packageName;
            imageVersion = DEFAULT_DOCKER_IMAGE_VERSION;
        }

        if (!canProceed(imageName, imageVersion)) {
            outStream.println("ballerina: aborting..\n");
            return;
        }

        BallerinaDockerClient dockerClient = new DefaultBallerinaDockerClient();

        switch (packageExtension) {
            case BALLERINA_SERVICE_PACKAGE_EXTENSION:
                try {
                    String createdImageName = dockerClient.createServiceImage(packageName, dockerHost,
                            packagePaths, imageName, imageVersion);
                    if (createdImageName != null) {
                        printServiceImageSuccessMessage(createdImageName);
                    } else {
                        throw LauncherUtils.createUsageException("Docker image build failed for image "
                                + imageName + ":" + imageVersion + ".");
                    }
                } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                    throw LauncherUtils.createUsageException(e.getMessage());
                }
                break;

            case BALLERINA_MAIN_PACKAGE_EXTENSION:
                try {
                    String createdImageName = dockerClient.createMainImage(packageName, dockerHost, packagePaths,
                            imageName, imageVersion);
                    if (createdImageName != null) {
                        printMainImageSuccessMessage(createdImageName);
                    } else {
                        throw LauncherUtils.createUsageException("Docker image build failed for image "
                                + imageName + ":" + imageVersion + ".");
                    }
                } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                    throw LauncherUtils.createUsageException(e.getMessage());
                }
                break;

            default:
                throw LauncherUtils.createUsageException("invalid package extension\n");
        }
    }

    private boolean canProceed(String imageName, String imageVersion) {
        Console c = System.console();
        String choice;
        String dockerHostToPrint = (dockerHost != null) ? dockerHost : DEFAULT_DOCKER_HOST;

        do {
            choice = c.readLine("ballerina: build docker image [" + imageName + ":" + imageVersion
                    + "] in docker host [" + dockerHostToPrint + "]? (y/n): ");

            if (choice.equalsIgnoreCase("y")) {
                return true;
            }

            if (choice.equalsIgnoreCase("n")) {
                return false;
            }

        } while (true);
    }

    private void printServiceImageSuccessMessage(String imageName) {
        outStream.println("\nYou can run the docker image as follows => docker run --name <container-name> -d "
                + imageName + "\n");
        outStream.println(
                "Find the docker container IP using      " + "=> docker inspect <container-name> | grep IPAddress");
        outStream.println("Ballerina service will be running in http://<container-ip>:9090 \n");
        outStream.println("Sample request would be [curl -X GET http://<container-ip>:9090/hello ]\n");
    }

    private void printMainImageSuccessMessage(String imageName) {
        outStream.println("\nYou can run the docker image as follows => docker run --name <container-name> -it "
                + imageName + "\n");
    }

    @Override
    public String getName() {
        return "docker";
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("ballerina docker <package-file-path> [--tag | -t <image-name>] [--host | -H <docker-hostURL>] " +
                "--help | -h\n");
    }

}
