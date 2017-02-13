package samples.fork_join;

import ballerina.lang.message;
import ballerina.lang.system;

function testForkJoin(message m)(message[]) {

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
