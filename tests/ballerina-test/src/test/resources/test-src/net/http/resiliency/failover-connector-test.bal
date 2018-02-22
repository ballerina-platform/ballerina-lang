import ballerina.net.http;
import ballerina.net.http.resiliency;
import ballerina.mime;

http:Retry retryConfiguration = {count:0};

http:HttpClient endPoint1 = (http:HttpClient) create MockHttpFailOverClient("http://invalidEP", {});
http:HttpClient endPoint2 = (http:HttpClient) create MockHttpFailOverClient("http://localhost:8080",
                                                   {endpointTimeout:5000,
                                                       keepAlive:true,
                                                       retryConfig:retryConfiguration});

http:HttpClient[] failoverGroup = [endPoint1, endPoint2];
int[] errorCodes = [404, 502];

resiliency:FailoverConfig errorCode = {failoverCodes:errorCodes};

function testSuccessScenario () (http:InResponse, http:HttpConnectorError) {
    endpoint<http:HttpClient> ep {
        create resiliency:Failover(failoverGroup, errorCode);
    }

    http:InResponse clientResponse = {};
    http:HttpConnectorError err;

    mime:MediaType contentType = mime:getMediaType("application/xml");
    mime:Entity requestEntity = {};
    requestEntity.setXml(xml `<name>Ballerina</name>`);
    requestEntity.contentType = contentType;

    http:OutRequest outReq = {};
    outReq.setEntity(requestEntity);

    int counter = 0;

    while (counter < 2) {
        clientResponse, err = ep.post("/", outReq);
        counter = counter + 1;
    }

    return clientResponse, err;
}

function testFailureScenario () (http:InResponse, http:HttpConnectorError) {
    endpoint<http:HttpClient> ep {
        create resiliency:Failover(failoverGroup, errorCode);
    }

    http:InResponse clientResponse = {};
    http:HttpConnectorError err;

    mime:MediaType contentType = mime:getMediaType("application/xml");
    mime:Entity requestEntity = {};
    requestEntity.setXml(xml `<name>Ballerina</name>`);
    requestEntity.contentType = contentType;

    http:OutRequest outReq = {};
    outReq.setEntity(requestEntity);

    int counter = 0;

    while (counter < 1) {
        clientResponse, err = ep.post("/", outReq);
        counter = counter + 1;
    }

    return clientResponse, err;
}

connector MockHttpFailOverClient (string serviceUri, http:Options connectorOptions) {

    endpoint<http:HttpClient> endPoint {
    }

    int actualRequestNumber = 0;

    action post (string path, http:OutRequest request) (http:InResponse, http:HttpConnectorError) {
        http:InResponse response;
        http:HttpConnectorError err;
        response, err = generateResponse(actualRequestNumber);
        actualRequestNumber = actualRequestNumber + 1;
        return response, err;
    }

    action head (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action put (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action execute (string httpVerb, string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action patch (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action delete (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action get (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action options (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action forward (string path, http:InRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }
}

function generateErrorResponse () (http:InResponse, http:HttpConnectorError) {
    http:HttpConnectorError err = {};
    err.message = "Connection refused";
    err.statusCode = 502;
    return null, err;
}

function generateResponse (int count) (http:InResponse, http:HttpConnectorError) {

    http:InResponse inResponse = {};
    http:HttpConnectorError err = {};
    if (count == 0) {
        err.message = "Connection refused";
        err.statusCode = 502;
        return null, err;
    } else {
        inResponse.statusCode = 200;
        mime:Entity requestEntity = {};
        requestEntity.setXml(xml `<name>Ballerina</name>`);
        inResponse.setEntity(requestEntity);
        return inResponse, null;
    }
}
