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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.ballerinalang.composer.service.workspace.Constants;
import org.ballerinalang.composer.service.workspace.api.PackagesApi;
import org.ballerinalang.composer.service.workspace.launcher.LaunchManager;
import org.ballerinalang.composer.service.workspace.rest.BallerinaProgramService;
import org.ballerinalang.composer.service.workspace.rest.FileServer;
import org.ballerinalang.composer.service.workspace.rest.WorkspaceService;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BLangFileRestService;
import org.ballerinalang.composer.service.workspace.rest.exception.DefaultExceptionMapper;
import org.ballerinalang.composer.service.workspace.rest.exception.FileNotFoundExceptionMapper;
import org.ballerinalang.composer.service.workspace.rest.exception.ParseCancellationExceptionMapper;
import org.ballerinalang.composer.service.workspace.rest.exception.SemanticExceptionMapper;
import org.ballerinalang.composer.service.workspace.swagger.factories.ServicesApiServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import java.nio.file.Paths;

/**
 * Workspace Service Entry point.
 *
 * @since 0.8.0
 */
public class WorkspaceServiceRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceServiceRunner.class);

    public static void main(String[] args) {
        String balHome = System.getProperty(Constants.SYS_BAL_COMPOSER_HOME);
        String filePath;
        if (balHome == null) {
            balHome = System.getenv(Constants.SYS_BAL_COMPOSER_HOME);
        }
        if (balHome == null) {
            logger.error("BAL_COMPOSER_HOME is not set. Please set ballerina.home system variable.");
            return;
        }

        boolean isCloudMode = Boolean.getBoolean(Constants.SYS_WORKSPACE_ENABLE_CLOUD);

        if (args.length > 1 && args[1] != null) {
            filePath = args[1];
        } else {
            filePath = "resources/composer/web/resources/samples/helloWorld.bal";
        }

//        // configure possible command line options
//        Options options = new Options();
//        Option cloudModeOption = new Option(Constants.CLOUD_MODE_INDICATOR_ARG,
//                Constants.CLOUD_MODE_INDICATOR_ARG_DESC);
//        options.addOption(cloudModeOption);
//        // read console args and process options
//        CommandLineParser parser = new DefaultParser();
//        HelpFormatter formatter = new HelpFormatter();
//        CommandLine commandLine;
//        try {
//            commandLine = parser.parse(options, args);
//            isCloudMode = commandLine.hasOption(Constants.CLOUD_MODE_INDICATOR_ARG);
//            logger.debug(isCloudMode ? "Cloud mode enabled." : "Running in local mode.");
//        } catch (ParseException e) {
//            // not a blocker
//            logger.warn("Exception while parsing console arguments.", e);
//            formatter.printHelp("workspace-service", options);
//        }

        Injector injector = Guice.createInjector(new WorkspaceServiceModule(isCloudMode));
        new MicroservicesRunner(Integer.getInteger(Constants.SYS_WORKSPACE_PORT, Constants.DEFAULT_WORKSPACE_PORT))
                .addExceptionMapper(new SemanticExceptionMapper())
                .addExceptionMapper(new ParseCancellationExceptionMapper())
                .addExceptionMapper(new FileNotFoundExceptionMapper())
                .addExceptionMapper(new DefaultExceptionMapper())
                .deploy(injector.getInstance(WorkspaceService.class))
                .deploy(new BLangFileRestService())
                .deploy(new PackagesApi())
                .deploy(ServicesApiServiceFactory.getServicesApi())
                .deploy(new BallerinaProgramService(filePath))
                .start();

        int port = Integer.getInteger(Constants.SYS_FILE_WEB_PORT, Constants.DEFAULT_FILE_WEB_PORT);
        String contextRoot = Paths.get(balHome, Constants.FILE_CONTEXT_RESOURCE, Constants
                .FILE_CONTEXT_RESOURCE_COMPOSER, Constants.FILE_CONTEXT_RESOURCE_COMPOSER_WEB)
                .toString();
        FileServer fileServer = new FileServer();
        fileServer.setContextRoot(contextRoot);
        new MicroservicesRunner(port)
                .deploy(fileServer)
                .start();

        //start the launcher service
        //The launcherservice was implemented with netty since msf4j do not have websocket support yet.
        LaunchManager launchManager = LaunchManager.getInstance();
        launchManager.init();

        if (!isCloudMode) {
            logger.info("Ballerina Composer URL: http://localhost:" + port);
        }
    }
}
