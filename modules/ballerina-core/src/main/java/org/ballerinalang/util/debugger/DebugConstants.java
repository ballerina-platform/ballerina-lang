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
    public static final String CMD_START = "START";
    public static final String CMD_STOP = "STOP";
    public static final String CMD_SET_POINTS = "SET_POINTS";
    public static final String CMD_STEP_OVER = "STEP_OVER";
    public static final String CMD_RESUME = "RESUME";
    public static final String CMD_STEP_IN = "STEP_IN";
    public static final String CMD_STEP_OUT = "STEP_OUT";

    // messages sent back to client
    public static final String CODE_HIT = "DEBUG_HIT";
    public static final String MSG_HIT = "Debug point hit.";

    public static final String CODE_INVALID = "INVALID";
    public static final String MSG_INVALID = "Invalid Command";

    public static final String CODE_ACK = "ACK";

    public static final String CODE_COMPLETE = "COMPLETE";
    public static final String MSG_COMPLETE = "Debug session completed.";

    public static final String CODE_EXIT = "EXIT";
    public static final String MSG_EXIT = "Exiting from debugger.";

    //startup message.
    public static final String DEBUG_MESSAGE = "Ballerina remote debugger is activated on port : ";
    public static final String ERROR_JSON = "{ \"error\": true }";
    public static final String DEBUG_SERVER_ERROR = "Debug Server Error. Closing client connection.";
}
