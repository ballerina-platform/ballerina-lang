import org.wso2.ballerina.connectors.salesforcerest;

import ballerina.lang.jsons;
import ballerina.lang.system;
import ballerina.net.http;

function main (string[] args) {

    http:Response response;
    json JSONResponse;

    string accessToken = args[0];
    string clientID = args[1];
    string clientSecret = args[2];
    string refreshToken = args[3];
    string apiInstance = args[4];
    string refreshEndpoint = args[5];
    string apiVersion = "v32.0";

    salesforcerest:ClientConnector salesforceClient;
    // Create a Salesforcerest Client Connector with the arguments accessToken, clientID,
    // clientSecret, refreshToken, apiInstance, refreshEndpoint
    salesforceClient = create salesforcerest:ClientConnector(accessToken, clientID,
                             clientSecret, refreshToken, apiInstance, refreshEndpoint);

    // Call describeGlobal action to list the available objects and their metadata for
    // your organization’s data
    response = salesforceClient.describeGlobal (apiVersion);
    JSONResponse = response.getJsonPayload();

    //Print the response
    system:println("===== Response from describeGlobal action =====");
    system:println(jsons:toString(JSONResponse));

    // Call sObjectDescribe action with to describes the individual metadata at all
    // levels for the specified object
    string sobjectName = "account";
    response = salesforceClient.sObjectDescribe (apiVersion, sobjectName);
    JSONResponse = response.getJsonPayload();

    system:println("===== Response from sObjectDescribe action =====");
    system:println(jsons:toString(JSONResponse));

    // Call query action to execute the specified SOQL query
    string query = "SELECT+Name+FROM+account";
    response = salesforceClient.query (apiVersion, query);
    JSONResponse = response.getJsonPayload();

    system:println("===== Response from query action =====");
    system:println(jsons:toString(JSONResponse));

    // Call queryAll action to retrieve the records that have been deleted because of a
    // merge or delete, archived Task and Event records
    response = salesforceClient.queryAll (apiVersion, query);
    JSONResponse = response.getJsonPayload();

    system:println("===== Response from queryAll action =====");
    system:println(jsons:toString(JSONResponse));

    // Call createRecord methods to create new record with record
    json sampleCreateRecord = {"Name":"TestingBallerina"};
    response = salesforceClient.createRecord (apiVersion, "Account", sampleCreateRecord);
    JSONResponse = response.getJsonPayload();

    system:println("===== Response from createRecord action =====");
    system:println(jsons:toString(JSONResponse));
}
