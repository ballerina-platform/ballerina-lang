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
package org.ballerinalang.protobuf.utils;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.protobuf.BalGenerationConstants;
import org.ballerinalang.protobuf.exception.BalGenToolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.ballerinalang.protobuf.BalGenerationConstants.DESC_SUFFIX;
import static org.ballerinalang.protobuf.BalGenerationConstants.EMPTY_STRING;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTO_SUFFIX;

/**
 * Util function used when generating bal file from .proto definition.
 */
public class BalFileGenerationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BalFileGenerationUtils.class);
    
    /**
     * Create meta folder for storing meta files which is used in intermediate processing.
     *
     * @param folderPath folder path which is needed to be created.
     */
    public static void createMetaFolder(String folderPath) {
        boolean isFileCreated = new File(folderPath).getParentFile().mkdirs();
        if (!isFileCreated) {
            LOG.debug("Meta folder did not create successfully '" + folderPath + "'");
        }
        byte dataBytes[] = new byte[0];
        try {
            Path file = Paths.get(folderPath);
            Files.write(file, dataBytes);
        } catch (IOException e) {
            throw new BalGenToolException("Error creating .desc meta files.", e);
        }
    }
    
    /**
     * Execute command and generate file descriptor.
     *
     * @param command protoc executor command.
     */
    public static void generateDescriptor(String command) {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase(Locale.ENGLISH).startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }
        builder.directory(new File(System.getProperty("user.home")));
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new BalGenToolException("Error in executing protoc command '" + command + "'.", e);
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new BalGenToolException("Process not successfully completed. Process is interrupted while" +
                    " running the protoC executor.", e);
        }
        if (process.exitValue() != 0) {
            try (BufferedReader bufferedReader = new BufferedReader(new
                    InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String err;
                StringBuilder errMsg = new StringBuilder();
                while ((err = bufferedReader.readLine()) != null) {
                    errMsg.append(System.lineSeparator()).append(err);
                }
                throw new BalGenToolException(errMsg.toString());
            } catch (IOException e) {
                throw new BalGenToolException("Invalid command syntax.", e);
            }
        }
    }
    
    /**
     * Build descriptor path using dependent proto path.
     *
     * @param protoPath dependent protoPath
     * @return descriptor path of proto
     */
    public static String getDescriptorPath(String protoPath) {
        return BalGenerationConstants.META_DEPENDENCY_LOCATION + protoPath
                .substring(protoPath.lastIndexOf(BalGenerationConstants
                        .FILE_SEPARATOR), protoPath.length()).replace(PROTO_SUFFIX,
                        BalGenerationConstants.EMPTY_STRING) + DESC_SUFFIX;
    }
    
    /**
     * Resolve proto folder path from Proto file path.
     *
     * @param protoPath Proto file path
     * @return
     */
    public static String resolveProtoFloderPath(String protoPath) {
        int idx = protoPath.lastIndexOf(BalGenerationConstants.FILE_SEPARATOR);
        String protofolderPath = EMPTY_STRING;
        if (idx > 0) {
            protofolderPath = protoPath.substring(0, idx);
        }
        return protofolderPath;
    }
    
    /**
     * Generate proto file and convert it to byte array.
     *
     * @param exePath        protoc executor path
     * @param protoPath      .proto file path
     * @param descriptorPath file descriptor path.
     * @return byte array of generated proto file.
     */
    public static byte[] getProtoByteArray(String exePath, String protoPath, String descriptorPath) {
        
        String command = new ProtocCommandBuilder
                (exePath, protoPath, resolveProtoFloderPath(protoPath), descriptorPath).build();
        generateDescriptor(command);
        File initialFile = new File(descriptorPath);
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
            if (set.getFileList().size() > 0) {
                return set.getFile(0).toByteArray();
            }
        } catch (IOException e) {
            throw new BalGenToolException("Error reading generated descriptor file '" + descriptorPath + "'.", e);
        }
        return new byte[0];
    }
    
    /**
     * Used to clear the temporary files.
     *
     * @param file file to be deleted
     */
    public static void delete(File file) {
        if ((file != null) && file.exists() && file.isDirectory()) {
            String files[] = file.list();
            if (files != null) {
                if (files.length != 0) {
                    for (String temp : files) {
                        File fileDelete = new File(file, temp);
                        if (fileDelete.isDirectory()) {
                            delete(fileDelete);
                        }
                        if (fileDelete.delete()) {
                            LOG.debug("Successfully deleted file " + file.toString());
                        }
                    }
                }
            }
            if (file.delete()) {
                LOG.debug("Successfully deleted file " + file.toString());
            }
            if ((file.getParentFile() != null) && (file.getParentFile().delete())) {
                LOG.debug("Successfully deleted parent file " + file.toString());
            }
        } else if (file != null) {
            if (file.delete()) {
                LOG.debug("Successfully deleted parent file " + file.toString());
            }
        }
    }
    
    /**
     * Sae generated intermediate files.
     *
     * @param url  file URL
     * @param file destination file location
     */
    public static void saveFile(URL url, String file) {
        try (InputStream in = url.openStream(); FileOutputStream fos = new FileOutputStream(new File(file))) {
            int length;
            byte[] buffer = new byte[1024]; // buffer for portion of data from
            while ((length = in.read(buffer)) > -1) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            in.close();
        } catch (IOException e) {
            throw new BalGenToolException("Error saving file '" + file + "'.", e);
        }
    }
    
    /**
     * Grant permission to the protoc executor file.
     *
     * @param file protoc executor file.
     */
    public static void grantPermission(File file) {
        boolean isExecutable = file.setExecutable(true);
        boolean isReadable = file.setReadable(true);
        boolean isWritable = file.setWritable(true);
        if (isExecutable && isReadable && isWritable) {
            LOG.debug("Successfully grated permission for protoc exe file");
        } else {
            LOG.debug("Failed to prowide execute permission to protoc executor.");
        }
    }
}
