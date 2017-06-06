import ballerina.lang.messages;
import ballerina.lang.system;

function testForkJoinAll(message m)(message[]) {
    return testForkJoinAllVM(m);
  }

function testForkJoinAllVM(message m)(message[]) {

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
        } join (all) (any[][] airlineResponses) {
            results[0] = (message) airlineResponses[0][0];
            results[1] = (message) airlineResponses[1][0];
            system:println(results[0]);
            system:println(results[1]);
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
                m -> fork;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                m -> fork;
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
                m -> fork;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                m -> fork;
            }

            worker PQR_Airline {
                json payload;
                payload = {"name":"pqr"};
                messages:setJsonPayload(m, payload);
                m -> fork;
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
                m -> fork;
            }

            worker XYZ_Airline {
                json payload;
                payload = {"name":"xyz"};
                messages:setJsonPayload(m, payload);
                m -> fork;
            }

            worker PQR_Airline {
                json payload;
                payload = {"name":"pqr"};
                messages:setJsonPayload(m, payload);
                m -> fork;
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

function testForkJoinWithoutTimeoutExpression()(int, float) {
    int x;
    float y;
    fork {
    worker W1 {
    100 -> fork;
    }
    worker W2 {
    1.23 -> fork;
    }
    } join (all) (any[][] results) {
    x = (int)results[0][0];
    y = (float)results[1][0];
    system:println(x);
    system:println(y);
    }
    system:println("After the fork-join statement");
    return x, y;
}