package org.wso2.ballerina.sample;

import ballerina.lang.json;
import ballerina.lang.message;
import ballerina.lang.string;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.net.uri;
import ballerina.util;

connector BasicAuthConnector (string userName, string password) {

    http:HTTPConnector httpConnectorEP = new http:HTTPConnector("");
 
    string encodedBasicAuthHeader;

    action get(BasicAuthConnector basicAuthConnector, string path, message request) (message) {

    	message response;

    	encodedBasicAuthHeader = constructBasicAuthHeader (request, encodedBasicAuthHeader, userName, password);

    	response = http:HTTPConnector.get(httpConnectorEP, path, request);

    	return response;
     }

    action post(BasicAuthConnector basicAuthConnector, string path, message request) (message) {

	    message response;
	   	 
    	encodedBasicAuthHeader = constructBasicAuthHeader (request, encodedBasicAuthHeader, userName, password);

    	response = http:HTTPConnector.post(httpConnectorEP, path, request);

    	return response;
     }

    action put(BasicAuthConnector basicAuthConnector, string path, message request) (message) {

    	message response;

    	encodedBasicAuthHeader = constructBasicAuthHeader (request, encodedBasicAuthHeader, userName, password);

    	response = http:HTTPConnector.put(httpConnectorEP, path, request);

    	return response;
     }

    action delete(BasicAuthConnector basicAuthConnector, string path, message request)(message) {

    	message response;

    	encodedBasicAuthHeader = constructBasicAuthHeader (request, encodedBasicAuthHeader, userName, password);
  	 
    	response = http:HTTPConnector.delete(httpConnectorEP, path, request);

    	return response;
    }
}

function constructBasicAuthHeader(message request, string encodedBasicAuthHeader, string userName, string password) (string) {

	if (encodedBasicAuthHeader == "") {
    	encodedBasicAuthHeader = util:base64encode(userName + ":" + password);
	}
    
	message:setHeader(request, "Authorization", "Basic "+ encodedBasicAuthHeader);
    
	return encodedBasicAuthHeader;
}

function main (string[] args) {

	sample:BasicAuthConnector basicAuthConnector = new sample:BasicAuthConnector(args[0], args[1]);

	message request;
	message basicAuthResponse;
	json basicJSONResponse;

	request = new message;

	basicAuthResponse = sample:BasicAuthConnector.get(basicAuthConnector, args[2], request);

	basicJSONResponse = message:getJsonPayload(basicAuthResponse);
	system:println(json:toString(basicJSONResponse));
}
