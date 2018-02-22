package ballerina.net.http.resiliency;

import ballerina.net.http;

//Constants to represent Failover connector actions.
public const string FORWARD = "forward";
public const string GET = "get";
public const string POST = "post";
public const string DELETE = "delete";
public const string OPTIONS = "options";
public const string PUT = "put";
public const string PATCH = "patch";
public const string HEAD = "head";

enum HttpOperation {
    GET, POST, DELETE, OPTIONS, PUT, PATCH, HEAD, FORWARD
}

// makes the actual endpoints call according to the http operation passed in.
public function invokeEndpoint (string path, http:OutRequest outRequest, http:InRequest inRequest,
                                HttpOperation requestAction, http:HttpClient httpClient)
(http:InResponse, http:HttpConnectorError) {

    endpoint<http:HttpClient> endPoint {
    }

    bind httpClient with endPoint;

    if (HttpOperation.GET == requestAction) {
        return endPoint.get(path, outRequest);
    } else if (HttpOperation.POST == requestAction) {
        return endPoint.post(path, outRequest);
    } else if (HttpOperation.OPTIONS == requestAction) {
        return endPoint.options(path, outRequest);
    } else if (HttpOperation.PUT == requestAction) {
        return endPoint.put(path, outRequest);
    } else if (HttpOperation.DELETE == requestAction) {
        return endPoint.delete(path, outRequest);
    } else if (HttpOperation.PATCH == requestAction) {
        return endPoint.patch(path, outRequest);
    } else if (HttpOperation.FORWARD == requestAction) {
        return endPoint.forward(path, inRequest);
    } else if (HttpOperation.HEAD == requestAction) {
        return endPoint.head(path, outRequest);
    } else {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.statusCode = 400;
        httpConnectorError.message = "Unsupported connector action received.";
        return null, httpConnectorError;
    }
}

// Extracts HttpOperation from the Http verb passed in.
function extractHttpOperation (string httpVerb) (HttpOperation connectorAction) {
    HttpOperation inferredConnectorAction;
    if (GET == httpVerb) {
        inferredConnectorAction = HttpOperation.GET;
    } else if (POST == httpVerb) {
        inferredConnectorAction = HttpOperation.POST;
    } else if (OPTIONS == httpVerb) {
        inferredConnectorAction = HttpOperation.OPTIONS;
    } else if (PUT == httpVerb) {
        inferredConnectorAction = HttpOperation.PUT;
    } else if (DELETE == httpVerb) {
        inferredConnectorAction = HttpOperation.DELETE;
    } else if (PATCH == httpVerb) {
        inferredConnectorAction = HttpOperation.PATCH;
    } else if (FORWARD == httpVerb) {
        inferredConnectorAction = HttpOperation.FORWARD;
    } else if (HEAD == httpVerb) {
        inferredConnectorAction = HttpOperation.HEAD;
    }
    return inferredConnectorAction;
}
