/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.exception.LSConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility operations for the connector service.
 *
 * @since 2.0.0
 */


public class LSConnectorUtil {
    private static final String ZIPENTRY_FILE_SEPARATOR = "/";
    private static final Logger logger = LoggerFactory.getLogger(LSConnectorUtil.class);


    private LSConnectorUtil() {
    }

    public static void extract(Path baloPath, String cacheableKey)
            throws LSConnectorException {
        FileInputStream baloFileInputStream;
        if (!Files.exists(baloPath)) {
            throw new LSConnectorException("Invalid module source root provided for module: ["
                    + cacheableKey + "]");
        }
        // Create the new extract root
        Path destinationPath = CommonUtil.LS_CONNECTOR_CACHE_DIR.resolve(cacheableKey);
        if (Files.exists(destinationPath)) {
            return;
        }
        try {
            destinationPath = Files.createDirectories(CommonUtil.LS_CONNECTOR_CACHE_DIR.resolve(cacheableKey));
        } catch (IOException e) {
            throw new LSConnectorException(e.getMessage(), e);
        }

        byte[] buffer = new byte[1024];
        try {
            baloFileInputStream = new FileInputStream(baloPath.toAbsolutePath().toString());
        } catch (FileNotFoundException e) {
            throw new LSConnectorException(e.getMessage(), e);
        }

        try (ZipInputStream zis = new ZipInputStream(baloFileInputStream)) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                if (zipEntry.isDirectory() && fileName.startsWith(ProjectDirConstants.SOURCE_DIR_NAME)) {
                    Files.createDirectories(destinationPath.resolve(fileName));
                } else {
                    if (fileName.equalsIgnoreCase(ProjectDirConstants.BALO_METADATA_DIR_NAME +
                            ZIPENTRY_FILE_SEPARATOR + ProjectDirConstants.MANIFEST_FILE_NAME)) {
                        fileName = ProjectDirConstants.MANIFEST_FILE_NAME;
                    } else if (!fileName.startsWith(ProjectDirConstants.SOURCE_DIR_NAME)) {
                        fileName = null;
                    }
                    if (fileName != null) {
                        File newFile = destinationPath.resolve(fileName).toFile();
                        int length;
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            while ((length = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            boolean readOnly = newFile.setReadOnly();
                            if (!readOnly) {
                                logger.warn("Failed to set the cached source read only");
                            }
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            throw new LSConnectorException(e.getMessage(), e);
        } finally {
            try {
                baloFileInputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
