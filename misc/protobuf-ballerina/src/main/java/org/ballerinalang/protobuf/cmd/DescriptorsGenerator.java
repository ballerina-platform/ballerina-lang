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
import org.ballerinalang.protobuf.exception.BalGenToolException;
import org.ballerinalang.protobuf.utils.ProtocCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.protobuf.BalGenerationConstants.GOOGLE_STANDARD_LIB;
import static org.ballerinalang.protobuf.BalGenerationConstants.META_LOCATION;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.createMetaFolder;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.generateDescriptor;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.getDescriptorPath;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.resolveProtoFloderPath;

/**
 * Class for generate file descriptors for proto files.
 */
public class DescriptorsGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DescriptorsGenerator.class);
    
    public static List<byte[]> generatedependentDescriptor(String parentDescPath, String parentProtoPath,
                                                           List<byte[]> list,
                                                           String exePath, ClassLoader classLoader) {
        File initialFile = new File(parentDescPath);
        try (InputStream targetStream = new FileInputStream(initialFile);) {
            DescriptorProtos.FileDescriptorSet descSet = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
            for (String depPath : descSet.getFile(0).getDependencyList()) {
                String path = BalGenerationConstants.META_DEPENDENCY_LOCATION + depPath.substring(
                        depPath.lastIndexOf(BalGenerationConstants.FILE_SEPARATOR)
                        , depPath.length()).replace(".proto", "") + ".desc";
                createMetaFolder(path);
                String protoPath;
                if (!depPath.contains(GOOGLE_STANDARD_LIB)) {
                    protoPath = new File(new File(resolveProtoFloderPath(parentProtoPath)).toURI().getPath()
                            + depPath).toURI().getPath();
                } else {
                    //Get file from resources folder
                    File dependentDesc = new File(META_LOCATION + depPath);
                    File parentFile = dependentDesc.getParentFile();
                    if (!parentFile.exists() && !parentFile.mkdirs()) {
                        throw new IllegalStateException("Couldn't create directory '" + META_LOCATION + depPath + "'");
                    }
                    try (InputStream initialStream = classLoader.getResourceAsStream(depPath);
                         OutputStream outStream = new FileOutputStream(dependentDesc)) {
                        byte[] buffer = new byte[initialStream.available()];
                        int read = initialStream.read(buffer);
                        if (read == -1) {
                            throw new IllegalStateException("Couldn't read input stream of 'google/protobuf'" +
                                    " resource: ");
                        }
                        outStream.write(buffer);
                        outStream.close();
                        protoPath = dependentDesc.getAbsolutePath();
                    } catch (IOException e) {
                        throw new BalGenToolException("Error reading resource file '" + depPath + "'", e);
                    }
                }
                
                String command = new ProtocCommandBuilder(exePath, protoPath, resolveProtoFloderPath(protoPath)
                        , new File(getDescriptorPath(depPath)).getAbsolutePath()).build();
                generateDescriptor(command);
                File childFile = new File(path);
                try (InputStream childStream = new FileInputStream(childFile)) {
                    DescriptorProtos.FileDescriptorSet childDescSet = DescriptorProtos.FileDescriptorSet
                            .parseFrom(childStream);
                    if (childDescSet.getFile(0).getDependencyCount() != 0) {
                        List<byte[]> newList = new ArrayList<>();
                        generatedependentDescriptor(path, protoPath, newList,
                                exePath, classLoader);
                    } else {
                        initialFile = new File(path);
                        try (InputStream dependentStream = new FileInputStream(initialFile)) {
                            DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet
                                    .parseFrom(dependentStream);
                            list.add(set.getFile(0).toByteArray());
                        } catch (IOException e) {
                            throw new BalGenToolException("Error reading dependent descriptor.", e);
                        }
                    }
                } catch (IOException e) {
                    throw new BalGenToolException("Error extracting dependent bal.", e);
                }
            }
        } catch (IOException e) {
            throw new BalGenToolException("Error parsing descriptor file " + initialFile, e);
        }
        return list;
    }
}
