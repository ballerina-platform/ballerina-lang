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

package org.ballerinalang.test.util;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * FTP embedded server, that is used for the purpose of testing teh file server connector related scenarios.
 */
public class FTPTestServer {
    private static FTPTestServer instance = new FTPTestServer();
    private FtpServer ftpServer;

    /**
     * To get the instance of FTPTestServer.
     * @return instance of the FTPTestServer
     */
    public static FTPTestServer getInstance() {
        return instance;
    }

    /**
     * To start the FTP server.
     * @throws FtpException FTP Exception
     * @throws IOException IO Exception
     */
    public void start() throws FtpException, IOException {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        user.setName("username");
        user.setPassword("password");
        ClassLoader classLoader = getClass().getClassLoader();
        String fileURI = new File(classLoader.getResource(Constant.VFS_LOCATION).getFile()).getAbsolutePath();
        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        user.setHomeDirectory(fileURI);
        userManager.save(user);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(2221);

        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        factory.addListener("default", listenerFactory.createListener());

        if (ftpServer == null) {
            ftpServer = factory.createServer();
            ftpServer.start();
        }
    }
}
