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

import org.ballerinalang.protobuf.BalGenerationConstants;
import org.ballerinalang.protobuf.exception.CodeGeneratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.ballerinalang.protobuf.BalGenerationConstants.EMPTY_STRING;

/**
 * Util function used when generating bal file from .proto definition.
 */
public class BalFileGenerationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BalFileGenerationUtils.class);
    
    /**
     * Execute command and generate file descriptor.
     *
     * @param command protoc executor command.
     * @throws CodeGeneratorException if an error occurred when executing protoc command.
     */
    public static void generateDescriptor(String command) throws CodeGeneratorException {
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
            throw new CodeGeneratorException("Error in executing protoc command '" + command + "'.", e);
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new CodeGeneratorException("Process not successfully completed. Process is interrupted while" +
                    " running the protoc executor.", e);
        }
        if (process.exitValue() != 0) {
            try (BufferedReader bufferedReader = new BufferedReader(new
                    InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String err;
                StringBuilder errMsg = new StringBuilder();
                while ((err = bufferedReader.readLine()) != null) {
                    errMsg.append(System.lineSeparator()).append(err);
                }
                throw new CodeGeneratorException(errMsg.toString());
            } catch (IOException e) {
                throw new CodeGeneratorException("Invalid command syntax.", e);
            }
        }
    }
    
    /**
     * Resolve proto folder path from Proto file path.
     *
     * @param protoPath Proto file path
     * @return Parent folder path of proto file.
     */
    public static String resolveProtoFolderPath(String protoPath) {
        int idx = protoPath.lastIndexOf(BalGenerationConstants.FILE_SEPARATOR);
        String protoFolderPath = EMPTY_STRING;
        if (idx > 0) {
            protoFolderPath = protoPath.substring(0, idx);
        }
        return protoFolderPath;
    }
    
    /**
     * Used to clear the temporary files.
     *
     * @param file file to be deleted
     */
    public static void delete(File file) {
        if ((file != null) && file.exists() && file.isDirectory()) {
            String[] files = file.list();
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
     * Download file in the url to the destination file.
     *
     * @param url  file URL.
     * @param file destination file.
     * @throws CodeGeneratorException if an error occurred while downloading the file.
     */
    public static void downloadFile(URL url, File file) throws CodeGeneratorException {
        try (InputStream in = url.openStream(); FileOutputStream fos = new FileOutputStream(file)) {
            int length;
            byte[] buffer = new byte[1024]; // buffer for portion of data from
            while ((length = in.read(buffer)) > -1) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            String msg = "Error while downloading the file: " + file.getName() + ". " + e.getMessage();
            throw new CodeGeneratorException(msg, e);
        }
    }
    
    /**
     * Grant permission to the protoc executor file.
     *
     * @param file protoc executor file.
     * @throws CodeGeneratorException if an error occurred while providing execute permission to protoc executor file.
     */
    public static void grantPermission(File file) throws CodeGeneratorException {
        boolean isExecutable = file.setExecutable(true);
        boolean isReadable = file.setReadable(true);
        boolean isWritable = file.setWritable(true);
        if (isExecutable && isReadable && isWritable) {
            LOG.debug("Successfully granted permission for protoc exe file");
        } else {
            String msg = "Error while providing execute permission to protoc executor file: " + file.getName();
            throw new CodeGeneratorException(msg);
        }
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("windows");
    }
}
