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
package org.ballerinalang.protobuf.cmd;

import org.ballerinalang.net.grpc.builder.BallerinaFileBuilder;
import org.ballerinalang.net.grpc.exception.CodeBuilderException;
import org.ballerinalang.protobuf.exception.CodeGeneratorException;
import org.ballerinalang.tool.BLauncherCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.TMP_DIRECTORY_PATH;
import static org.ballerinalang.protobuf.BalGenerationConstants.COMPONENT_IDENTIFIER;
import static org.ballerinalang.protobuf.BalGenerationConstants.EMPTY_STRING;
import static org.ballerinalang.protobuf.BalGenerationConstants.META_LOCATION;
import static org.ballerinalang.protobuf.BalGenerationConstants.NEW_LINE_CHARACTER;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTOC_PLUGIN_EXE_PREFIX;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTOC_PLUGIN_EXE_URL_SUFFIX;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTO_SUFFIX;
import static org.ballerinalang.protobuf.BalGenerationConstants.TEMP_API_DIRECTORY;
import static org.ballerinalang.protobuf.BalGenerationConstants.TEMP_COMPILER_DIRECTORY;
import static org.ballerinalang.protobuf.BalGenerationConstants.TEMP_GOOGLE_DIRECTORY;
import static org.ballerinalang.protobuf.BalGenerationConstants.TEMP_PROTOBUF_DIRECTORY;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.delete;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.downloadFile;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.grantPermission;

/**
 * Class to implement "grpc" command for ballerina.
 * Ex: ballerina grpc  --input (proto-file-path)  --output (output-directory-path)
 */
@CommandLine.Command(
        name = "grpc",
        description = "generate Ballerina gRPC client stub for gRPC service for a given gRPC protoc " +
                "definition.")
public class GrpcCmd implements BLauncherCmd {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcCmd.class);
    
    private static final PrintStream outStream = System.out;
    
    private CommandLine parentCmdParser;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"--input"}, description = "Input .proto file")
    private String protoPath;

    @CommandLine.Option(names = {"--mode"},
            description = "Ballerina source [service or client]"
    )
    private String mode = "stub";

    @CommandLine.Option(names = {"--output"},
            description = "Generated Ballerina source files location"
    )
    private String balOutPath;
    
    private String protocExePath;

    @CommandLine.Option(names = {"--protocVersion"}, hidden = true)
    private String protocVersion = "3.9.1";

    @Override
    public void execute() {
        //Help flag check
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        // check input protobuf file path
        if (protoPath == null || !protoPath.toLowerCase(Locale.ENGLISH).endsWith(PROTO_SUFFIX)) {
            String errorMessage = "Invalid proto file path. Please input valid proto file location.";
            outStream.println(errorMessage);
            return;
        }
        if (!Files.isReadable(Paths.get(protoPath))) {
            String errorMessage = "Provided service proto file is not readable. Please input valid proto file " +
                    "location.";
            outStream.println(errorMessage);
            return;
        }

        // download protoc executor.
        try {
            downloadProtocexe();
        } catch (IOException | CodeGeneratorException e) {
            String errorMessage = "Error while generating protoc executable. " + e.getMessage();
            LOG.error("Error while generating protoc executable.", e);
            outStream.println(errorMessage);
            return;
        }
        // read root/dependent file descriptors.
        File descFile = createTempDirectory();
        StringBuilder msg = new StringBuilder();
        LOG.debug("Initializing the ballerina code generation.");
        byte[] root;
        Set<byte[]> dependant;
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            try {
                List<String> protoFiles = readProperties(classLoader);
                for (String file : protoFiles) {
                    exportResource(file, classLoader);
                }
            } catch (Exception e) {
                LOG.error("Error extracting resource file ", e);
                outStream.println("Error while reading proto library files. " + e.getMessage());
                return;
            }
            if (msg.toString().isEmpty()) {
                outStream.println("Successfully extracted library files.");
            } else {
                outStream.println(msg.toString());
                return;
            }
            try {
                root = DescriptorsGenerator.generateRootDescriptor(this.protocExePath, new File(protoPath)
                        .getAbsolutePath(), descFile.getAbsolutePath());
            } catch (CodeGeneratorException e) {
                String errorMessage = "Error occurred when generating proto descriptor. " + e.getMessage();
                LOG.error("Error occurred when generating proto descriptor.", e);
                outStream.println(errorMessage);
                return;
            }
            if (root.length == 0) {
                String errorMsg = "Error occurred when generating proto descriptor.";
                LOG.error(errorMsg);
                outStream.println(errorMsg);
                return;
            }
            LOG.debug("Successfully generated root descriptor.");
            try {
                dependant = DescriptorsGenerator.generateDependentDescriptor(this.protocExePath, new File(protoPath)
                                .getAbsolutePath(), descFile.getAbsolutePath());
            } catch (CodeGeneratorException e) {
                String errorMessage = "Error occurred when generating dependent proto descriptor. " + e.getMessage();
                LOG.error(errorMessage, e);
                outStream.println(errorMessage);
                return;
            }
            LOG.debug("Successfully generated dependent descriptor.");
        } finally {
            //delete temporary meta files
            File tempDir = new File(TMP_DIRECTORY_PATH);
            delete(new File(tempDir, META_LOCATION));
            delete(new File(tempDir, TEMP_GOOGLE_DIRECTORY));
            LOG.debug("Successfully deleted temporary files.");
        }
        // generate ballerina stub based on descriptor values.
        BallerinaFileBuilder ballerinaFileBuilder;
        // If user provides output directory, generate service stub inside output directory.
        if (balOutPath == null) {
            ballerinaFileBuilder = new BallerinaFileBuilder(root, dependant);
        } else {
            ballerinaFileBuilder = new BallerinaFileBuilder(root, dependant, balOutPath);
        }
        try {
            ballerinaFileBuilder.build(this.mode);
        } catch (CodeBuilderException e) {
            LOG.error("Error generating ballerina file.", e);
            msg.append("Error generating ballerina file.").append(e.getMessage()).append(NEW_LINE_CHARACTER);
            outStream.println(msg.toString());
            return;
        }
        msg.append("Successfully generated ballerina file.").append(NEW_LINE_CHARACTER);
        outStream.println(msg.toString());
    }
    
    /**
     * Create meta temp directory which needed for intermediate processing.
     *
     * @return Temporary Created meta file.
     */
    private File createTempDirectory() {
        File parent = new File(TMP_DIRECTORY_PATH);
        File metadataHome = new File(parent, META_LOCATION);
        if (!metadataHome.exists() && !metadataHome.mkdir()) {
            throw new IllegalStateException("Couldn't create dir: " + metadataHome);
        }
        File googleHome = new File(parent, TEMP_GOOGLE_DIRECTORY);
        createTempDirectory(googleHome);
        File protobufHome = new File(googleHome, TEMP_PROTOBUF_DIRECTORY);
        createTempDirectory(protobufHome);
        File compilerHome = new File(protobufHome, TEMP_COMPILER_DIRECTORY);
        createTempDirectory(compilerHome);
        File apiHome = new File(googleHome, TEMP_API_DIRECTORY);
        createTempDirectory(protobufHome);
        createTempDirectory(apiHome);
        return new File(metadataHome, getProtoFileName() + "-descriptor.desc");
    }

    private void createTempDirectory(File dirName) {
        if (!dirName.exists() && !dirName.mkdir()) {
            throw new IllegalStateException("Couldn't create dir: " + dirName);
        }
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/wrapper.proto"
     */
    private static void exportResource(String resourceName, ClassLoader classLoader) throws CodeGeneratorException {
        try (InputStream initialStream = classLoader.getResourceAsStream(resourceName);
             OutputStream resStreamOut = new FileOutputStream(new File(TMP_DIRECTORY_PATH, resourceName))) {
            if (initialStream == null) {
                throw new CodeGeneratorException("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = initialStream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            throw new CodeGeneratorException("Cannot find '" + resourceName + "' resource  at the jar.", e);
        }
    }
    
    /**
     * Download the protoc executor.
     */
    private void downloadProtocexe() throws IOException, CodeGeneratorException {
        if (protocExePath == null) {
            String protocFilename = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
            File protocExeFile = new File(TMP_DIRECTORY_PATH, protocFilename);
            protocExePath = protocExeFile.getAbsolutePath(); // if file already exists will do nothing
            if (!protocExeFile.exists()) {
                outStream.println("Downloading protoc executor file - " + protocFilename);
                String protocDownloadurl = PROTOC_PLUGIN_EXE_URL_SUFFIX + protocVersion + "/protoc-" + protocVersion
                        + "-" + OSDetector.getDetectedClassifier() + PROTOC_PLUGIN_EXE_PREFIX;
                File tempDownloadFile = new File(TMP_DIRECTORY_PATH,
                        "protoc-" + OSDetector.getDetectedClassifier() + ".exe.download");
                try {
                    downloadFile(new URL(protocDownloadurl), tempDownloadFile);
                    Files.move(tempDownloadFile.toPath(), protocExeFile.toPath());
                    //set application user permissions to 455
                    grantPermission(protocExeFile);
                } catch (CodeGeneratorException e) {
                    Files.deleteIfExists(Paths.get(protocExePath));
                    throw e;
                }
                outStream.println("Download successfully completed. Executor file path - " + protocExeFile.getPath());
            } else {
                grantPermission(protocExeFile);
                outStream.println("Continue with existing protoc executor file at " + protocExeFile.getPath());
            }
        } else {
            outStream.println("Continue with provided protoc executor file at " + protocExePath);
        }
    }
    
    
    @Override
    public String getName() {
        return COMPONENT_IDENTIFIER;
    }
    
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generates ballerina gRPC client stub for gRPC service").append(System.lineSeparator());
        out.append("for a given grpc protoc definition").append(System.lineSeparator());
        out.append(System.lineSeparator());
    }
    
    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append("  ballerina " + COMPONENT_IDENTIFIER + " --input chat.proto\n");
    }
    
    private String getProtoFileName() {
        File file = new File(protoPath);
        return file.getName().replace(PROTO_SUFFIX, EMPTY_STRING);
    }
    
    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }
    
    private List<String> readProperties(ClassLoader classLoader) throws CodeGeneratorException {
        String fileName;
        List<String> protoFilesList = new ArrayList<>();
        try (InputStream initialStream = classLoader.getResourceAsStream("standardProtos.properties");
             BufferedReader reader = new BufferedReader(new InputStreamReader(initialStream, StandardCharsets.UTF_8))) {
            while ((fileName = reader.readLine()) != null) {
                protoFilesList.add(fileName);
            }
        } catch (IOException e) {
            throw new CodeGeneratorException("Error while reading property file.", e);
        }
        return protoFilesList;
    }
    
    public void setProtoPath(String protoPath) {
        this.protoPath = protoPath;
    }
    
    public void setBalOutPath(String balOutPath) {
        this.balOutPath = balOutPath;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}


