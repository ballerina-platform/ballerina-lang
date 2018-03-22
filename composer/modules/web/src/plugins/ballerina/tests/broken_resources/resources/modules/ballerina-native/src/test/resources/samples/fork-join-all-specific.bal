import ballerina/lang.messages;
import ballerina/lang.system;

function testForkJoinAllOfSpecific(message m)(message[]) {

        message[] results = [null, null];
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
        } join (all ABC_Airline, XYZ_Airline) (map airlineResponses) {
            any[] abc;
            any[] xyz;
            abc,_ = (any[]) airlineResponses["ABC_Airline"];
            xyz,_ = (any[]) airlineResponses["XYZ_Airline"];
            results[0], _ = (message) abc[0];
            results[1], _ = (message) xyz[0];
            system:println(results[0]);
            system:println(results[1]);
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