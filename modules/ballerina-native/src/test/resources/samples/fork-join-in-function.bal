import ballerina.lang.message;
import ballerina.lang.system;

function testForkJoinAll(message m)(message[]) {

        message[] results;
        json error;
        system:println("Airfare ");
        fork (m) {
            worker ABC_Airline (message m) {
                json payload;
                payload = `{"name":"abc"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }

            worker XYZ_Airline (message m) {
                json payload;
                payload = `{"name":"xyz"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }
        } join (all) (message[] airlineResponses) {
            system:println(message:getStringPayload(airlineResponses[0]));
            system:println(message:getStringPayload(airlineResponses[1]));
            return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
            system:println("error occurred");
            error = `{"error":{"code":"500", "reason":"timed out"}}`;
            message res = {};
            message:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinAny(message m)(message[]) {

        message[] results;
        json error;
        system:println("Airfare ");
        fork (m) {
            worker ABC_Airline (message m) {
                json payload;
                payload = `{"name":"abc"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }

            worker XYZ_Airline (message m) {
                json payload;
                payload = `{"name":"xyz"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }
        } join (any 1) (message[] airlineResponses) {
            system:println(message:getStringPayload(airlineResponses[0]));
            return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
            system:println("error occurred");
            error = `{"error":{"code":"500", "reason":"timed out"}}`;
            message res = {};
            message:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinAllOfSpecific(message m)(message[]) {

        message[] results;
        json error;
        system:println("Airfare ");
        fork (m) {
            worker ABC_Airline (message m) {
                json payload;
                payload = `{"name":"abc"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }

            worker XYZ_Airline (message m) {
                json payload;
                payload = `{"name":"xyz"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }

            worker PQR_Airline (message m) {
                json payload;
                payload = `{"name":"pqr"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }
        } join (all ABC_Airline, XYZ_Airline) (message[] airlineResponses) {
            system:println(message:getStringPayload(airlineResponses[0]));
            system:println(message:getStringPayload(airlineResponses[1]));
            return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
            system:println("error occurred");
            error = `{"error":{"code":"500", "reason":"timed out"}}`;
            message res = {};
            message:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinAnyOfSpecific(message m)(message[]) {

        message[] results;
        json error;
        system:println("Airfare ");
        fork (m) {
            worker ABC_Airline (message m) {
                json payload;
                payload = `{"name":"abc"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }

            worker XYZ_Airline (message m) {
                json payload;
                payload = `{"name":"xyz"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }

            worker PQR_Airline (message m) {
                json payload;
                payload = `{"name":"pqr"}`;
                message:setJsonPayload(m, payload);
                reply m;
            }
        } join (any 1 ABC_Airline, XYZ_Airline) (message[] airlineResponses) {
            system:println(message:getStringPayload(airlineResponses[0]));
            return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
            system:println("error occurred");
            error = `{"error":{"code":"500", "reason":"timed out"}}`;
            message res = {};
            message:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}