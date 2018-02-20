import ballerina.io;
import ballerina.net.http;
import ballerina.runtime;

service<http> sample {

    int requestCount = 0;

    resource echo(http:Connection conn, http:InRequest req) {
        lock {
            requestCount = requestCount + 1;
        }
        _ = conn.respond({});
    }

    resource getCount(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("count - " + requestCount);
        _ = conn.respond(res);
    }
}


service<http> sample1 {

    int requestCount = 0;

    float price = 0;

    string finalText = "";

    Person person = {age:2, name:"a", address:{no:5, line1:"", line2:"ppp"}};

    float[] floatArr = [1, 2];

    map mapVal = {name:""};

    resource echo(http:Connection conn, http:InRequest req) {
        lock {
            requestCount = requestCount + 1;
            price = price + 2;
            person.age = person.age + 1;
            person.address.line1 = person.address.line1 + "5";
            finalText = finalText + "3";
            floatArr[0] = floatArr[0] + 1;
            floatArr[1] = floatArr[1] + 2;
            var strVal, _ = (string) mapVal.name;
            mapVal.name = strVal + "7";
        }
        _ = conn.respond({});
    }

    resource echo1(http:Connection conn, http:InRequest req) {
        lock {
            floatArr[1] = floatArr[1] + 2;
            finalText = finalText + "3";
            floatArr[0] = floatArr[0] + 1;
            requestCount = requestCount + 1;
            var strVal, _ = (string) mapVal.name;
            mapVal.name = strVal + "7";
            person.address.line1 = person.address.line1 + "5";
            person.age = person.age + 1;
            price = price + 2;
        }
        _ = conn.respond({});
    }

    resource echo2(http:Connection conn, http:InRequest req) {
        lock {
            person.age = person.age + 1;
            requestCount = requestCount + 1;
            person.address.line1 = person.address.line1 + "5";
            floatArr[1] = floatArr[1] + 2;
            finalText = finalText + "3";
            price = price + 2;
            var strVal, _ = (string) mapVal.name;
            mapVal.name = strVal + "7";
            floatArr[0] = floatArr[0] + 1;
        }
        _ = conn.respond({});
    }

    resource getResult(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var strVal, _ = (string) mapVal.name;
        res.setStringPayload(finalText + requestCount + price + person.age + person.address.line1
                             + floatArr[0] + floatArr[1] + strVal);
        _ = conn.respond(res);
    }
}

struct Person {
    int age;
    string name;
    Address address;
}

struct Address {
    int no;
    string line1;
    string line2;
}

string finalText1 = "";

Person person1 = {age:2, name:"a", address:{no:5, line1:"", line2:"ppp"}};

float[] floatArr1 = [1, 2];

service<http> sample2 {

    int requestCount = 0;

    float price = 0;

    map mapVal = {name:""};

    resource echo(http:Connection conn, http:InRequest req) {
        io:println("************************* echo");
        lock {
            requestCount = requestCount + 1;
            price = price + 2;
            person1.age = person1.age + 1;
            person1.address.line1 = person1.address.line1 + "5";
            finalText1 = finalText1 + "3";
            floatArr1[0] = floatArr1[0] + 1;
            floatArr1[1] = floatArr1[1] + 2;
            var strVal, _ = (string) mapVal.name;
            mapVal.name = strVal + "7";
        }
        _ = conn.respond({});
    }

    resource echo1(http:Connection conn, http:InRequest req) {
        io:println("************************* echo1");
        lock {
            floatArr1[1] = floatArr1[1] + 2;
            finalText1 = finalText1 + "3";
            floatArr1[0] = floatArr1[0] + 1;
            requestCount = requestCount + 1;
            var strVal, _ = (string) mapVal.name;
            mapVal.name = strVal + "7";
            person1.address.line1 = person1.address.line1 + "5";
            person1.age = person1.age + 1;
            price = price + 2;
        }
        _ = conn.respond({});
    }

    resource echo2(http:Connection conn, http:InRequest req) {
        io:println("************************* echo2");
        lock {
            person1.age = person1.age + 1;
            requestCount = requestCount + 1;
            person1.address.line1 = person1.address.line1 + "5";
            floatArr1[1] = floatArr1[1] + 2;
            finalText1 = finalText1 + "3";
            price = price + 2;
            var strVal, _ = (string) mapVal.name;
            mapVal.name = strVal + "7";
            floatArr1[0] = floatArr1[0] + 1;
        }
        _ = conn.respond({});
    }

    resource getResult(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        var strVal, _ = (string) mapVal.name;
        res.setStringPayload(finalText1 + requestCount + price + person1.age + person1.address.line1
                                + floatArr1[0] + floatArr1[1] + strVal);
        _ = conn.respond(res);
    }
}

service<http> sample3 {

    string message = "";

    resource echo(http:Connection conn, http:InRequest req) {
        lock {
            io:println("************** waiting inside first request");
            runtime:sleepCurrentWorker(100);
            message = "sample Response";
            error err = {message:"error occurred"};
            throw err;
        }
        _ = conn.respond({});
    }

    resource getMsg(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};

        lock {
            message = "modified by second resource";
        }
        res.setStringPayload(message);
        _ = conn.respond(res);
    }
}


