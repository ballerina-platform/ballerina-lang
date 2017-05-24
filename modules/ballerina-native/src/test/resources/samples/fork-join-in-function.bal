import ballerina.lang.messages;
import ballerina.lang.system;

function testForkJoinAll(message m)(message[]) {

        message[] results = [null, null];
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
                m1 -> ;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                x = 500;
                system:println(x);
                messages:setJsonPayload(m, payload);
                m -> ;
            }
        } join (all) (any[][] airlineResponses) {
            results[0] = (message) airlineResponses[0][0];
            results[1] = (message) airlineResponses[1][0];
            system:println(results[0]);
            system:println(results[1]);
            system:println(mm["name"]);
            x = 999;
            //return airlineResponses;
        } timeout (30000) (any[][] airlineResponses) {
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

        message[] results = [null];
        json error;
        system:println("Airfare ");
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }
        } join (some 1) (any[][] airlineResponses) {
            results[0] = (message) airlineResponses[0][0];
            system:println(results[0]);
            return results;
        } timeout (30000) (any[][] airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinAllOfSpecific(message m)(message[]) {

        message[] results = [null, null];
        json error;
        system:println("Airfare ");
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }

            worker PQR_Airline {
                json payload;
                payload = {"name":"pqr"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }
        } join (all ABC_Airline, XYZ_Airline) (any[][] airlineResponses) {
            results[0] = (message) airlineResponses[0][0];
            results[1] = (message) airlineResponses[1][0];
            system:println(results[0]);
            system:println(results[1]);
            return results;
        } timeout (30000) (any[][] airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinAnyOfSpecific(message m)(message[]) {

        message[] results = [null];
        json error;
        system:println("Airfare ");
        fork {
            worker ABC_Airline {
                json payload;
                payload = {"name":"abc"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }

            worker PQR_Airline {
                json payload;
                payload = {"name":"pqr"};
                messages:setJsonPayload(m, payload);
                m -> ;
            }
        } join (some 1 ABC_Airline, XYZ_Airline) (any[][] airlineResponses) {
            results[0] = (message) airlineResponses[0][0];
            system:println(results[0]);
            return results;
        } timeout (30000) (any[][] airlineResponses) {
            system:println("error occurred");
            error = {"error":{"code":"500", "reason":"timed out"}};
            message res = {};
            messages:setJsonPayload(res, error);
            results[0] = m;
            return results;
        }
}

function testForkJoinWithoutTimeoutExpression()(map m) {
    m = {"name":"Abhaya", "era":"Anuradhapura"};
    fork {
    worker W1 {
    system:println(m["name"]);
    m["time"] = "120 BC";
    }
    worker W2 {
    system:println(m["name"]);
    m["period"] = "30 years";
    }
    } join (all) (any[][] results) {
    system:println(m["time"]);
    system:println(m["period"]);
    }
    system:println("After the fork-join statement");
    return m;
}