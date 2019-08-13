package org.ballerinalang.openapi;

/**
 * This class contains the messages constants required for OpenApi tool.
 */
public class OpenApiMesseges {

    public static final String CLIENT_MANDATORY = "Client name is mandatory to generate the ballerina client. " +
            "\nE.g. ballerina openapi gen-client [<module>]:<servicename> <openapicontract>";
    public static final String OPENAPI_FILE_MANDATORY = "An OpenApi definition file is required to " +
            "generate the client. \nE.g: ballerina openapi gen-client [<module>]:<servicename> <OpenApiContract>";
    public static final String OPENAPI_CLIENT_EXCEPTION = "Error occurred when generating client for openapi contract";


    public static final String CONTRACT_SERVICE_MANDATORY = "A service name is mandatory to generate an " +
            "OpenApi contract.";
    public static final String CONTRACT_BALLERINA_DOC_MANDATORY = "Please specify a ballerina document path in " +
            "order generate an OpenApi contract for the service ";




    public static final String GEN_SERVICE_MODULE_REQUIRED = "A module name is required to successfully generate the" +
                                                             " service from the provided OpenApi contract. " +
                                                             "\nE.g ballerina openapi gen-service " +
                                                             "<modulename>:<servicename>";
    public static final String GEN_SERVICE_PROJECT_ROOT = "Ballerina service generation should be done from the " +
                                                          "project root. \nIf you like to start with a new project " +
                                                          "use `ballerina new` command to create a new project.";
    public static final String MODULE_DIRECTORY_EXCEPTION = "Unable to create module directory. File system error " +
                                                            "occured.";
    public static final String RESOURCE_DIRECTORY_EXCEPTION = "Unable to create resource directory. File system error" +
            " occured.";
    public static final String SOURCE_DIRECTORY_EXCEPTION = "Unable to create source directory. File system error " +
            "occured.";
    public static final String DEFINITION_EXISTS = "There is already an OpenApi contract in the location.";

    private OpenApiMesseges() {
        throw new AssertionError();
    }

}
