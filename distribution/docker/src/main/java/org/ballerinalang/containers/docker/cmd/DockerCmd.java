/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.containers.docker.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.containers.Constants;
import org.ballerinalang.containers.docker.BallerinaDockerClient;
import org.ballerinalang.containers.docker.cmd.validator.DockerHostValidator;
import org.ballerinalang.containers.docker.cmd.validator.DockerImageNameValidator;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.impl.DefaultBallerinaDockerClient;
import org.ballerinalang.containers.docker.utils.Utils;
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
@Parameters(commandNames = "docker", commandDescription = "create docker images for Ballerina program archives")
public class DockerCmd implements BLauncherCmd {

    private static final String BALLERINA_COMPILED_PACKAGE_EXTENSION = "balx";
    private static final String BALLERINA_NON_COMPILED_PACKAGE_EXTENSION = "bal";
    private static final String DEFAULT_DOCKER_HOST = "localhost";
    private static final String OS_NAME = "os.name";
    private static final String OS_NAME_WINDOWS = "Windows";

    private static PrintStream outStream = System.err;
    private JCommander parentCmdParser = null;
    private BallerinaDockerClient dockerClient = new DefaultBallerinaDockerClient();

    @Parameter(arity = 1, description = "package names")
    private List<String> packagePathNames;

    @Parameter(names = {"--tag", "-t"}, validateWith = DockerImageNameValidator.class,
            description = "docker image name. <image-name>:<version>")
    private String dockerImageName;

    @Parameter(names = {"--host", "-H"}, validateWith = DockerHostValidator.class,
            description = "docker Host. http://<ip-address>:<port>")
    private String dockerHost;

    @Parameter(names = {"--help", "-h"}, hidden = true, description = "show usage")
    private boolean helpFlag;

    @Parameter(names = {"--yes", "-y"}, description = "assume yes for prompts")
    private boolean assumeYes;

    /**
     * Create an image name and image version from the given (if) image name and the package names.
     *
     * @param givenImageName The provided image name
     * @param packageName    The provided package name.
     * @return A {@link String} array containing the image name [0] and image version [1].
     */
    private static String[] getImageNameDetails(String givenImageName, String packageName) {
        if (givenImageName != null) {
            givenImageName = givenImageName.toLowerCase(Locale.getDefault());
            if (givenImageName.contains(":")) {
                return givenImageName.split(":");
            } else {
                return new String[]{givenImageName.toLowerCase(Locale.getDefault()), Constants.IMAGE_VERSION_LATEST};
            }
        } else {
            return new String[]{packageName.toLowerCase(Locale.getDefault()), Constants.IMAGE_VERSION_LATEST};
        }
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, getName());
            outStream.println(commandUsageInfo);
            return;
        }

        if (packagePathNames == null || packagePathNames.size() == 0) {
            throw LauncherUtils.createUsageException("no ballerina package is provided\n");
        }

        // extract the package name and extension
        String packageCompletePath = packagePathNames.get(0);
        if (!Files.exists(Paths.get(packageCompletePath))) {
            throw LauncherUtils.createUsageException("cannot find ballerina package: " + packageCompletePath);
        }

        if (StringUtils.isEmpty(dockerHost)) {
            String osName = System.getProperty(OS_NAME);
            if (osName != null && osName.contains(OS_NAME_WINDOWS)) {
                throw LauncherUtils.createUsageException("docker host parameter is required");
            }
        }

        String packageName = FilenameUtils.getBaseName(packageCompletePath);
        String packageExtension = FilenameUtils.getExtension(packageCompletePath);
        List<Path> packagePaths = new ArrayList<>();
        packagePaths.add(Paths.get(packageCompletePath));

        String[] imageNameParts = getImageNameDetails(dockerImageName, packageName);
        String imageName = imageNameParts[0];
        String imageVersion = imageNameParts[1];

        if (!assumeYes && !canProceed(imageName, imageVersion)) {
            outStream.println("ballerina: aborting..\n");
            return;
        }

        switch (packageExtension) {
            case BALLERINA_COMPILED_PACKAGE_EXTENSION:
            case BALLERINA_NON_COMPILED_PACKAGE_EXTENSION:
                if (!(packagePathNames.size() == 1)) {
                    throw LauncherUtils.createUsageException("Only one ballerina bal/balx file can be specified\n");
                }
                try {
                    String createdImageName = dockerClient.createMainImage(packageName, dockerHost, packagePaths.get(0),
                            imageName, imageVersion);
                    if (createdImageName != null) {
                        printImageSuccessMessage(createdImageName);
                    } else {
                        throw LauncherUtils.createUsageException("Docker image build failed for image "
                                + imageName + "." + imageVersion + " : " + dockerClient.getBuildError());
                    }
                } catch (BallerinaDockerClientException | IOException | InterruptedException e) {
                    throw LauncherUtils.createUsageException(e.getMessage());
                }
                break;


            default:
                throw LauncherUtils.createUsageException("invalid package extension\n");
        }
    }

    @Override
    public String getName() {
        return "docker";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Creates docker images for Ballerina programs." + System.lineSeparator());
        out.append(System.lineSeparator());
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("ballerina docker <package-name> [--tag | -t <image-name>] [--host | -H <docker-hostURL>] " +
                "--help | -h --yes | -y\n");
    }

    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }

    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    }

    /**
     * Prompt user to continue Docker image building.
     *
     * @param imageName    Name of the image to be built.
     * @param imageVersion Version of the image to be built
     * @return True if confirmed, false if aborted or exceeded attempts.
     */
    private boolean canProceed(String imageName, String imageVersion) {
        Console console = System.console();
        String choice;
        String dockerHostToPrint = (dockerHost != null) ? dockerHost : DEFAULT_DOCKER_HOST;

        int attempts = 3;
        do {
            choice = console.readLine("Build docker image [" + imageName + ":" + imageVersion
                    + "] in docker host [" + dockerHostToPrint + "]? (y/n): ");

            if (choice.equalsIgnoreCase("y")) {
                return true;
            }

            if (choice.equalsIgnoreCase("n")) {
                return false;
            }

        } while (--attempts > 0);

        return false;
    }

    /*
    Print helpful messages for functionality to perform after image build for a Ballerina bal/balx main/service files.
     */
    private void printImageSuccessMessage(String imageName) {
        String containerName = Utils.generateContainerName();
        int portNumber = Utils.generateContainerPort();
        outStream.println("\nDocker image " + imageName + " successfully built.");
        outStream.println();
        outStream.println("\nUse the following command to execute the main program bal/balx in a container.");
        outStream.println("\tdocker run --name " + containerName + " -it " + imageName);
        outStream.println();
        outStream.println("Use the following command to start the main service bal/balx in container.");
        outStream.println("\tdocker run -p " + portNumber + ":9090 --name " + containerName + " -d " + imageName);
        outStream.println();
        outStream.println("Use the following command to inspect the logs.");
        outStream.println("\tdocker logs " + containerName);
        outStream.println();
        outStream.println("Use the following command to retrieve the IP address of the container");
        outStream.println("\tdocker inspect " + containerName + " | grep IPAddress");
        outStream.println();
        outStream.println("Ballerina service will be running on the following ports.");
        outStream.println("\thttp://localhost:" + portNumber);
        outStream.println("\thttp://<container-ip>:9090");
        outStream.println();
        outStream.println("Make requests using the format [curl -X <http-method> http://localhost:" + portNumber
                + "/<service-name>]");
        outStream.println();
    }
}
