/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.protobuf.cmd;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.protobuf.BalGenerationConstants;
import org.ballerinalang.protobuf.exception.CodeGeneratorException;
import org.ballerinalang.protobuf.utils.ProtocCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.TMP_DIRECTORY_PATH;
import static org.ballerinalang.protobuf.BalGenerationConstants.DESC_SUFFIX;
import static org.ballerinalang.protobuf.BalGenerationConstants.GOOGLE_STANDARD_LIB_API;
import static org.ballerinalang.protobuf.BalGenerationConstants.GOOGLE_STANDARD_LIB_PROTOBUF;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTO_SUFFIX;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.generateDescriptor;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.isWindows;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.resolveProtoFolderPath;

/**
 * Class for generate file descriptors for proto files.
 */
public class DescriptorsGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DescriptorsGenerator.class);
    
    static Set<byte[]> generateDependentDescriptor(String exePath, String rootProtoPath, String
            rootDescriptorPath) throws CodeGeneratorException {
        Set<byte[]> dependentDescSet = new HashSet<>();
        File tempDir = new File(TMP_DIRECTORY_PATH);
        File initialFile = new File(rootDescriptorPath);
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            DescriptorProtos.FileDescriptorSet descSet = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
            for (String dependentFilePath : descSet.getFile(0).getDependencyList()) {
                if (isWindows()) {
                    dependentFilePath = dependentFilePath.replaceAll("/", "\\\\");
                }
                Path protoFilePath = Paths.get(dependentFilePath).getFileName();
                if (protoFilePath == null) {
                    throw new CodeGeneratorException("Error occurred while reading proto descriptor. Dependent " +
                            "filepath is not defined properly. Filepath: " + dependentFilePath);
                }
                String protoFilename = protoFilePath.toString();
                String descFilename = protoFilename.endsWith(PROTO_SUFFIX) ? protoFilename.replace(PROTO_SUFFIX,
                        DESC_SUFFIX) : null;
                if (descFilename == null) {
                    throw new CodeGeneratorException("Error occurred while reading proto descriptor. Dependent " +
                            "filepath is not defined properly. Filepath: " + dependentFilePath);
                }
                // desc file path: desc_gen/dependencies + <filename>.desc
                String relativeDescFilepath = BalGenerationConstants.META_DEPENDENCY_LOCATION + descFilename;

                File dependentDescFile = new File(tempDir, relativeDescFilepath);
                boolean isDirectoryCreated = dependentDescFile.getParentFile().mkdirs();
                if (!isDirectoryCreated) {
                    LOG.debug("Parent directories didn't create for the file '" + relativeDescFilepath);
                }
                //Derive proto file path of the dependent library.
                String protoPath;
                String protoFolderPath;
                if (!dependentFilePath.contains(GOOGLE_STANDARD_LIB_PROTOBUF) &&
                        !dependentFilePath.contains(GOOGLE_STANDARD_LIB_API)) {
                    protoFolderPath = resolveProtoFolderPath(rootProtoPath);
                    protoPath = new File(protoFolderPath, dependentFilePath).getAbsolutePath();
                } else {
                    protoPath = new File(tempDir, dependentFilePath).getAbsolutePath();
                    protoFolderPath = tempDir.getAbsolutePath();
                }
                
                String command = new ProtocCommandBuilder(exePath, protoPath, protoFolderPath, dependentDescFile
                        .getAbsolutePath()).build();
                generateDescriptor(command);
                File childFile = new File(tempDir, relativeDescFilepath);
                try (InputStream childStream = new FileInputStream(childFile)) {
                    DescriptorProtos.FileDescriptorSet childDescSet = DescriptorProtos.FileDescriptorSet
                            .parseFrom(childStream);
                    if (childDescSet.getFile(0).getDependencyCount() != 0) {
                        Set<byte[]> childList = generateDependentDescriptor(exePath, rootProtoPath, childFile
                                .getAbsolutePath());
                        dependentDescSet.addAll(childList);
                    }
                    byte[] dependentDesc = childDescSet.getFile(0).toByteArray();
                    if (dependentDesc.length == 0) {
                        throw new CodeGeneratorException("Error occurred at generating dependent proto " +
                                "descriptor for dependent proto '" + relativeDescFilepath + "'.");
                    }
                    dependentDescSet.add(dependentDesc);
                } catch (IOException e) {
                    throw new CodeGeneratorException("Error extracting dependent bal.", e);
                }
            }
        } catch (IOException e) {
            throw new CodeGeneratorException("Error parsing descriptor file " + initialFile, e);
        }
        return dependentDescSet;
    }

    /**
     * Generate proto file and convert it to byte array.
     *
     * @param exePath        protoc executor path
     * @param protoPath      .proto file path
     * @param descriptorPath file descriptor path.
     * @return byte array of generated proto file.
     */
    static byte[] generateRootDescriptor(String exePath, String protoPath, String descriptorPath)
            throws CodeGeneratorException {
        String command = new ProtocCommandBuilder
                (exePath, protoPath, resolveProtoFolderPath(protoPath), descriptorPath).build();
        generateDescriptor(command);
        File initialFile = new File(descriptorPath);
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
            if (set.getFileList().size() > 0) {
                return set.getFile(0).toByteArray();
            }
        } catch (IOException e) {
            throw new CodeGeneratorException("Error reading generated descriptor file '" + descriptorPath + "'.", e);
        }
        return new byte[0];
    }
}
