/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.server;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * FTP embedded server, that is used for the purpose of testing teh file server connector related scenarios.
 */
public class FTPTestServer implements Server {
    private static FTPTestServer instance = new FTPTestServer();
    private Logger logger = LoggerFactory.getLogger(FTPTestServer.class);
    private FtpServer ftpServer;

    private FTPTestServer() {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        user.setName("username");
        user.setPassword("password");
        ClassLoader classLoader = getClass().getClassLoader();
        String fileURI = new File(classLoader.getResource(Constant.FTP_LOCATION).getFile()).getAbsolutePath();
        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        user.setHomeDirectory(fileURI);
        try {
            userManager.save(user);
        } catch (FtpException e) {
            logger.error("Exception occured while saving the user.", e);
        }

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(2221);

        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());
        ftpServer = factory.createServer();
    }

    /**
     * To get the instance of FTPTestServer.
     *
     * @return instance of the FTPTestServer
     */
    public static FTPTestServer getInstance() {
        return instance;
    }

    /**
     * To start the FTP server.
     *
     * @throws FtpException FTP Exception
     */
    @Override
    public void start() throws FtpException {
        ftpServer.start();
    }

    /**
     * To stop the FTP server
     */
    @Override
    public void stop() {
        if (!ftpServer.isStopped()) {
            ftpServer.stop();
        }
    }

    @Override
    public void restart() throws Exception {
        ftpServer.start();
    }

    @Override
    public boolean isRunning() {
        return !ftpServer.isStopped() && !ftpServer.isSuspended();
    }
}
