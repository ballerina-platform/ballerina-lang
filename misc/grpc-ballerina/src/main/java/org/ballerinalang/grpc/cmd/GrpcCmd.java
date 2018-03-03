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
package org.ballerinalang.net.grpc.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.net.grpc.exception.BalGenToolException;
import org.ballerinalang.net.grpc.utils.BalFileGenerationUtils;
import org.ballerinalang.net.grpc.utils.BalGenerationConstants;
import org.ballerinalang.launcher.BLauncherCmd;
import org.ballerinalang.net.grpc.builder.BallerinaFile;
import org.ballerinalang.net.grpc.exception.BalGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.grpc.utils.BalFileGenerationUtils.delete;
import static org.ballerinalang.net.grpc.utils.BalFileGenerationUtils.grantPermission;
import static org.ballerinalang.net.grpc.utils.BalFileGenerationUtils.saveFile;
import static org.ballerinalang.net.grpc.utils.BalGenerationConstants.FILE_SEPARATOR;
import static org.ballerinalang.net.grpc.utils.BalGenerationConstants.NEW_LINE_CHARACTER;
import static org.ballerinalang.net.grpc.utils.BalGenerationConstants.PLUGIN_PROTO_FILEPATH;

/**
 * Class to implement "grpc" command for ballerina.
 * Ex: ballerina grpc  --proto_path (proto-file-path)  --exe_path (protoc-executor-path)
 */
@Parameters(commandNames = "grpc", commandDescription = "DescriptorGenerate connector/service using grpc definition")
public class GrpcCmd implements BLauncherCmd {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcCmd.class);
    
    private static final PrintStream outStream = System.out;
    private JCommander parentCmdParser;
    
    @Parameter(names = {"--proto_path"},
            description = "Path of the .proto file",
            required = true
    )
    private String protoPath;
    
    @Parameter(names = {"--bal_out"},
            description = "output location of .bal file"
    )
    private String balOutPath = "";
    
    @Parameter(names = {"--exe_path"},
            description = "Full path of the .exe file"
    )
    private String exePath;
    
    @Parameter(names = {"--protoc_version"},
            description = "Full path of the .exe file"
    )
    private String protocVerstion = "3.4.0";
    
    @Parameter(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;
    
    @Parameter(names = "--debug", hidden = true)
    private String debugPort;
    
    @Parameter(names = "--java.debug", hidden = true, description = "remote java debugging port")
    private String javaDebugPort;
    
    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "build");
            outStream.println(commandUsageInfo);
            return;
        }
        try {
            downloadProtocexe();
        } catch (BalGenToolException e) {
            LOG.error("Error while generating protoc executable. ", e);
            throw new BalGenToolException("Error while generating protoc executable. ", e);
        }
        ClassLoader classLoader = this.getClass().getClassLoader();
        String descriptorPath = BalGenerationConstants.META_LOCATION + getProtoFileName() + "-descriptor.desc";
        File descFile = generateDescTempFolder(descriptorPath);
        StringBuilder msg = new StringBuilder("Initializing the ballerina code generation." + NEW_LINE_CHARACTER);
        List<String> protoFiles = readProperties(classLoader);
        for (String file : protoFiles) {
            try {
                exportResource(file, classLoader);
            } catch (Exception e) {
                msg.append("Error extracting resource file ").append(file).append(NEW_LINE_CHARACTER);
                outStream.println(msg.toString());
                LOG.error("Error exacting resource file " + file, e);
            }
        }
        msg.append("Successfully generated initial files.").append(NEW_LINE_CHARACTER);
        byte[] root = BalFileGenerationUtils.getProtoByteArray(this.exePath, this.protoPath, descFile
                .getAbsolutePath());
        msg.append("Successfully generated root descriptor.").append(NEW_LINE_CHARACTER);
        List<byte[]> dependant;
        dependant = DescriptorsGenerator.dependentDescriptorGenerator(descriptorPath, this.protoPath,
                new ArrayList<>(), exePath, classLoader);
        msg.append("Successfully generated dependent descriptor.").append(NEW_LINE_CHARACTER);
        Path balPath = Paths.get(balOutPath);
        try {
            new BallerinaFile(root, dependant, balPath).build();
        } catch (BalGenerationException e) {
            LOG.error("Error generating ballerina file.", e);
            msg.append("Error generating ballerina file.").append(NEW_LINE_CHARACTER);
            outStream.println(msg.toString());
        }
        msg.append("Successfully generated ballerina file.").append(NEW_LINE_CHARACTER);
        //delete temporary meta files
        delete(new File("desc_gen"));
        delete(new File("google"));
        msg.append("Successfully deleted temporary files.").append(NEW_LINE_CHARACTER);
        outStream.println(msg.toString());
    }
    
    /**
     * Generate the meta folder which needed for intermediate processing.
     *
     * @param descriptorPath proto descriptor path
     * @return Temporary Created meta file.
     */
    private File generateDescTempFolder(String descriptorPath) {
        File descFile = new File(descriptorPath);
        String path = descFile.getAbsolutePath().substring(0, descFile.getAbsolutePath()
                .lastIndexOf(BalGenerationConstants.FILE_SEPARATOR));
        File folderPath = new File(path);
        try {
            if (!folderPath.exists() && !folderPath.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + descFile);
            }
            descriptorPath = descFile.getAbsolutePath();
            byte data[] = new byte[0];
            Path file = Paths.get(descriptorPath);
            Files.write(file, data);
        } catch (IOException e) {
            throw new BalGenToolException("Error creating " + descriptorPath + " file.", e);
        }
        
        File targetFile = new File(PLUGIN_PROTO_FILEPATH);
        File parent1 = targetFile.getParentFile();
        File parent2 = targetFile.getParentFile().getParentFile();
        File parent3 = targetFile.getParentFile().getParentFile().getParentFile();
        if (!parent1.exists() && !parent1.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent1);
        }
        if (!parent2.exists() && !parent2.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent2);
        }
        if (!parent3.exists() && !parent3.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent3);
        }
        return descFile;
    }
    
    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/wrapper.proto"
     */
    private static void exportResource(String resourceName, ClassLoader classLoader) {
        try (InputStream initialStream = classLoader.getResourceAsStream(resourceName);
             OutputStream resStreamOut = new FileOutputStream(resourceName)) {
            if (initialStream == null) {
                throw new BalGenToolException("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = initialStream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            throw new BalGenToolException("Cannot find '" + resourceName + "' resource  at the jar.", e);
        }
    }
    
    /**
     * Download the protoc executor.
     */
    private void downloadProtocexe() {
        outStream.println("Downloading proc executor ...");
        if (exePath == null) {
            exePath = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
            File exeFile = new File(exePath);
            exePath = exeFile.getAbsolutePath(); // if file already exists will do nothing
            if (!exeFile.isFile()) {
                try {
                    boolean newFile = exeFile.createNewFile();
                    if (newFile) {
                        LOG.debug("Successfully created new protoc exe file" + exePath);
                    }
                } catch (IOException e) {
                    throw new BalGenToolException("Exception occurred while creating new file for protoc exe. ", e);
                }
                String url = "http://repo1.maven.org/maven2/com/google/protobuf/protoc/" + protocVerstion + "/" +
                        "protoc-" + protocVerstion + "-" + OSDetector.getDetectedClassifier() + ".exe";
                try {
                    saveFile(new URL(url), exePath);
                    File file = new File(exePath);
                    //set application user permissions to 455
                    grantPermission(file);
                } catch (IOException e) {
                    throw new BalGenToolException("Exception occurred while writing protoc executable to file. ", e);
                }
                outStream.println("Download successfully completed!");
            } else {
                grantPermission(exeFile);
                outStream.println("Continue with existing protoc executor.");
            }
        }
    }
    
    
    @Override
    public String getName() {
        return "grpc";
    }
    
    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generates ballerina grRPC client stub for gRPC service").append(System.lineSeparator());
        out.append("for a given grpc protoc definition").append(System.lineSeparator());
        out.append(System.lineSeparator());
    }
    
    @Override
    public void printUsage(StringBuilder stringBuilder) {
        
        stringBuilder.append("  ballerina grpc --proto_path <<proto-file-path>>  --exe_path " +
                "<<protoc-executor-path>> \n");
    }
    
    private String getProtoFileName() {
        String[] arr = protoPath.split(FILE_SEPARATOR);
        return arr[arr.length - 1].replace(".proto", "");
    }
    
    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        this.parentCmdParser = parentCmdParser;
    }
    
    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    
    }
    
    private List<String> readProperties(ClassLoader classLoader) {
        String fileName;
        List<String> protoFilesList = new ArrayList<>();
        try (InputStream initialStream = classLoader.getResourceAsStream("standardProtos.properties");
             BufferedReader reader = new BufferedReader(new InputStreamReader(initialStream, StandardCharsets.UTF_8))) {
            while ((fileName = reader.readLine()) != null) {
                protoFilesList.add(fileName);
            }
        } catch (IOException e) {
            throw new BalGenToolException("Error in reading standardProtos.properties.", e);
        }
        return protoFilesList;
    }
    
    public void setProtoPath(String protoPath) {
        this.protoPath = protoPath;
    }
    
    public void setBalOutPath(String balOutPath) {
        this.balOutPath = balOutPath;
    }
    
    public void setExePath(String exePath) {
        this.exePath = exePath;
    }
    
    public void setProtocVerstion(String protocVerstion) {
        this.protocVerstion = protocVerstion;
    }
}


