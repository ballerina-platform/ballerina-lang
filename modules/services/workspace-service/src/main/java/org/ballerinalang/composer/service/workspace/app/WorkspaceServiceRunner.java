/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.ballerinalang.composer.service.workspace.Constants;
import org.ballerinalang.composer.service.workspace.api.PackagesApi;
import org.ballerinalang.composer.service.workspace.langserver.LangServerManager;
import org.ballerinalang.composer.service.workspace.launcher.LaunchManager;
import org.ballerinalang.composer.service.workspace.launcher.util.LaunchUtils;
import org.ballerinalang.composer.service.workspace.rest.BallerinaProgramService;
import org.ballerinalang.composer.service.workspace.rest.ConfigServiceImpl;
import org.ballerinalang.composer.service.workspace.rest.FileServer;
import org.ballerinalang.composer.service.workspace.rest.WorkspaceService;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BLangFileRestService;
import org.ballerinalang.composer.service.workspace.rest.exception.DefaultExceptionMapper;
import org.ballerinalang.composer.service.workspace.rest.exception.FileNotFoundExceptionMapper;
import org.ballerinalang.composer.service.workspace.rest.exception.ParseCancellationExceptionMapper;
import org.ballerinalang.composer.service.workspace.rest.exception.SemanticExceptionMapper;
import org.ballerinalang.composer.service.workspace.rest.typelattice.TypeLatticeService;
import org.ballerinalang.composer.service.workspace.swagger.factories.ServicesApiServiceFactory;
import org.ballerinalang.composer.service.workspace.utils.WorkspaceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Workspace Service Entry point.
 *
 * @since 0.8.0
 */
public class WorkspaceServiceRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceServiceRunner.class);

    public static void main(String[] args) {
        String balHome = System.getProperty(Constants.SYS_BAL_COMPOSER_HOME);
        if (balHome == null) {
            balHome = System.getenv(Constants.SYS_BAL_COMPOSER_HOME);
        }
        if (balHome == null) {
            // this condition will never reach if the app is started with 'composer' script.
            logger.error(Constants.COMPOSER_HOME_NOT_FOUND_ERROR_MESSAGE);
            return;
        }

        ComposerCommand composer = new ComposerCommand();
        JCommander jcomander = new JCommander(composer);
        jcomander.setProgramName("composer");
        try {
            jcomander.parse(args);
        } catch (ParameterException e) {
            PrintStream err = System.err;
            err.println("Invalid argument passed.");
            printUsage();
            return;
        }

        if (composer.helpFlag) {
            PrintStream out = System.out;
            out.println("Ballerina composer, helps you to visualize and edit ballerina programs.");
            out.println();
            out.println("Find more information at http://ballerinalang.org");
            printUsage();
            return;
        }

        String apiPath = null;
        String launcherPath = null;
        String langserverPath = null;
        String debuggerPath = null;
        String[] rootDirectoriesArray = null;
        String tryServiceURL = null;
        // reading configurations from workspace-service-config.yaml. Users are expected to drop the
        // workspace-service-config.yaml file inside the $ballerina-tools-distribution/resources/composer/services
        // directory. Default configurations will be set if user hasn't provided workspace-service-config.yaml
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File configFile = new File("./resources/composer/services/workspace-service-config.yaml");
        if (configFile.exists()) {
            try {
                WorkspaceServiceConfig workspaceServiceConfig = mapper.readValue(configFile,
                        WorkspaceServiceConfig.class);
                apiPath = workspaceServiceConfig.getApiPath();
                launcherPath = workspaceServiceConfig.getLauncherPath();
                langserverPath = workspaceServiceConfig.getLangserverPath();
                debuggerPath = workspaceServiceConfig.getDebuggerPath();
                if (workspaceServiceConfig.getRootDirectories() != null) {
                    rootDirectoriesArray = workspaceServiceConfig.getRootDirectories().split(",");
                }
                tryServiceURL = workspaceServiceConfig.getTryServiceURL();
            } catch (IOException e) {
                logger.error("Error while reading workspace-service-config.yaml");
            }
        }

        /*
           Check if the ports have conflicts, if there are conflicts following is the plan.
               1. If file-server port has a conflict we will inform the user and ask for a different port.
               2. If API port(s) have a conflict we will find next available port and start the service.
               3. The service ports will be passed to the editor app via a file server API.
        */
        //check if another instance of an editor started if so terminate the program and inform user
        int fileServerPort = Integer.getInteger(Constants.SYS_FILE_WEB_PORT, Constants.DEFAULT_FILE_WEB_PORT);
        //if a custom port is given give priority to that.
        if (null != composer.fileServerPort) {
            //if the file server port is set to 0 we will
            if (composer.fileServerPort.equals(0)) {
                fileServerPort = WorkspaceUtils.getAvailablePort(fileServerPort);
            } else {
                fileServerPort = composer.fileServerPort.intValue();
            }
        }

        if (!WorkspaceUtils.available(fileServerPort)) {
            PrintStream err = System.err;
            err.println("Error: Looks like you may be running the Ballerina composer already ?");
            err.println(String.format("In any case, it appears someone is already using port %d, " +
                    "please kick them out or tell me a different port to use.", fileServerPort));
            printUsage();
            System.exit(1);
        }

        //find free ports for API ports.
        int apiPort = Integer.getInteger(Constants.SYS_WORKSPACE_PORT, Constants.DEFAULT_WORKSPACE_PORT);
        apiPort = WorkspaceUtils.getAvailablePort(apiPort);

        //find free port for launch service.
        int launcherPort = apiPort + 1;
        int langserverPort = apiPort + 2;
        launcherPort = WorkspaceUtils.getAvailablePort(launcherPort);
        langserverPort = WorkspaceUtils.getAvailablePort(langserverPort);

        // find free port for debugger
        int debuggerPort = LaunchUtils.getFreePort();

        boolean isCloudMode = Boolean.getBoolean(Constants.SYS_WORKSPACE_ENABLE_CLOUD);

        Injector injector = Guice.createInjector(new WorkspaceServiceModule(isCloudMode));
        WorkspaceService workspaceService = injector.getInstance(WorkspaceService.class);

        // set list of root directories provided in workspace-service-config.yaml configuration file
        if (rootDirectoriesArray != null && rootDirectoriesArray.length > 0) {
            List<Path> rootDirs = new ArrayList<Path>();
            Stream.of(rootDirectoriesArray).forEach((rootDirectory) -> {
                rootDirs.add(Paths.get(rootDirectory));
            });
            workspaceService.setRootPaths(rootDirs);
        }

        new MicroservicesRunner(apiPort)
                .addExceptionMapper(new SemanticExceptionMapper())
                .addExceptionMapper(new ParseCancellationExceptionMapper())
                .addExceptionMapper(new FileNotFoundExceptionMapper())
                .addExceptionMapper(new DefaultExceptionMapper())
                .deploy(workspaceService)
                .deploy(new BLangFileRestService())
                .deploy(new PackagesApi())
                .deploy(ServicesApiServiceFactory.getServicesApi())
                .deploy(new BallerinaProgramService())
                .deploy(new TypeLatticeService())
                .start();



        String contextRoot = Paths.get(balHome, Constants.FILE_CONTEXT_RESOURCE, Constants
                .FILE_CONTEXT_RESOURCE_COMPOSER, Constants.FILE_CONTEXT_RESOURCE_COMPOSER_WEB)
                .toString();
        FileServer fileServer = new FileServer();

        ConfigServiceImpl configService = new ConfigServiceImpl();
        configService.setApiPort(apiPort);
        configService.setLauncherPort(launcherPort);
        configService.setLangserverPort(langserverPort);
        configService.setDebuggerPort(debuggerPort);
        configService.setApiPath(apiPath);
        configService.setLauncherPath(launcherPath);
        configService.setLangserverPath(langserverPath);
        configService.setDebuggerPath(debuggerPath);
        configService.setStartupFile(composer.filePath);


        fileServer.setContextRoot(contextRoot);
        new MicroservicesRunner(fileServerPort)
                .deploy(configService)
                .deploy(fileServer)
                .start();

        //start the launcher service
        //The launcher service was implemented with netty since msf4j do not have websocket support yet.
        LaunchManager launchManager = LaunchManager.getInstance();
        launchManager.init(launcherPort, tryServiceURL);

        LangServerManager langServerManager = LangServerManager.getInstance();
        langServerManager.init(langserverPort);

        if (!isCloudMode) {
            logger.info("Ballerina Composer URL: http://localhost:" + fileServerPort);
        }
    }

    private static class ComposerCommand {

        @Parameter(names = {"--help", "-h", "help"}, hidden = true, help = true)
        private boolean helpFlag = false;

        @Parameter(names = "--port", description = "Specify a custom port for file server to start.")
        private Integer fileServerPort;

        @Parameter(names = "--debug", hidden = true)
        private String debugPort;

        @Parameter(names = "--file" , description = "Specify a Ballerina program file to open at the startup.")
        private String filePath;
    }

    private static void printUsage() {
        PrintStream out = System.out;
        out.println("");
        out.println("Usage: composer [options]");
        out.println("  Options:");
        out.println("    --port <port_number>      Specify a custom port for file server to start.");
        out.println("    --file <file path>        Specify a Ballerina program file to open at the startup.");
        out.println("    --help -h help            for more information.");
        out.println("");
    }
}
