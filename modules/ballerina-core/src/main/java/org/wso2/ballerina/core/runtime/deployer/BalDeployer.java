/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime.deployer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.LinkerException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.BallerinaParserErrorStrategy;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.runtime.internal.ServiceContextHolder;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@code BalDeployer} is responsible for all ballerina file deployment tasks.
 *
 * @since 0.8.0
 */
public class BalDeployer {

    private static final Logger log = LoggerFactory.getLogger(BalDeployer.class);

    private static final String FILE_EXTENSION = ".bal";

    /**
     * Deploy given Ballerina file.
     *
     * @param file Ballerina file.
     * @return Number of services deployed.
     */
    public static int deployBalFile(File file) {
        InputStream inputStream = null;
        boolean successful = false;
        try {
            inputStream = new FileInputStream(file);

            if (file.getName().endsWith(FILE_EXTENSION)) {
                ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);

                // Setting the name of the source file being parsed, to the ANTLR input stream.
                // This is required by the parser-error strategy.
                antlrInputStream.name = file.getName();

                BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
                CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

                BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
                ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());

                BLangModelBuilder bLangModelBuilder = new BLangModelBuilder(null);
                BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder);
                ballerinaParser.addParseListener(ballerinaBaseListener);
                ballerinaParser.compilationUnit();
                BallerinaFile balFile = bLangModelBuilder.build();

                SymScope globalScope = GlobalScopeHolder.getInstance().getScope();
//                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(balFile, globalScope);
//                balFile.accept(semanticAnalyzer);

                if (Constants.RuntimeMode.RUN_FILE == ServiceContextHolder.getInstance().getRuntimeMode()) {
                    BallerinaFunction function = (BallerinaFunction) balFile.getMainFunction();
                    if (function != null) {
                        ServiceContextHolder.getInstance().setBallerinaFileToExecute(balFile);
                        successful = true;
                        return 0;
                    } else {
                        log.error("Error: Unable to locate Main function.");
                        ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
                        successful = false;
                        return 0;
                    }
                }

                // Create a runtime environment for this Ballerina application
                RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(balFile);

                // Get the existing application associated with this ballerina config
                Application app = ApplicationRegistry.getInstance().getApplication(file.getName());
                if (app == null) {
                    // Create a new application with ballerina file name, if there is no app currently exists.
                    app = new Application(file.getName());
                    app.setRuntimeEnv(runtimeEnv);
                    ApplicationRegistry.getInstance().registerApplication(app);
                }

                Package aPackage = app.getPackage(file.getName());
                if (aPackage == null) {
                    // check if package name is null
                    if (balFile.getPackagePath() != null) {
                        aPackage = new Package(balFile.getPackagePath());
                    } else {
                        aPackage = new Package("default");
                    }
                    app.addPackage(aPackage);
                }
                aPackage.addFiles(balFile);

                // Here we need to link all the resources with this application. We execute the matching resource
                // when a request is made. At that point, we need to access runtime environment to execute the resource.
                for (Service service : balFile.getServices()) {
                    for (Resource resource : service.getResources()) {
                        resource.setApplication(app);
                    }
                }

                ApplicationRegistry.getInstance().updatePackage(aPackage);
                successful = true;
                log.info("Deployed ballerina file: " + file.getName());
                return balFile.getServices().length;
            } else {
                if (Constants.RuntimeMode.RUN_FILE == ServiceContextHolder.getInstance().getRuntimeMode()) {
                    log.error("Error: File extension not supported. Supported extensions {}.", FILE_EXTENSION);
                    ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
                }
                log.error("Error: File extension not supported. Support only {}.", FILE_EXTENSION);
                return 0;
            }
        } catch (ParseCancellationException | SemanticException | LinkerException e) {
            log.error(e.getMessage());
            successful = false;
        } catch (Throwable e) {
            log.error(file.getName() + ": " + e.getMessage());
            successful = false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
            if (!successful && Constants.RuntimeMode.RUN_FILE == ServiceContextHolder.getInstance().getRuntimeMode()) {
                ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
            }
        }
        return 0;
    }

//    /**
//     * Deploy all Ballerina files in a give directory.
//     *
//     * @param files to deploy.
//     */
//    public static void deployBalFiles(File[] files) {
//        if (files != null) {
//            Arrays.stream(files).filter(file1 -> file1.getName().endsWith(FILE_EXTENSION)).forEach
//                    (BalDeployer::deployBalFile);
//        }
//    }

    /**
     * Undeploy a service registered through a ballerina file.
     *
     * @param fileName Name of the ballerina file
     */
    public void undeployBalFile(String fileName) {
        Application app = ApplicationRegistry.getInstance().getApplication(fileName);
        if (app == null) {
            log.warn("Warning: Could not find service to undeploy: " + fileName + ".");
            return;
        }
        ApplicationRegistry.getInstance().unregisterApplication(app);
        log.info("Undeployed ballerina file : " + fileName);
    }

}
