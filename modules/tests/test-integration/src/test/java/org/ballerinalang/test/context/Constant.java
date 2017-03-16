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
package org.ballerinalang.test.context;

/**
 * Constant used for server startup.
 */
public class Constant {
    //Name of the system property define the location of the service distribution
    public static final String SYSTEM_PROP_SERVER_ZIP = "server.zip";
    //Name of the system property define the current dir of maven execution
    public static final String SYSTEM_PROP_BASE_DIR = "basedir";
    //Name of the script file which start the server
    public static final String BALLERINA_SERVER_SCRIPT_NAME = "ballerina";
    //Name of the file where ballerina server process id is stored
    public static final String SERVER_PID_FILE_NAME = "ballerina.pid";
    //File extension the the ballerina service file
    public static final String SERVICE_FILE_EXTENSION = ".bal";
    //Default HTTP port of the server
    public static final int DEFAULT_HTTP_PORT = 9090;
    //Service samples file location
    public static final String SERVICE_SAMPLE_DIR = "samples";
    //ActiveMq broker URL
    public static final String ACTIVEMQ_PROVIDER_URL = "vm://localhost:6161";
    public static final String ACTIVEMQ_ALL_JAR = "activemq-all-5.14.3.jar";
    // Common nets jar needed for ftp support
    public static final String COMMON_NETS_JAR = "commons-net-3.6.jar";
    // Vfs location in the local directory
    public static final String VFS_LOCATION = "FTPLocation";
}
