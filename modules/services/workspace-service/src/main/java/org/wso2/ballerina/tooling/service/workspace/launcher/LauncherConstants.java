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
    public static final String TERMINATE = "TERMINATE";
    public static final String INVALID_CMD = "INVALID_CMD";
    public static final String MSG_INVALID = "Unsupported command";


    public static final String OUTPUT = "OUTPUT";
    public static final String EXECUTION_STARTED = "EXECUTION_STARTED";
    public static final String EXECUTION_STOPED = "EXECUTION_STOPED";
    public static final String DEBUG = "DEBUG_PORT";
    public static final String EXIT = "EXIT";
    public static final String RUN_MESSAGE = "Running %s application." ;
    public static final String END_MESSAGE = "Execution Ended.";
    public static final String INVALID_BAL_PATH_MESSAGE = "ERROR: Unable to run program, ballerina runtime cannot be found.";
    public static final String SERVICE_MESSAGE = "Running %s service.";
    public static final String EXECUTION_TERMINATED = "EXECUTION_TERMINATED" ;
    public static final String SET_BAL_PATH_MESSAGE = "Please set BAL_HOME environment variable pointing to ballerina runtime.";


    public static enum ProgramType{
        RUN, SERVICE
    }

    //message types
    public static final String INFO = "INFO";
    public static final String ERROR = "ERROR";
    public static final String DATA = "DATA";
}
