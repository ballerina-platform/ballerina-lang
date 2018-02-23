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
package org.wso2.ballerinalang.grpc.tool.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ballerinalang.launcher.BLauncherCmd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class to implement "grpc" command for ballerina.
 * Ex: ballerina grpc  (grpcFile) -p(package name) -d(output directory name)
 */
//ballerina grpc --proto_path=src/main/proto/helloWorld.proto --descriptor_set_out=out
@Parameters(commandNames = "grpc", commandDescription = "DescriptorGenerate connector/service using grpc definition")
public class GRPCCmd implements BLauncherCmd {
    private static final String FILE_SEPERATOR = System.getProperty("file.separator");
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
    private String balOutPath;
    
    @Parameter(names = {"--exe_path"},
            description = "Full path of the .exe file"
    )
    private String exePath;
    
    @Parameter(names = {"--descriptor_set_out"},
            description = "Output file location of descriptor file"
    )
    private String descriptorPath;
    
    @Parameter(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;
    
    @Parameter(names = "--debug", hidden = true)
    private String debugPort;
    
    @Parameter(names = "--java.debug", hidden = true, description = "remote java debugging port")
    private String javaDebugPort;
    
    @Override
    public void execute() {
        
        if (exePath == null) {
            exePath = "protoc-" + OSDetector.getDetectedClassifier() + ".exe";
            File yourFile = new File(exePath);
            exePath = yourFile.getAbsolutePath(); // if file already exists will do nothing
            if (!yourFile.isFile()) {
                try {
                    yourFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Error: ", e);
                }
                // TODO: 2/13/18 check this condition
                String url = "http://repo1.maven.org/maven2/com/google/protobuf/protoc/3.4.0/" +
                        "protoc-3.4.0-" + OSDetector.getDetectedClassifier() + ".exe";
                try {
                    URL url2 = new URL(url);
                    saveFile(url2, exePath);
                    File file = new File(exePath);
                    //set application user permissions to 455
                    file.setExecutable(true);
                    file.setReadable(true);
                    file.setWritable(true);
                } catch (IOException e) {
                    throw new RuntimeException("Error: ", e);
                }
            }
        } else {
            //assume user has downloaded correct exe and has given the path
            // TODO: 2/12/18 check file permission
        }
        // TODO: 2/12/18 assign default to descriptor file
        if (descriptorPath == null) {
            descriptorPath = "desc_gen/" + getProtoFileName() + "-descriptor" + ".desc";
        } else if (!descriptorPath.contains(".desc")) {
            //log error
        }
        File yourFile = new File(descriptorPath);
        String path = yourFile.getAbsolutePath().substring(0, yourFile.getAbsolutePath().lastIndexOf('/'));
        File folderPath = new File(path);
        try {
            if (!folderPath.exists() && !folderPath.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + yourFile);
            }
            descriptorPath = yourFile.getAbsolutePath();
            byte data[] = new byte[0];
            Path file = Paths.get(descriptorPath);
            Files.write(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Error: ", e);
        }
        if (balOutPath == null) {
            balOutPath = getProtoFileName() + "-client-stub.bal";
        }
        yourFile = new File(balOutPath);
        try {
            yourFile.createNewFile(); // if file already exists will do nothing
            balOutPath = yourFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Error: ", e);
        }
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(parentCmdParser, "build");
            outStream.println(commandUsageInfo);
            return;
        }
        StringBuilder msg = new StringBuilder("successfully generated descriptor ");
        ClassLoader classLoader = this.getClass().getClassLoader();
        byte[] root = DescriptorsGenerator.getRootByteArray(this.exePath, this.protoPath, this.descriptorPath);
        List<byte[]> dependant = DescriptorsGenerator.getDependentByteArray(this.exePath, this.descriptorPath,
                this.protoPath, descriptorPath.substring(0, descriptorPath.lastIndexOf('/'))
                        + "/dependencies/", classLoader);
        new BalGenerate().generate(root, dependant, balOutPath);
        File directory = new File("desc_gen");
        if (!directory.exists()) {
            //
        } else {
            try {
                delete(directory);
            } catch (IOException e) {
                throw new RuntimeException("Error: ", e);
            }
        }
        outStream.println(msg.toString());
    }
    
    private static void delete(File file)
            throws IOException {
        if (file.isDirectory()) {
            //directory is empty, then delete it
            if (file.list().length == 0) {
                file.delete();
            } else {
                //list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);
                    //recursive delete
                    delete(fileDelete);
                }
                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }
    
    private static void saveFile(URL url, String file) throws IOException {
        
        InputStream in = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File(file));
        
        int length = -1;
        byte[] buffer = new byte[1024]; // buffer for portion of data from
        // connection
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
        
        out.append("Generates ballerina connector, service skeleton and mock service" + System.lineSeparator());
        out.append("for a given grpc definition" + System.lineSeparator());
        out.append(System.lineSeparator());
    }
    
    @Override
    public void printUsage(StringBuilder stringBuilder) {
        
        stringBuilder.append("  ballerina grpc <connector | skeleton | mock> <grpcFile> -p<package name> " +
                "-d<output directory name>\n");
    }
    
    private String getProtoFileName() {
        // TODO: 2/13/18
        String[] arr = protoPath.split(FILE_SEPERATOR);
        return arr[arr.length - 1].replace(".proto", "");
    }
    
    @Override
    public void setParentCmdParser(JCommander parentCmdParser) {
        
        this.parentCmdParser = parentCmdParser;
    }
    
    @Override
    public void setSelfCmdParser(JCommander selfCmdParser) {
    
    }
}
