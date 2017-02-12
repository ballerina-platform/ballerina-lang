package org.wso2.ballerina.tooling.service.workspace.launcher;


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

    public static final String LAUNCHER_PORT = "9092";
    public static final String MESSAGE = "Launch service started at ";
    public static final String RUN_PROGRAM = "RUN_PROGRAM";
    public static final String RUN_SERVICE = "RUN_SERVICE";
    public static final String DEBUG_PROGRAM = "DEBUG_PROGRAM";
    public static final String DEBUG_SERVICE = "DEBUG_SERVICE";
    public static final String STOP = "STOP";
    public static final String INVALID_CMD = "INVALID_CMD";
    public static final String MSG_INVALID = "Unsupported command";


    public static final String OUTPUT = "OUTPUT";
}
