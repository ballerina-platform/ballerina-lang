/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.compiler.backend.jvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.bir.BIRModuleUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.model.types.TypeKind.STRING;

/**
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compiler_backend_jvm",
        functionName = "createBIRContext",
        args = {
                @Argument(name = "sourceDir", type = STRING),
                @Argument(name = "pathToCompilerBackend", type = STRING),
                @Argument(name = "libDir", type = STRING)
        }
)
public class CreateBIRContext extends BlockingNativeCallableUnit {

    private static final String EXEC_RESOURCE_FILE_NAME = "compiler_backend_jvm.balx";

    @Override
    public void execute(Context context) {
        String projectDir = context.getStringArgument(0);
        String pathToCompilerBackend = context.getStringArgument(1);
        String libDir = context.getStringArgument(2);

        CompilerContext compilerContext = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        options.put(CompilerOptionName.PROJECT_DIR, projectDir);
        System.setProperty("ballerina.home", libDir);
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());

        SourceDirectoryManager.getInstance(compilerContext); // initialize the source directory

        byte[] resBytes;
        try {
            resBytes = Files.readAllBytes(Paths.get(pathToCompilerBackend).resolve(EXEC_RESOURCE_FILE_NAME));
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load " + EXEC_RESOURCE_FILE_NAME + ": ", e);
        }

        ProgramFile programFile = loadProgramFile(resBytes);
        BMap<String, BValue> birContext = BIRModuleUtils.createBIRContext(programFile,
                PackageLoader.getInstance(compilerContext), Names.getInstance(compilerContext));

        context.setReturnValues(birContext);
    }

    private static ProgramFile loadProgramFile(byte[] resBytes) {
        try (ByteArrayInputStream byteAIS = new ByteArrayInputStream(resBytes)) {
            ProgramFileReader programFileReader = new ProgramFileReader();
            return programFileReader.readProgram(byteAIS);
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
    }
}
