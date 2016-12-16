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
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.BallerinaParserErrorStrategy;
import org.wso2.ballerina.core.parser.ParserException;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_BAL_ARGS;

/**
 * {@code BalDeployer} is responsible for all ballerina file deployment tasks
 */
@Component(
        name = "org.wso2.ballerina.core.runtime.deployer.BalDeployer",
        immediate = true,
        service = Deployer.class)
public class BalDeployer implements Deployer {

    private static final Logger log = LoggerFactory.getLogger(BalDeployer.class);

    private static final String BAL_FILES_DIRECTORY = "ballerina-files";
    private static final String FILE_EXTENSION = ".bal";
    private ArtifactType artifactType = new ArtifactType<>("bal");
    ;
    private URL directoryLocation;

    @Activate
    protected void activate(BundleContext bundleContext) {
    }

    @Override
    public void init() {
        try {
            directoryLocation = new URL("file:" + BAL_FILES_DIRECTORY);
        } catch (MalformedURLException e) {
            log.error("Error while initializing directoryLocation" + directoryLocation.getPath(), e);
        }
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        deployBalFile(artifact.getFile());
        return artifact.getFile().getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        undeployBalFile((String) key);
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        log.info("Updating " + artifact.getName() + "...");
        undeployBalFile(artifact.getName());
        deployBalFile(artifact.getFile());
        return artifact.getName();
    }

    @Override
    public URL getLocation() {
        return directoryLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public static void deployBalFile(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);

            if (file.getName().endsWith(FILE_EXTENSION)) {
                ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);
                
                // Setting the name of the source file being parsed, to the antlr inputstream.
                // This is required by the parser-error strategy.
                antlrInputStream.name = file.getName();
                
                BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
                CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

                BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
                ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());

//              // Visitor based approach
//              CompilationUnitVisitor ballerinaBaseVisitor = new CompilationUnitVisitor();
//              BallerinaFile balFile = (BallerinaFile) ballerinaBaseVisitor.accept(ballerinaParser.compilationUnit());

                // Listener based approach
//                BallerinaBaseListenerImpl ballerinaBaseListener = new BallerinaBaseListenerImpl();
//                ballerinaParser.addParseListener(ballerinaBaseListener);
//                ballerinaParser.compilationUnit();
//                BallerinaFile balFile = ballerinaBaseListener.balFile;

                // Builder based approach
                BLangModelBuilder bLangModelBuilder = new BLangModelBuilder();
                BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder);
                ballerinaParser.addParseListener(ballerinaBaseListener);
                ballerinaParser.compilationUnit();
                BallerinaFile balFile = bLangModelBuilder.build();

                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(balFile);
                balFile.accept(semanticAnalyzer);

                // Invoke the Linker
//                BallerinaLinker ballerinaLinker = new BallerinaLinker();
//                balFile.accept(ballerinaLinker);

                // Link function invocations and Run main function
                linkAndRunMainFunction(balFile);

                // Get the existing application associated with this ballerina config
                Application app = ApplicationRegistry.getInstance().getApplication(file.getName());
                if (app == null) {
                    // Create a new application with ballerina file name, if there is no app currently exists.
                    app = new Application(file.getName());
                    ApplicationRegistry.getInstance().registerApplication(app);
                }
                
                Package aPackage = app.getPackage(file.getName());
                if (aPackage == null) {
                    // check if package name is null
                    if (balFile.getPackageName() != null) {
                        aPackage = new Package(balFile.getPackageName());
                    } else {
                        aPackage = new Package("default");
                    }
                    app.addPackage(aPackage);

                }
                aPackage.addFiles(balFile);
                ApplicationRegistry.getInstance().updatePackage(aPackage);

                log.info("Deployed ballerina file : " + file.getName());
            }
        } catch (IOException e) {
            log.error("Error while creating Ballerina object model from file : " + file.getName(), e);
        } catch (ParserException e) {
            log.error("Failed to deploy " + file.getName() + ": " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Undeploy a service registered through a ballerina file.
     * 
     * @param fileName  Name of the ballerina file
     */
    private void undeployBalFile(String fileName) {
        Application app = ApplicationRegistry.getInstance().getApplication(fileName);
        if (app == null) {
            log.warn("Could not find service to undeploy: " + fileName + ".");
            return;
        }
        ApplicationRegistry.getInstance().unregisterApplication(app);
        log.info("Undeployed ballerina file : " + fileName);
    }
    

    private static void linkAndRunMainFunction(BallerinaFile bFile) {

//        // Linking functions defined in the same source file
//        for (FunctionInvocationExpr expr : bFile.getFuncIExprs()) {
//            SymbolName symName = expr.getFunctionName();
//            BallerinaFunction bFunction = (BallerinaFunction) bFile.getFunctions().get(symName.getName());
//            if (bFunction == null) {
//                throw new IllegalStateException("Undefined function: " + symName.getName());
//            }
//            expr.setFunction(bFunction);
//        }

        BLangLinker bLangLinker = new BLangLinker(bFile);
        bLangLinker.link(GlobalScopeHolder.getInstance().getScope());

        BallerinaFunction function = (BallerinaFunction) bFile.getFunctions().get("main");
        if (function == null) {
            return;
        }

        // Check whether this is a standard main function with one integer argument
        // This will be changed to string[] args once we have the array support
        Parameter[] parameters = function.getParameters();
        if (parameters.length != 1 || parameters[0].getTypeC() != TypeC.INT_TYPE) {
            log.info("main function is not comply with standard main function in ballerina, hence skipping");
            return;
        }

        // Execute main function
        // Create control stack and the stack frame
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();
        int sizeOfValueArray = function.getStackFrameSize();
        BValueRef[] values = new BValueRef[sizeOfValueArray];
        int i = 0;

        // Main function only have one input parameter
        // Read from command line arguments
        String balArgs = System.getProperty(SYSTEM_PROP_BAL_ARGS);

        // Only integers allowed at the moment
        if (balArgs != null) {
            int intValue = Integer.parseInt(balArgs);
            values[i++] = new BValueRef(new IntValue(intValue));
        } else {
            values[i++] = new BValueRef(new IntValue(0));
        }

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = function.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            values[i] = BValueRef.getDefaultValue(variableDcl.getTypeC());
            i++;
        }

        BValueRef[] returnVals = new BValueRef[function.getReturnTypesC().length];
        StackFrame stackFrame = new StackFrame(values, returnVals);
        controlStack.pushFrame(stackFrame);
        BLangInterpreter interpreter = new BLangInterpreter(ctx);
        function.accept(interpreter);
        log.info("return value: " + returnVals[0].getInt());
    }


}
