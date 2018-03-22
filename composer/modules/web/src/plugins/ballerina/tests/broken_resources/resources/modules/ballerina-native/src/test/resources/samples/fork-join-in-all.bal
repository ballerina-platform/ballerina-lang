import ballerina/lang.messages;
import ballerina/lang.system;

function testForkJoinAll(message m)(message[]) {

        message[] results = [null, null];
        json error;
        int x = 100;
        float y = 1000.5;
        system:println("Airfare ");
        system:println(y);

        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                message m1 = messages:clone(m);
                messages:setJsonPayload(m1, payload);
                //x = 100;
                //system:println(mm["name"]);
                x = 234;
                system:println(y);
                m1 -> fork;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                x = 500;
                system:println(x);
                messages:setJsonPayload(m, payload);
                m -> fork;
            }
        } join (all) (map airlineResponses) {
            any[] abc;
            any[] xyz;
            abc,_ = (any[]) airlineResponses["ABC_Airline"];
            xyz,_ = (any[]) airlineResponses["XYZ_Airline"];
            results[0], _ = (message) abc[0];
            results[1], _ = (message) xyz[0];
            system:println(results[0]);
            system:println(results[1]);
            x = 999;
            //return airlineResponses;
        } timeout (30000) (map airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            //return results;
        }
        system:println("Outside fork join statement");
        system:println(results[0]);
        system:println(results[1]);
        system:println(x);
        return results;
}