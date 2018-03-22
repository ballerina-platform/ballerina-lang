import ballerina/lang.messages;
import ballerina/lang.system;

function testForkJoinAnyOfSpecific(message m)(message[]) {

        message[] results = [null];
        json error;
        system:println("Airfare ");
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                message m1 = messages:clone(m);
                messages:setJsonPayload(m1, payload);
                m1 -> fork;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                message m1 = messages:clone(m);
                messages:setJsonPayload(m1, payload);
                m1 -> fork;
            }

            worker PQR_Airline {
                json payload;
                payload = {"name":"pqr"};
                message m1 = messages:clone(m);
                messages:setJsonPayload(m1, payload);
                m1 -> fork;
            }
        } join (some 1 ABC_Airline, XYZ_Airline) (map airlineResponses) {
            if (airlineResponses["ABC_Airline"] != null) {
                any[] abc;
                abc,_ = (any[]) airlineResponses["ABC_Airline"];
                results[0], _ = (message) abc[0];
                system:println(results[0]);
                return results;
            }

            if (airlineResponses["XYZ_Airline"] != null) {
                any[] xyz;
                xyz,_ = (any[]) airlineResponses["XYZ_Airline"];
                results[0], _ = (message) xyz[0];
                system:println(results[0]);
                return results;
            }
            return results;
        } timeout (30) (map airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}