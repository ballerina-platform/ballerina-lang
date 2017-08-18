import ballerina.lang.messages;
import ballerina.lang.system;

function testForkJoinAny(message m)(message[], int) {

        message[] results = [null];
        json error;
        system:println("Airfare ");
        int i = 0;
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                messages:setJsonPayload(m, payload);
                m -> fork;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                m -> fork;
            }
        } join (some 1) (map airlineResponses) {
            if (airlineResponses["ABC_Airline"] != null) {
                any[] abc;
                abc,_ = (any[]) airlineResponses["ABC_Airline"];
                results[0], _ = (message) abc[0];
                system:println(results[0]);
                i = i + 1;
                //return results, i;
            }

            if (airlineResponses["XYZ_Airline"] != null) {
                any[] xyz;
                xyz,_ = (any[]) airlineResponses["XYZ_Airline"];
                results[0], _ = (message) xyz[0];
                system:println(results[0]);
                i = i + 1;
                //return results;
            }
            return results, i;

        } timeout (30000) (any[][] airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results, 0;
        }
}