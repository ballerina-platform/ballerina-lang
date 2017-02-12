/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core;

//import org.ballerinalang.testerina.core.langutils.Functions;

import org.ballerinalang.mockerina.MockerinaRegistry;
import org.ballerinalang.testerina.core.entity.TesterinaFile;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.testerina.core.langutils.ParserUtils;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.ballerina.core.runtime.registry.ApplicationRegistry;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

//import org.wso2.ballerina.core.model.BallerinaFile;
//import org.wso2.ballerina.core.model.values.BValue;

/**
 * TestRunner entity class
 */
public class TestRunner {

    private BallerinaFile bFile;

    private static final Logger logger = java.util.logging.Logger.getLogger("TestRunner");
//    private static PrintStream outStream = System.err;


    public static void runTest(Path[] sourceFilePaths) {
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());

        Path sourceFilePath = sourceFilePaths[0]; //todo
        BallerinaFile bFile = ParserUtils.buildLangModel(sourceFilePath);

        // Load Client Connectors
        BallerinaConnectorManager.getInstance().initializeClientConnectors(new MessageProcessor());

        // Check whether there is a main function
        BallerinaFunction function = (BallerinaFunction) bFile.getMainFunction();
        if (function == null) {
            String pkgString = (bFile.getPackagePath() != null) ? "in package " + bFile.getPackagePath() : "";
            pkgString = (pkgString.equals("")) ? "in file '" + ParserUtils.getFileName(sourceFilePath) + "'" : "";
            String errorMsg = "ballerina: main method not found " + pkgString + "";
            throw new BallerinaException(errorMsg);
        }

        buildApplicationModel(bFile, sourceFilePath, "default");


        execute(bFile, function, Collections.emptyList());

        Runtime.getRuntime().exit(0);
    }

    private static void buildApplicationModel(BallerinaFile originalBFile, Path sourceFilePath, String appName) {
        try {
            MockerinaRegistry mockerinaRegistry = MockerinaRegistry.getInstance();

            //populate applications without services
            BallerinaFile bFile = getBFileWithoutServices(originalBFile, sourceFilePath);
            mockerinaRegistry.addBallerinaFile(originalBFile, bFile);
            Arrays.stream(originalBFile.getServices())          // save the services list for later use by StartServer
                    .forEachOrdered(mockerinaRegistry::addService);

            // Create a runtime environment for this Ballerina application
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bFile);

            // Get the existing application associated with this ballerina config
            Application app = ApplicationRegistry.getInstance().getApplication(appName);
            if (app == null) {
                // Create a new application with ballerina file name, if there is no app currently exists.
                app = new Application(appName);
                app.setRuntimeEnv(runtimeEnv);
                ApplicationRegistry.getInstance().registerApplication(app);
            }

            Package aPackage = app.getPackage(appName);
            if (aPackage == null) {
                // check if package name is null
//                if (bFile.getPackagePath() != null) {
//                    aPackage = new Package(bFile.getPackagePath());
//                } else {
                aPackage = new Package("default");
//                }
                app.addPackage(aPackage);
            }
            aPackage.addFiles(bFile);

            // Here we need to link all the resources with this application. We execute the matching resource
            // when a request is made. At that point, we need to access runtime environment to execute the resource.
            for (Service service : bFile.getServices()) {
                for (Resource resource : service.getResources()) {
                    resource.setApplication(app);
                }
            }

           ApplicationRegistry.getInstance().updatePackage(aPackage);
            //outStream.println("ballerina: deployed service(s) in " + LauncherUtils.getFileName(serviceFilePath));
        } catch (Throwable e) {
            throw new BallerinaException(e.getMessage());
        }

    }

    private static BallerinaFile getBFileWithoutServices(BallerinaFile originalBFile, Path sourceFilePath) {
        BallerinaFile bFile = ParserUtils.buildLangModel(sourceFilePath);
        bFile.setServices(new Service[0]);
        return bFile;
    }

    private static void execute(BallerinaFile balFile, BallerinaFunction function, List<String> args) {
        Context bContext = new Context();
        try {
            SymbolName argsName;
            NodeLocation funcLocation = function.getNodeLocation();
            ParameterDef[] parameterDefs = function.getParameterDefs();
            argsName = parameterDefs[0].getSymbolName();

            Expression[] exprs = new Expression[1];
            VariableRefExpr variableRefExpr = new VariableRefExpr(funcLocation, argsName);
            variableRefExpr.setVariableDef(parameterDefs[0]);
            StackVarLocation location = new StackVarLocation(0);
            variableRefExpr.setMemoryLocation(location);
            variableRefExpr.setType(BTypes.typeString);
            exprs[0] = variableRefExpr;

            BArray<BString> arrayArgs = new BArray<>(BString.class);
            for (int i = 0; i < args.size(); i++) {
                arrayArgs.add(i, new BString(args.get(i)));
            }
            BValue[] argValues = {arrayArgs};

            // 3) Create a function invocation expression
            FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(funcLocation,
                    function.getName(), null, function.getPackagePath(), exprs);
            funcIExpr.setOffset(1);
            funcIExpr.setCallableUnit(function);

            CallableUnitInfo functionInfo = new CallableUnitInfo(funcIExpr.getName(),
                    funcIExpr.getPackagePath(), funcLocation);

            StackFrame currentStackFrame = new StackFrame(argValues, new BValue[0], functionInfo);
            bContext.getControlStack().pushFrame(currentStackFrame);

            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(balFile);
            BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
            funcIExpr.executeMultiReturn(executor);

            bContext.getControlStack().popFrame();
        } catch (Throwable ex) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(ex);
            String stacktrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            throw new BallerinaException(errorMsg + "\n" + stacktrace);
        }
    }

    public void runMain(Path sourceFilePath) {

        logger.info("sourceFilePath: " + sourceFilePath.toString());
        SymbolScope symScope = GlobalScope.getInstance();
        //FunctionUtils.addNativeFunction(symScope, new PrintlnInt());
        //FunctionUtils.addNativeFunction(symScope, new PrintlnString());
        bFile = ParserUtils.parseBalFile(sourceFilePath.toString(), symScope, true);

        TesterinaFile tFile = new TesterinaFile(getFileNameFromResourcePath(sourceFilePath.toString()),
                sourceFilePath.toString(), bFile);
        ArrayList<TesterinaFunction> testFunctions = tFile.getTestFunctions();
        Iterator<TesterinaFunction> testFuncIter = testFunctions.iterator();
        while (testFuncIter.hasNext()) {
            TesterinaFunction tFunc = testFuncIter.next();
            logger.info("Running '" + tFunc.getName() + "'...");
            try {
                tFunc.invoke();
            } catch (BallerinaException e) {
                logger.info(e.getMessage());
            }
        }

        //BValue[] returns = Functions.invoke(bFile, "testFunction1");
        //log.info("return: " + returns[0]);
    }

    private String getFileNameFromResourcePath(String path) {
        int indexOfLastSeparator = path.lastIndexOf(File.separator);
        String fileName = path.substring(indexOfLastSeparator + 1);
        return fileName;
    }
}
