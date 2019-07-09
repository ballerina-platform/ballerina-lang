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

    private OpenApiMesseges() {
        throw new AssertionError();
    }

}
