/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.debugger;


/**
 * {@code DebugConstants} define debugger constants.
 *
 * @since 0.8.0
 */
public class DebugConstants {

    //default debugger port where websocket server will listen
    static final String DEFAULT_DEBUG_PORT = "5006";

    //debugger web-socket path.
    static final String DEBUG_WEBSOCKET_PATH = "/debug";

    // commands sent by client
    static final String CMD_START = "START";
    static final String CMD_STOP = "STOP";
    static final String CMD_SET_POINTS = "SET_POINTS";
    static final String CMD_STEP_OVER = "STEP_OVER";
    static final String CMD_RESUME = "RESUME";
    static final String CMD_STEP_IN = "STEP_IN";
    static final String CMD_STEP_OUT = "STEP_OUT";

    // messages sent back to client
    static final String CODE_HIT = "DEBUG_HIT";
    static final String MSG_HIT = "Debug point hit.";

    static final String CODE_INVALID = "INVALID";
    static final String MSG_INVALID = "Invalid Command";
    static final String MSG_INVALID_WORKER_ID = "Invalid Worker ID : ";
    static final String MSG_INVALID_BREAKPOINT = "Invalid Breakpoint : ";

    static final String CODE_ACK = "ACK";

    static final String CODE_EXIT = "EXIT";
    static final String MSG_EXIT = "Exiting from debugger.";

    //startup message.
    static final String DEBUG_MESSAGE = "Ballerina remote debugger is activated on port : ";
    public static final String ERROR_JSON = "{ \"error\": true }";
    static final String DEBUG_SERVER_ERROR = "Debug Server Error. Closing client connection.";
}
