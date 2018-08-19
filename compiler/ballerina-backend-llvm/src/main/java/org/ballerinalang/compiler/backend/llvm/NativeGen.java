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
package org.ballerinalang.compiler.backend.llvm;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.bir.BIREmitter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.writer.BIRBinaryWriter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Contains utility methods for generating native executables for Ballerina programs.
 *
 * @since 0.980.0
 */
public class NativeGen {

    private static PrintStream out = System.out;
    private static final String EXEC_RESOURCE_FILE_NAME = "compiler_backend_llvm.balx";

    public static void genBinaryExecutable(Path projectPath,
                                           String progPath,
                                           String targetPath,
                                           boolean offline,
                                           boolean lockEnabled,
                                           boolean dumpBIR,
                                           boolean dumpLLVMIR) {
        // First compile the given Ballerina program
        BLangPackage bLangPackage = compileProgram(projectPath, progPath, offline, lockEnabled);
        if (bLangPackage.diagCollector.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }

        // TODO Check compilation errors

        // Get the generated BIR
        BIRPackage bir = bLangPackage.symbol.bir;

        // Dump bir to the console if the flag is set
        dumpBIR(dumpBIR, bir);

        // Generate the native object file from the bir
        // TODO get the path of the object file
        genObjectFile(bir, targetPath, dumpLLVMIR);

        // TODO exec gcc or any other alternative.
    }

    private static BLangPackage compileProgram(Path projectPath,
                                               String progPath,
                                               boolean offline,
                                               boolean lockEnabled) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, projectPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));

        Compiler compiler = Compiler.getInstance(context);
        return compiler.build(progPath);
    }

    private static void dumpBIR(boolean dumpBIR, BIRPackage bir) {
        if (!dumpBIR) {
            return;
        }

        BIREmitter birEmitter = new BIREmitter();
        String birText = birEmitter.emit(bir);
        out.println(birText);
    }

    private static void genObjectFile(BIRPackage bir, String targetPath, boolean dumpLLVMIR) {
        // Get the resource URI
        URI resURI = getExecResourceURIFromThisJar();

        // Get resource bytes
        byte[] resBytes = readExecResource(resURI);

        // Load the executable program file
        ProgramFile programFile = loadProgramFile(resBytes);

        // Now prepare function arguments.
        BValue[] args = getFunctionArgs(bir, targetPath, dumpLLVMIR);

        // Generate the object file
        // TODO why do we need to set the debugger
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        BLangFunctions.invokeEntrypointCallable(programFile,
                programFile.getEntryPkgName(), "genObjectFile", args);
    }

    private static URI getExecResourceURIFromThisJar() {
        URL resourceURL = NativeGen.class.getClassLoader()
                .getResource("META-INF/ballerina/" + EXEC_RESOURCE_FILE_NAME);
        if (resourceURL == null) {
            throw new BLangCompilerException("missing embedded executable resource: " + EXEC_RESOURCE_FILE_NAME);
        }

        URI resURI;
        try {
            resURI = resourceURL.toURI();
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
        return resURI;
    }

    private static byte[] readExecResource(URI resURI) {
        byte[] resBytes;
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem ignored = FileSystems.newFileSystem(resURI, env)) {
            resBytes = Files.readAllBytes(Paths.get(resURI));
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
        return resBytes;
    }

    private static ProgramFile loadProgramFile(byte[] resBytes) {
        try (ByteArrayInputStream byteAIS = new ByteArrayInputStream(resBytes)) {
            ProgramFileReader programFileReader = new ProgramFileReader();
            return programFileReader.readProgram(byteAIS);
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
    }

    private static BValue[] getFunctionArgs(BIRPackage bir, String targetPath, boolean dumpLLVMIR) {
        BValue[] args = new BValue[3];
        args[0] = new BByteArray(getBinaryForm(bir));
        args[1] = new BString(targetPath);
        args[2] = new BBoolean(dumpLLVMIR);
        return args;
    }

    private static byte[] getBinaryForm(BIRPackage bir) {
        BIRBinaryWriter binaryWriter = new BIRBinaryWriter();
        return binaryWriter.write(bir);
    }
}
