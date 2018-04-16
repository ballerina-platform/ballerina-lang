/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.natives.test;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.swagger.CodeGenerator;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;
import org.ballerinalang.swagger.utils.GeneratorConstants;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.util.Utils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Native function ballerina.test:startServiceSkeleton.
 * Start a service skeleton from a given Swagger definition.
 *
 * @since 0.97.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "test", functionName = "startServiceSkeleton", args =
        {@Argument(name = "packageName", type = TypeKind
                .STRING), @Argument(name = "swaggerFilePath", type = TypeKind.STRING)}, returnType = {@ReturnType
        (type = TypeKind.BOOLEAN)}, isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value", value = "Start a " +
        "service skeleton from a given Swagger definition in the given ballerina package.")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "packageName", value = "Name of " +
        "the package"), @Attribute(name = "swaggerFilePath", value = "Path to the Swagger definition")})
public class StartServiceSkeleton extends BlockingNativeCallableUnit {

    private static PrintStream errStream = System.err;

    @Override
    public void execute(Context ctx) {
        String packageName = ctx.getStringArgument(0);
        String swaggerFilePath = ctx.getStringArgument(1);

        //TODO : validate for duplicate package in the source which can conflict with mock packages
        String sourceRoot = System.getProperty(TesterinaConstants.BALLERINA_SOURCE_ROOT);
        initTempDir(sourceRoot);
        Path rootDir = Paths.get(sourceRoot, TesterinaConstants.TESTERINA_TEMP_DIR);
        CodeGenerator generator = new CodeGenerator();
        generator.setSrcPackage(packageName);

        try {
            generator.generate(GeneratorConstants.GenType.MOCK, swaggerFilePath, rootDir.toString());

        } catch (IOException | BallerinaOpenApiException e) {
            throw new BallerinaIOException(String.format("Service skeleton creation failed. Failed to generate the "
                    + "service from the [Swagger file] %s [cause] %s", swaggerFilePath, e.getMessage()), e);
        }

        CompileResult compileResult = BCompileUtil.compile(rootDir.toString(), packageName, CompilerPhase.CODE_GEN);

        // print errors
        for (Diagnostic diagnostic : compileResult.getDiagnostics()) {
            errStream.println(diagnostic.getKind() + ": " + diagnostic.getPosition() + " " + diagnostic.getMessage());
        }
        if (compileResult.getErrorCount() > 0) {
            throw new BallerinaException("Service skeleton creation failed. Compilation failed.");
        }
        // set the debugger

        ProgramFile programFile = compileResult.getProgFile();
        programFile.setProgramFilePath(Paths.get(rootDir.toString()));
        // start the service
        Utils.startService(programFile);
        // keep a reference to be used in stop service skeleton
        TesterinaRegistry.getInstance().addSkeletonProgramFile(programFile);
        ctx.setReturnValues(new BBoolean(true));
    }

    private void initTempDir(String sourceRoot) {

        // create the .testerina directory and .ballerina directory
        Path projectRoot = Paths.get(sourceRoot, TesterinaConstants.TESTERINA_TEMP_DIR, ".ballerina");
        if (!Files.exists(projectRoot)) {
            try {
                Files.createDirectories(projectRoot);
            } catch (IOException e) {
                throw new BallerinaIOException(String.format("Service skeleton creation failed. Failed to create " +
                                                             "[.ballerina] %s [cause] %s", projectRoot.toString(), e
                    .getMessage()), e);
            }
        }
    }
}
