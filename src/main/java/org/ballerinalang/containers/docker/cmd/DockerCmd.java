package org.ballerinalang.containers.docker.cmd;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.containers.docker.cmd.validator.DockerHostValidator;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.impl.DefaultBallerinaDockerClient;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.launcher.LauncherUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Ballerina Command to support Docker based packaging Ballerina packages.
 */
@Parameters(commandNames = "docker", commandDescription = "Dockerize Ballerina program")
public class DockerCmd implements BLauncherCmd {

    private static final String DEFAULT_DOCKER_IMAGE_VERSION = "latest";

    private static final String BALLERINA_MAIN_PACKAGE_EXTENTION = "bmz";

    private static final String BALLERINA_SERVICE_PACKAGE_EXTENTION = "bsz";

    private static final String DEFAULT_DOCKER_HOST = "localhost";

    @Parameter(arity = 1, description = "builds the given package with all the dependencies")
    private List<String> packagePath;

    @Parameter(names = { "--tag", "-t" })
    private String dockerImageName;

    @Parameter(names = { "--host", "-h" }, validateWith = DockerHostValidator.class)
    private String dockerHost;

    @Override
    public void execute() {
        if (packagePath == null || packagePath.size() == 0) {
            throw LauncherUtils.createUsageException("no ballerina package is provided\n");
        }

        if (dockerHost == null) {
            dockerHost = DEFAULT_DOCKER_HOST;
            System.out.println("\nballerina: docker host URL is not provided. Using default docker host 'localhost'");
        }

        // extract the package name and extension
        String packageCompletePath = packagePath.get(0);
        String packageName = FilenameUtils.getBaseName(packageCompletePath);
        String packageExtention = FilenameUtils.getExtension(packageCompletePath);

        if (dockerImageName == null) {
            dockerImageName = packageName;
            System.out.println("ballerina: docker tag is not provided. Using " + packageName + ":"
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
            System.out.println("provided service package -- TODO remove this msg");
            try {
                new DefaultBallerinaDockerClient().createServiceImage("pkg1", null, Paths.get(packageCompletePath),
                        imageName, imageVersion);
            } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                System.out.println("Error : " + e.getMessage());
            }
            break;

        case BALLERINA_MAIN_PACKAGE_EXTENTION:
            System.out.println("provided main package -- TODO remove this msg");
            try {
                new DefaultBallerinaDockerClient().createMainImage("pkg1", null, Paths.get(packageCompletePath),
                        imageName, imageVersion);
            } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                System.out.println("Error : " + e.getMessage());
            }
            break;

        default:
            throw LauncherUtils.createUsageException("Invalid package extention\n");
        }

        String dockerHostString = "";  // TODO
        System.out.println(
                "\nYou can run the docker image as follows => docker run --name <container-name> -d " + imageName + ":" + imageVersion + "\n");
        System.out.println("Find the docker container IP using      => docker inspect <container-name> | grep IPAddress");
        System.out.println("Ballerina service will be running in http://<container-ip>:9090 \n");
        
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
}
