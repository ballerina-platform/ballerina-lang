/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.nativeimpl.connectors.file.util;


import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;

import java.util.zip.ZipInputStream;

/**
 * Utility class for file unzip
 */
public class FileUnzipUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUnzipUtil.class);
    private static StandardFileSystemManager manager = null;

    /**
     * @param source        Location of the zip file
     * @param destDirectory Location of the destination folder
     */
    public boolean unzip(String source, String destDirectory, Context messageContext) {
//        OMFactory factory = OMAbstractFactory.getOMFactory();
//        OMNamespace ns = factory.createOMNamespace(FileConstants.FILECON, FileConstants.NAMESPACE);
//        OMElement result = factory.createOMElement(FileConstants.RESULT, ns);
//        boolean resultStatus = false;
//        String[] params = new String[0];
//        FileSystemOptions opts = FileConnectorUtils.init(messageContext, params);
//        try {
//            manager = FileConnectorUtils.getManager();
//            // Create remote object
//            FileObject remoteFile = manager.resolveFile(source, opts);
//            FileObject remoteDesFile = manager.resolveFile(destDirectory, opts);
//            // File destDir = new File(destDirectory);
//            if (remoteFile.exists()) {
//                if (!remoteDesFile.exists()) {
//                    //create a folder
//                    remoteDesFile.createFolder();
//                }
//                //open the zip file
//                ZipInputStream zipIn = new ZipInputStream(remoteFile.getContent().getInputStream());
//                ZipEntry entry = zipIn.getNextEntry();
//                try {
//                    // iterates over entries in the zip file
//                    while (entry != null) {
//                        // boolean testResult;
//                        String filePath = destDirectory + File.separator + entry.getName();
//                        // Create remote object
//                        FileObject remoteFilePath = manager.resolveFile(filePath, opts);
//                        if (log.isDebugEnabled()) {
//                            log.debug("The created path is " + remoteFilePath.toString());
//                        }
//                        try {
//                            if (!entry.isDirectory()) {
//                                // if the entry is a file, extracts it
//                                extractFile(zipIn, filePath, opts);
//                                OMElement messageElement = factory.createOMElement(FileConstants.FILE
//                                        , ns);
//                                messageElement.setText(entry.getName() + " | status:" + "true");
//                                result.addChild(messageElement);
//                            } else {
//                                // if the entry is a directory, make the directory
//                                remoteFilePath.createFolder();
//                            }
//                        } catch (IOException e) {
//                            log.error("Unable to process the zip file. ", e);
//                        } finally {
//                            zipIn.closeEntry();
//                            entry = zipIn.getNextEntry();
//                        }
//                    }
//                    //BXML bxml = new BXML(result);
//                    resultStatus = true;
//                } finally {
//                    //we must always close the zip file
//                    zipIn.close();
//                }
//            } else {
//                log.error("File does not exist.");
//            }
//        } catch (IOException e) {
//            log.error("Unable to process the zip file." + e.getMessage(), e);
//        } finally {
//            manager.close();
//        }
//        return resultStatus;
        return true;
    }

    /**
     * @param zipIn    :Input zip stream
     * @param filePath :Location of each entry of the file.
     */
    public void extractFile(ZipInputStream zipIn, String filePath, FileSystemOptions opts) {
//        BufferedOutputStream bos = null;
//        try {
//            // Create remote object
//            FileObject remoteFilePath = manager.resolveFile(filePath, opts);
//            //open the zip file
//            OutputStream fOut = remoteFilePath.getContent().getOutputStream();
//            bos = new BufferedOutputStream(fOut);
//            byte[] bytesIn = new byte[FileConstants.BUFFER_SIZE];
//            int read;
//            while ((read = zipIn.read(bytesIn)) != -1) {
//                bos.write(bytesIn, 0, read);
//            }
//        } catch (IOException e) {
//            log.error("Unable to read an entry: " + e.getMessage(), e);
//        } finally {
//            //we must always close the zip file
//            if (bos != null) {
//                try {
//                    bos.close();
//                } catch (IOException e) {
//                    log.error("Error while closing the BufferedOutputStream: " + e.getMessage(), e);
//                }
//            }
//        }
    }
}
