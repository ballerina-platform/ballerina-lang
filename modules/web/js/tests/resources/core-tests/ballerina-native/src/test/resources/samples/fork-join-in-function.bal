import ballerina.lang.messages;
import ballerina.lang.system;

function testForkJoinAll(message m)(message[]) {

        message[] results;
        json error;
        int x = 100;
        float y = 1000.5;
        system:println("Airfare ");
        system:println(y);
        map mm = {"name":"chanaka", "age":"32"};
        system:println(mm["name"]);

        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                message m1 = messages:clone(m);
                messages:setJsonPayload(m1, payload);
                mm["name"] = "wso2";
                //x = 100;
                //system:println(mm["name"]);
                x = 234;
                system:println(y);
                reply m1;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                x = 500;
                system:println(x);
                messages:setJsonPayload(m, payload);
                reply m;
            }
        } join (all) (message[] airlineResponses) {
            system:println(messages:getStringPayload(airlineResponses[0]));
            system:println(messages:getStringPayload(airlineResponses[1]));
            results = airlineResponses;
            system:println(mm["name"]);
            x = 999;
            //return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
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

function testForkJoinAny(message m)(message[]) {

        message[] results;
        json error;
        system:println("Airfare ");
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                messages:setJsonPayload(m, payload);
                reply m;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                reply m;
            }
        } join (some 1) (message[] airlineResponses) {
            system:println(messages:getStringPayload(airlineResponses[0]));
            return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinAllOfSpecific(message m)(message[]) {

        message[] results;
        json error;
        system:println("Airfare ");
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                messages:setJsonPayload(m, payload);
                reply m;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                reply m;
            }

            worker PQR_Airline {
                json payload;
                payload = {"name":"pqr"};
                messages:setJsonPayload(m, payload);
                reply m;
            }
        } join (all ABC_Airline, XYZ_Airline) (message[] airlineResponses) {
            system:println(messages:getStringPayload(airlineResponses[0]));
            system:println(messages:getStringPayload(airlineResponses[1]));
            return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinAnyOfSpecific(message m)(message[]) {

        message[] results;
        json error;
        system:println("Airfare ");
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                messages:setJsonPayload(m, payload);
                reply m;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                reply m;
            }

            worker PQR_Airline {
                json payload;
                payload = {"name":"pqr"};
                messages:setJsonPayload(m, payload);
                reply m;
            }
        } join (some 1 ABC_Airline, XYZ_Airline) (message[] airlineResponses) {
            system:println(messages:getStringPayload(airlineResponses[0]));
            return airlineResponses;
        } timeout (30000) (message[] airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}