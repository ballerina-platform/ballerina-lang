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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final String TMP_OBJECT_FILE_NAME = "ballerina_native_objf.o";


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

        // Get the generated BIR
        BIRPackage bir = bLangPackage.symbol.bir;

        // Dump bir to the console if the flag is set
        dumpBIR(dumpBIR, bir);

        // Generate the native object file from the bir
        Path objectFilePath = genObjectFile(bir, dumpLLVMIR);

        // Figure out the executable file name
        String execFilename = resolveTargetPath(progPath, targetPath);

        // Generate the native executable file
        genExecutable(objectFilePath, execFilename);
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

    private static Path genObjectFile(BIRPackage bir, boolean dumpLLVMIR) {
        // Get the resource URI
        URI resURI = getExecResourceURIFromThisJar();

        // Get resource bytes
        byte[] resBytes = readExecResource(resURI);

        // Load the executable program file
        ProgramFile programFile = loadProgramFile(resBytes);

        // Get the path of the object file
        Path osTempDirPath = Paths.get(System.getProperty("java.io.tmpdir"));
        Path objectFilePath = osTempDirPath.resolve(TMP_OBJECT_FILE_NAME);

        // Now prepare function arguments.
        BValue[] args = getFunctionArgs(bir, objectFilePath.toString(), dumpLLVMIR);

        // Generate the object file
        try {
            // TODO why do we need to set the debugger
            Debugger debugger = new Debugger(programFile);
            programFile.setDebugger(debugger);
            BLangFunctions.invokeEntrypointCallable(programFile,
                    programFile.getEntryPkgName(), "genObjectFile", args);
        } catch (Exception e) {
            throw new BLangCompilerException("object file generation failed: " + e.getMessage(), e);
        }

        return objectFilePath;
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
        BIRBinaryWriter binaryWriter = new BIRBinaryWriter(bir);
        return binaryWriter.serialize();
    }

    private static void genExecutable(Path objectFilePath, String execFilename) {
        // Check whether gcc is installed
        checkGCCAvailability();

        // Create the os-specific gcc command
        ProcessBuilder gccProcessBuilder = createOSSpecificGCCCommand(objectFilePath, execFilename);

        try {
            // Execute gcc
            Process gccProcess = gccProcessBuilder.start();
            int exitCode = gccProcess.waitFor();
            if (exitCode != 0) {
                throw new BLangCompilerException("linker failed: " + getProcessErrorOutput(gccProcess));
            }
        } catch (IOException | InterruptedException e) {
            throw new BLangCompilerException("linker failed: " + e.getMessage(), e);
        }
    }

    private static String getProcessErrorOutput(Process process) throws IOException {
        //TODO: check win https://stackoverflow.com/questions/8398277/which-encoding-does-process-getinputstream-use
        InputStreamReader in = new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8);
        try (BufferedReader errReader = new BufferedReader(in)) {
            return errReader.lines().collect(Collectors.joining(", "));
        }
    }

    private static ProcessBuilder createOSSpecificGCCCommand(Path objectFilePath, String execFilename) {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        ProcessBuilder gccProcessBuilder = new ProcessBuilder();
        if (osName.startsWith("windows")) {
            // TODO Window environment
            gccProcessBuilder.command("cmd.exe", "/c", "dir");
        } else if (osName.startsWith("mac os x")) {
            // Mac OS X environment
            gccProcessBuilder.command("gcc", objectFilePath.toString(), "-o", execFilename);
        } else {
            // TODO Is this assumption correct?
            // Linux environment
            gccProcessBuilder.command("gcc", objectFilePath.toString(), "-static", "-o", execFilename);
        }
        return gccProcessBuilder;
    }

    private static void checkGCCAvailability() {
        Runtime rt = Runtime.getRuntime();
        try {
            Process gccCheckProc = rt.exec("gcc -v");
            int exitVal = gccCheckProc.waitFor();
            if (exitVal != 0) {
                throw new BLangCompilerException("'gcc' is not installed in your environment");
            }
        } catch (IOException | InterruptedException e) {
            throw new BLangCompilerException("probably, 'gcc' is not installed in your environment: " +
                    e.getMessage(), e);
        }
    }

    private static String resolveTargetPath(String progName, String targetFilename) {
        // Ballerina program name is used as the output filename if it is not given by the user.
        if (targetFilename != null && !targetFilename.isEmpty()) {
            return targetFilename;
        }

        String target = progName;
        int srcExtIndex = target.lastIndexOf(ProjectDirConstants.BLANG_SOURCE_EXT);
        if (srcExtIndex != -1) {
            target = target.substring(0, srcExtIndex);
        }

        return target;
    }
}
