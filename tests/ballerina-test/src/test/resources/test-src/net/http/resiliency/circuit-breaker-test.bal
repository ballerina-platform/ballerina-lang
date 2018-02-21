import ballerina.net.http;
import ballerina.net.http.resiliency;
import ballerina.runtime;

const string TEST_SCENARIO_HEADER = "test-scenario";

const string SCENARIO_TYPICAL = "typical-scenario";
const string SCENARIO_TRIAL_RUN_FAILURE = "trial-run-failure";

function testTypicalScenario () (http:InResponse[], http:HttpConnectorError[]) {

    endpoint<resiliency:CircuitBreaker> circuitBreakerEP {
        create resiliency:CircuitBreaker((http:HttpClient)create MockHttpClient("http://localhost:8080", {}), 0.1, 2000);
    }

    http:OutRequest request;
    http:InResponse[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;

    while (counter < 8) {
        request = {};
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TYPICAL);
        responses[counter], errs[counter] = circuitBreakerEP.get("/hello", request);
        counter = counter + 1;

        // To ensure the reset timeout period expires
        if (counter == 5) {
            runtime:sleepCurrentWorker(5000);
        }
    }

    return responses, errs;
}

function testTrialRunFailure () (http:InResponse[], http:HttpConnectorError[]) {

    endpoint<resiliency:CircuitBreaker> circuitBreakerEP {
        create resiliency:CircuitBreaker((http:HttpClient)create MockHttpClient("http://localhost:8080", {}), 0.1, 2000);
    }

    http:OutRequest request;
    http:InResponse[] responses = [];
    http:HttpConnectorError[] errs = [];
    int counter = 0;

    while (counter < 6) {
        request = {};
        request.setHeader(TEST_SCENARIO_HEADER, SCENARIO_TRIAL_RUN_FAILURE);
        responses[counter], errs[counter] = circuitBreakerEP.get("/hello", request);
        counter = counter + 1;

        if (counter == 3) {
            runtime:sleepCurrentWorker(5000);
        }
    }

    return responses, errs;
}

connector MockHttpClient (string serviceUri, http:Options connectorOptions) {

    int actualRequestNumber = 0;

    action post (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
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
        http:InResponse response;
        http:HttpConnectorError err;
        actualRequestNumber = actualRequestNumber + 1;

        string scenario = req.getHeader(TEST_SCENARIO_HEADER);

        if (scenario == SCENARIO_TYPICAL) {
            response, err = handleScenario1(actualRequestNumber);
        } else if (scenario == SCENARIO_TRIAL_RUN_FAILURE) {
            response, err = handleScenario2(actualRequestNumber);
        }

        return response, err;
    }

    action options (string path, http:OutRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }

    action forward (string path, http:InRequest req) (http:InResponse, http:HttpConnectorError) {
        return null, null;
    }
}

function handleScenario1 (int requesetNo) (http:InResponse, http:HttpConnectorError) {
    // Deliberately fail a request
    if (requesetNo == 3) {
        http:HttpConnectorError err = getErrorStruct();
        return null, err;
    }

    http:InResponse response = getResponse();
    return response, null;
}

function handleScenario2 (int counter) (http:InResponse, http:HttpConnectorError) {
    // Fail a request. Then, fail the trial request sent while in the HALF_OPEN state as well.
    if (counter == 2 || counter == 3) {
        http:HttpConnectorError err = getErrorStruct();
        return null, err;
    }

    http:InResponse response = getResponse();
    return response, null;
}

function getErrorStruct () (http:HttpConnectorError) {
    http:HttpConnectorError err = {};
    err.message = "Connection refused";
    err.statusCode = 502;
    return err;
}

function getResponse () (http:InResponse) {
    // TODO: The way the status code is set may need to be changed once struct fields can be made read-only
    http:InResponse response = {};
  //  MockInResponse response = {};
    response.statusCode = 200;
    return response;
}

public struct MockInResponse {
    int statusCode;
    string reasonPhrase;
    string server;
}
