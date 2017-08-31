/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.workspace.launcher;


/**
 * Application launcher constants.
 */
public class LauncherConstants {
    /**
     * The minimum server port number. Lets start with 5006 which is the default port number
     */
    public static final int MIN_PORT_NUMBER = 5006;
    
    /**
     * The maximum server currentMinPort number.
     */
    public static final int MAX_PORT_NUMBER = 5999;
    
    public static final String LAUNCHER_WEBSOCKET_PATH = "/launch";
    
    public static final String RUN_PROGRAM = "RUN_PROGRAM";
    public static final String RUN_SERVICE = "RUN_SERVICE";
    public static final String DEBUG_PROGRAM = "DEBUG_PROGRAM";
    public static final String DEBUG_SERVICE = "DEBUG_SERVICE";
    public static final String TERMINATE = "TERMINATE";
    public static final String INVALID_CMD = "INVALID_CMD";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String MSG_INVALID = "Unsupported command";
    
    public static final String TRY_IT_URL = "TRY_IT_URL";
    public static final String OUTPUT = "OUTPUT";
    public static final String EXECUTION_STARTED = "EXECUTION_STARTED";
    public static final String EXECUTION_STOPED = "EXECUTION_STOPED";
    public static final String DEBUG = "DEBUG_PORT";
    public static final String EXIT = "EXIT";
    public static final String RUN_MESSAGE = "Running %s application.";
    public static final String END_MESSAGE = "Execution Ended.";
    public static final String INVALID_BAL_PATH_MESSAGE = "ERROR: Unable to run program, ballerina runtime cannot be " +
                                                          "found.";
    public static final String SERVICE_MESSAGE = "Running %s service.";
    public static final String EXECUTION_TERMINATED = "EXECUTION_TERMINATED";
    public static final String SET_BAL_PATH_MESSAGE = "Please set BALLERINA_HOME environment variable pointing to " +
                                                      "ballerina runtime.";

    public static final String SERVER_CONNECTOR_STARTED_AT_HTTP_DEFAULT_PORT_LOG = "ballerina: started server " +
                                                                                    "connector";
    public static final String SERVER_CONNECTOR_STARTED_LOG = "ballerina: started server connector";
    public static final String TERMINATE_MESSAGE = "Program Terminated.";
    public static final String UNSUPPORTED_OPERATING_SYSTEM = "Unsupported operating system";
    public static final String LOCAL_TRY_IT_URL = "http://%s:%s";
    
    /**
     * Types of programs that are executable.
     */
    public static enum ProgramType {
        RUN, SERVICE
    }
    
    //message types
    public static final String INFO = "INFO";
    public static final String ERROR = "ERROR";
    public static final String DATA = "DATA";
}
