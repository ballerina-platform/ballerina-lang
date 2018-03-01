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
package org.ballerinalang.grpc.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.grpc.exception.BalGenToolException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class to implement "grpc" command for ballerina.
 * Ex: ballerina grpc  --proto_path (proto-file-path)  --exe_path (protoc-executor-path)
 */
@Parameters(commandNames = "grpc", commandDescription = "DescriptorGenerate connector/service using grpc definition")
public class GRPCCmd implements BLauncherCmd {
    private static final Logger LOG = LoggerFactory.getLogger(BLauncherCmd.class);
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String NEW_LINE_CHARACTER = System.getProperty("line.separator");
    private static final PrintStream outStream = System.err;
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
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            downloadProtocexe();
        } catch (BalGenToolException e) {
            LOG.error("Error while generating protoc executable. ", e);
            throw new BalGenToolException("Error while generating protoc executable. ", e);
        }
        
        String descriptorPath = "desc_gen/" + getProtoFileName() + "-descriptor" + ".desc";
        File descFile = generateDescTempFolder(descriptorPath, classLoader);
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
        byte[] root = new byte[0];
        try {
            root = DescriptorsGenerator.getRootByteArray(this.exePath, this.protoPath, descFile.getAbsolutePath());
        } catch (InterruptedException e) {
            LOG.error("Error generating root descriptor waiting for command execution.", e);
            msg.append("Error generating root descriptor at waiting for command execution.").append(NEW_LINE_CHARACTER);
            outStream.println(msg.toString());
        } catch (IOException e) {
            LOG.error("Error generating root descriptor at process building.", e);
            msg.append("Error generating root descriptor at process building.").append(NEW_LINE_CHARACTER);
            outStream.println(msg.toString());
        }
        msg.append("Successfully generated root descriptor.").append(NEW_LINE_CHARACTER);
        List<byte[]> dependant = null;
        try {
            dependant = DescriptorsGenerator.dependentDescriptorGenerator(descriptorPath, this.protoPath,
                    new ArrayList<>(), descriptorPath.substring
                            (0, descriptorPath.lastIndexOf('/')) + "/dependencies/", exePath, classLoader);
            msg.append("Successfully generated dependent descriptor.").append(NEW_LINE_CHARACTER);
        } catch (IOException e) {
            LOG.error("Error generating dependent descriptors.", e);
            msg.append("Error generating dependent descriptors.").append(NEW_LINE_CHARACTER);
            outStream.println(msg.toString());
        }
        Path balPath = Paths.get(balOutPath);
        try {
            new BallerinaFile(root, dependant, balPath).build();
        } catch (BalGenerationException e) {
            LOG.error("Error generating ballerina file.", e);
            msg.append("Error generating ballerina file.").append(NEW_LINE_CHARACTER);
            outStream.println(msg.toString());
        }
        msg.append("Successfully generated ballerina file.").append(NEW_LINE_CHARACTER);
        delete(new File("desc_gen"));
        delete(new File("google"));
        msg.append("Successfully deleted temporary files.").append(NEW_LINE_CHARACTER);
        outStream.println(msg.toString());
    }
    
    private File generateDescTempFolder(String descriptorPath, ClassLoader classLoader) {
        File descFile = new File(descriptorPath);
        String path = descFile.getAbsolutePath().substring(0, descFile.getAbsolutePath().lastIndexOf('/'));
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
        
        File targetFile = new File("google/protobuf/compiler/plugin.proto");
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
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     * @throws Exception
     */
    private static String exportResource(String resourceName, ClassLoader classLoader) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        try {
            InputStream initialStream = classLoader.getResourceAsStream(resourceName);
            if (initialStream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }
            stream = initialStream;
            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = new File(GRPCCmd.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())
                    .getParentFile().getPath().replace('\\', '/');
            resStreamOut = new FileOutputStream(resourceName);
            
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } finally {
            assert stream != null;
            stream.close();
            assert resStreamOut != null;
            resStreamOut.close();
        }
        return jarFolder + resourceName;
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
                String url = "http://repo1.maven.org/maven2/com/google/protobuf/protoc/3.4.0/" +
                        "protoc-" + protocVerstion + "-" + OSDetector.getDetectedClassifier() + ".exe";
                try {
                    URL url2 = new URL(url);
                    saveFile(url2, exePath);
                    File file = new File(exePath);
                    //set application user permissions to 455
                    boolean isExecutable = file.setExecutable(true);
                    boolean isReadable = file.setReadable(true);
                    boolean isWritable = file.setWritable(true);
                    if (isExecutable && isReadable && isWritable) {
                        LOG.debug("Successfully grated permission for new protoc exe file" + exePath);
                    }
                } catch (IOException e) {
                    throw new BalGenToolException("Exception occurred while writing protoc executable to file. ", e);
                }
            } else {
                boolean isExecutable = exeFile.setExecutable(true);
                boolean isReadable = exeFile.setReadable(true);
                boolean isWritable = exeFile.setWritable(true);
                if (isExecutable && isReadable && isWritable) {
                    LOG.debug("Successfully grated permission for new protoc exe file" + exePath);
                }
                
            }
        }
        outStream.println("Download successfully completed!");
    }
    
    private static void delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                if (Objects.requireNonNull(file.list()).length == 0) {
                    boolean isDelete = file.delete();
                    if (isDelete) {
                        LOG.debug("Successfully deleted file " + file.toString());
                    }
                } else {
                    String files[] = file.list();
                    if (files != null) {
                        for (String temp : files) {
                            File fileDelete = new File(file, temp);
                            delete(fileDelete);
                        }
                    }
                    if (Objects.requireNonNull(file.list()).length == 0) {
                        boolean isDelete = file.delete();
                        if (isDelete) {
                            LOG.debug("Successfully deleted file " + file.toString());
                        }
                    }
                }
            } else {
                boolean isDelete = file.delete();
                if (isDelete) {
                    LOG.debug("Successfully deleted file " + file.toString());
                }
            }
        }
    }
    
    private static void saveFile(URL url, String file) throws IOException {
        
        InputStream in = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File(file));
        
        int length;
        byte[] buffer = new byte[1024]; // buffer for portion of data from
        while ((length = in.read(buffer)) > -1) {
            fos.write(buffer, 0, length);
        }
        fos.close();
        in.close();
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
        InputStream initialStream;
        String fileName = null;
        List<String> protoFilesList = new ArrayList<>();
        
        initialStream = classLoader.getResourceAsStream("standardProtos.properties");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(initialStream));
            while ((fileName = reader.readLine()) != null) {
                protoFilesList.add(fileName);
            }
        } catch (IOException e) {
            LOG.error("Error reading resource " + fileName, e);
        } finally {
            try {
                initialStream.close();
            } catch (Throwable e) {
                LOG.error("Error closing resource " + fileName, e);
            }
        }
        return protoFilesList;
    }
    
    public void setProtoPath(String protoPath) {
        this.protoPath = protoPath;
    }
    
    public void setBalOutPath(String balOutPath) {
        this.balOutPath = balOutPath;
    }
}


