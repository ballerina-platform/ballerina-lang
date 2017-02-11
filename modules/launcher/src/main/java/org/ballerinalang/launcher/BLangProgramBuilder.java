/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.launcher;

import org.wso2.ballerina.core.ProgramPackageRepository;
import org.wso2.ballerina.core.SystemPackageRepository;
import org.wso2.ballerina.core.UserPackageRepository;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.PackageRepository;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @since 0.8.0
 */
public class BLangProgramBuilder {
    public static BallerinaFile build(Path sourceFilePath) {
        // Get the global scope
        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BuiltInNativeConstructLoader.loadConstructs(globalScope);

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope);

        // Create a package scope for each and every package
        // First create a package scope for the given package
        BLangPackage bLangPackage = new BLangPackage(bLangProgram);


        return LauncherUtils.buildLangModel(sourceFilePath);
    }

    public static void main(String[] args) {
        // TODO WIP Code. We will change this soon
        System.setProperty("ballerina.home", "");

        // Get the global scope
        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BuiltInNativeConstructLoader.loadConstructs(globalScope);

        Path progDirPath = Paths.get("");
        Path packagePath = Paths.get("");

        PackageRepository packageRepository = initPackageRepositories(progDirPath);

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, progDirPath);
        BLangPackage mainPackage = BLangPackageLoader.load(bLangProgram, progDirPath, packagePath);
        bLangProgram.setMainPackage(mainPackage);


//        bLangProgram.define(new SymbolName(mainPackage.getPackagePath()), mainPackage);

//        // Remove redundant stuff using the Paths and Files API
//        BLangPackageLoader packageBuilder = new BLangPackageLoader(bLangProgram, basePath, packagePath);
//        BLangPackage mainPackage = packageBuilder.build();
//
//        // Define main package
//
//        resolveDependencies(mainPackage, bLangProgram);


        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
        bLangProgram.accept(semanticAnalyzer);
    }

    public static PackageRepository initPackageRepositories(Path programDirPath) {
        // TODO Refactor this method
        // 1) Create System repository
        String ballerinaHome = System.getProperty("ballerina.home");
        if (ballerinaHome == null || ballerinaHome.isEmpty()) {
            throw new IllegalStateException("ballerina.home is not set");
        }

        Path systemRepoPath = Paths.get(ballerinaHome);
        if (Files.notExists(systemRepoPath)) {
            throw new IllegalStateException("ballerina installation directory does not exists");
        }

        SystemPackageRepository systemPkgRepo = new SystemPackageRepository(systemRepoPath);

        // 2) Create user repository
        Path userPkgRepoPath;
        String ballerinaRepoProp = System.getenv("BALLERINA_REPOSITORY");
        if (ballerinaRepoProp == null || ballerinaRepoProp.isEmpty()) {
            String userHome = System.getProperty("user.home");
            userPkgRepoPath = Paths.get(userHome, ".ballerina");
            if (Files.notExists(userPkgRepoPath)) {
                try {
                    Files.createDirectory(userPkgRepoPath);
                } catch (IOException e) {
                    throw new IllegalStateException("failed to create user repository at '" +
                            userPkgRepoPath.toString() + "'");
                }
            }
        }  else {
            userPkgRepoPath = Paths.get(ballerinaRepoProp);
        }

        UserPackageRepository userPkgRepo = new UserPackageRepository(userPkgRepoPath, systemPkgRepo);
        return new ProgramPackageRepository(programDirPath, systemPkgRepo, userPkgRepo);
    }

    public static BLangProgram buildFile() {
        return null;
    }

    public static BLangProgram buildPackage() {
        return null;
    }

    public static void execute(BallerinaFile balFile, List<String> args) {
        Context bContext = new Context();
        try {
            SymbolName argsName;
            BallerinaFunction mainFun = (BallerinaFunction) balFile.getMainFunction();
            NodeLocation mainFuncLocation = mainFun.getNodeLocation();
            ParameterDef[] parameterDefs = mainFun.getParameterDefs();
            argsName = parameterDefs[0].getSymbolName();

            Expression[] exprs = new Expression[1];
            VariableRefExpr variableRefExpr = new VariableRefExpr(mainFuncLocation, argsName);
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
            FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(mainFuncLocation,
                    mainFun.getName(), null, mainFun.getPackagePath(), exprs);
            funcIExpr.setOffset(1);
            funcIExpr.setCallableUnit(mainFun);

            CallableUnitInfo functionInfo = new CallableUnitInfo(funcIExpr.getName(),
                    funcIExpr.getPackagePath(), mainFuncLocation);

            StackFrame currentStackFrame = new StackFrame(argValues, new BValue[0], functionInfo);
            bContext.getControlStack().pushFrame(currentStackFrame);

            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(balFile);
            BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
            funcIExpr.executeMultiReturn(executor);

            bContext.getControlStack().popFrame();
        } catch (Throwable ex) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(ex);
            ex.printStackTrace();
            String stacktrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            throw new BallerinaException(errorMsg + "\n" + stacktrace);
        }
    }
}
