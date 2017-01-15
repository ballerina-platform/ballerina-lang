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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;

/**
 * Utility class for file related operations
 */
public class FileConnectorUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileConnectorUtils.class);

    /**
     * @param remoteFile Location of the remote file
     * @return true/false
     */
    public static boolean isFolder(FileObject remoteFile) {
        boolean isFolder = false;
        if (StringUtils.isEmpty(remoteFile.getName().getExtension())) {
            isFolder = true;
        }
        return isFolder;
    }

    public static StandardFileSystemManager getManager() {
        StandardFileSystemManager fsm = null;
        try {
            fsm = new StandardFileSystemManager();
            fsm.init();
        } catch (FileSystemException e) {
            logger.error("Unable to get FileSystemManager: " + e.getMessage(), e);
        }
        return fsm;
    }

    public static FileSystemOptions init(Context context, String[] params) {
        String setTimeout = params[0] != null ? params[0] : "30000";
        String setPassiveMode = params[1] != null ? params[1] : "true";
        String setSoTimeout = params[2] != null ? params[2] : "60000";
        String setStrictHostKeyChecking = params[3] != null ? params[3] : "yes";
        String setUserDirIsRoot = params[4] != null ? params[4] : "false";

        if (logger.isDebugEnabled()) {
            logger.debug("File init starts with " + setTimeout + "," + setPassiveMode + "," +
                    "" + setSoTimeout + "," + setStrictHostKeyChecking + "," + setUserDirIsRoot);
        }
        FileSystemOptions opts = new FileSystemOptions();
        // SSH Key checking
        try {
            if (StringUtils.isEmpty(setStrictHostKeyChecking)) {
                SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            } else {
                setStrictHostKeyChecking = setStrictHostKeyChecking.trim();
                SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts,
                        setStrictHostKeyChecking);
            }
        } catch (FileSystemException e) {
            throw new BallerinaException("Error while configuring a " +
                    "setStrictHostKeyChecking", e);
        }
        // Root directory set to user home
        if (StringUtils.isEmpty(setUserDirIsRoot)) {
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
        } else {
            setUserDirIsRoot = setUserDirIsRoot.trim();
            try {
                SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, Boolean.valueOf
                        (setUserDirIsRoot));
            } catch (Exception e) {
                throw new BallerinaException("Error while configuring a " +
                        "setUserDirIsRoot", e);
            }
        }
        // Timeout is count by Milliseconds
        if (StringUtils.isEmpty(setTimeout)) {
            SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, FileConstants.TIME_OUT);
        } else {
            setTimeout = setTimeout.trim();
            try {
                SftpFileSystemConfigBuilder.getInstance().setTimeout(opts,
                        Integer.parseInt(setTimeout));
            } catch (NumberFormatException e) {
                throw new BallerinaException("Error while configuring a " +
                        "setTimeout", e);
            }
        }
        if (StringUtils.isEmpty(setPassiveMode)) {
            FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
            FtpsFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        } else {
            setPassiveMode = setPassiveMode.trim();
            try {
                FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts,
                        Boolean.valueOf(setPassiveMode));
                FtpsFileSystemConfigBuilder.getInstance().setPassiveMode(opts,
                        Boolean.valueOf(setPassiveMode));
            } catch (Exception e) {
                throw new BallerinaException("Error while configuring a " +
                        "setPassiveMode", e);
            }
        }
        if (StringUtils.isEmpty(setSoTimeout)) {
            FtpFileSystemConfigBuilder.getInstance().setSoTimeout(opts, FileConstants.TIME_OUT);
        } else {
            setSoTimeout = setSoTimeout.trim();
            try {
                FtpFileSystemConfigBuilder.getInstance().setSoTimeout(opts,
                        Integer.parseInt(setSoTimeout));
            } catch (NumberFormatException e) {
                throw new BallerinaException("Error while configuring a " +
                        "setSoTimeout", e);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("FileConnector configuration is completed.");
        }
        return opts;
    }

}
