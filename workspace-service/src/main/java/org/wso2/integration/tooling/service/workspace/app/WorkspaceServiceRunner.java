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
package org.wso2.integration.tooling.service.workspace.app;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.integration.tooling.service.workspace.Constants;
import org.wso2.integration.tooling.service.workspace.rest.FileServer;
import org.wso2.integration.tooling.service.workspace.rest.WorkspaceService;
import org.wso2.msf4j.MicroservicesRunner;

/**
 * Workspace Service Entry point.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class WorkspaceServiceRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceServiceRunner.class);

    public static void main(String[] args) {

        boolean isCloudMode = false;

        // configure possible command line options
        Options options = new Options();
        Option cloudModeOption = new Option(Constants.CLOUD_MODE_INDICATOR_ARG,
                Constants.CLOUD_MODE_INDICATOR_ARG_DESC);
        options.addOption(cloudModeOption);
        // read console args and process options
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
            isCloudMode = commandLine.hasOption(Constants.CLOUD_MODE_INDICATOR_ARG);
            logger.debug(isCloudMode ? "Cloud mode enabled." : "Running in local mode.");
        } catch (ParseException e) {
            // not a blocker
            logger.warn("Exception while parsing console arguments.", e);
            formatter.printHelp("workspace-service", options);
        }

        Injector injector = Guice.createInjector(new WorkspaceServiceModule(isCloudMode));
        new MicroservicesRunner(Integer.getInteger("http.port", 8289))
                .deploy(injector.getInstance(WorkspaceService.class))
                .start();


        String contextRoot = System.getProperty("web.context");
        int port = Integer.getInteger("http.editor.port", 8288);
        if (contextRoot == null) {
            contextRoot = "web";
        }
        FileServer fileServer = new FileServer();
        fileServer.setContextRoot(contextRoot);
        new MicroservicesRunner(port)
                .deploy(fileServer)
                .start();
        if (!isCloudMode) {
            logger.info("Ballerina Editor URL: http://localhost:" + port);
        }
    }
}