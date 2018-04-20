import ballerina/io;
import ballerina/http;
import ballerina/runtime;

endpoint http:NonListener echoEP {
    port:9090
};

@http:ServiceConfig {}
service<http:Service> sample bind echoEP {

    int requestCount = 0;

    echo(endpoint conn, http:Request req) {
        lock {
            requestCount = requestCount + 1;
        }
        http:Response res = new;
        _ = conn -> respond(res);
    }

    getCount(endpoint conn, http:Request req) {
        http:Response res = new;
        res.setStringPayload("count - " + requestCount);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {}
service<http:Service> sample1 bind echoEP {

    int requestCount = 0;

    float price = 0;

    string finalText = "";

    Person person = {age:2, name:"a", address:{no:5, line1:"", line2:"ppp"}};

    float[] floatArr = [1, 2];

    map mapVal = {name:""};

    echo(endpoint conn, http:Request req) {
        lock {
            requestCount = requestCount + 1;
            price = price + 2;
            person.age = person.age + 1;
            person.address.line1 = person.address.line1 + "5";
            finalText = finalText + "3";
            floatArr[0] = floatArr[0] + 1;
            floatArr[1] = floatArr[1] + 2;
            var strVal = <string> mapVal.name;
            mapVal.name = strVal + "7";
        }
       http:Response res = new;
        _ = conn -> respond(res);
    }

    echo1(endpoint conn, http:Request req) {
        lock {
            floatArr[1] = floatArr[1] + 2;
            finalText = finalText + "3";
            floatArr[0] = floatArr[0] + 1;
            requestCount = requestCount + 1;
            var strVal = <string> mapVal.name;
            mapVal.name = strVal + "7";
            person.address.line1 = person.address.line1 + "5";
            person.age = person.age + 1;
            price = price + 2;
        }
         http:Response res = new;
        _ = conn -> respond(res);
    }

    echo2(endpoint conn, http:Request req) {
        lock {
            person.age = person.age + 1;
            requestCount = requestCount + 1;
            person.address.line1 = person.address.line1 + "5";
            floatArr[1] = floatArr[1] + 2;
            finalText = finalText + "3";
            price = price + 2;
            var strVal = <string> mapVal.name;
            mapVal.name = strVal + "7";
            floatArr[0] = floatArr[0] + 1;
        }
        http:Response res = new;
        _ = conn -> respond(res);
    }

    getResult(endpoint conn, http:Request req) {
        http:Response res = new;
        var strVal = <string> mapVal.name;
        res.setStringPayload(finalText + requestCount + price + person.age + person.address.line1
                             + floatArr[0] + floatArr[1] + strVal);
        _ = conn -> respond(res);
    }
}

type Person {
    int age,
    string name,
    Address address,
};

type Address {
    int no,
    string line1,
    string line2,
};

string finalText1 = "";

Person person1 = {age:2, name:"a", address:{no:5, line1:"", line2:"ppp"}};

float[] floatArr1 = [1, 2];

@http:ServiceConfig {}
service<http:Service> sample2 bind echoEP {

    int requestCount = 0;

    float price = 0;

    map mapVal = {name:""};

    echo(endpoint conn, http:Request req) {
        io:println("************************* echo");
        lock {
            requestCount = requestCount + 1;
            price = price + 2;
            person1.age = person1.age + 1;
            person1.address.line1 = person1.address.line1 + "5";
            finalText1 = finalText1 + "3";
            floatArr1[0] = floatArr1[0] + 1;
            floatArr1[1] = floatArr1[1] + 2;
            var strVal  = <string> mapVal.name;
            mapVal.name = strVal + "7";
        }
        http:Response res = new;
        _ = conn -> respond(res);
    }

    echo1(endpoint conn, http:Request req) {
        io:println("************************* echo1");
        lock {
            floatArr1[1] = floatArr1[1] + 2;
            finalText1 = finalText1 + "3";
            floatArr1[0] = floatArr1[0] + 1;
            requestCount = requestCount + 1;
            var strVal = <string> mapVal.name;
            mapVal.name = strVal + "7";
            person1.address.line1 = person1.address.line1 + "5";
            person1.age = person1.age + 1;
            price = price + 2;
        }
        http:Response res = new;
        _ = conn -> respond(res);
    }

    echo2(endpoint conn, http:Request req) {
        io:println("************************* echo2");
        lock {
            person1.age = person1.age + 1;
            requestCount = requestCount + 1;
            person1.address.line1 = person1.address.line1 + "5";
            floatArr1[1] = floatArr1[1] + 2;
            finalText1 = finalText1 + "3";
            price = price + 2;
            var strVal = <string> mapVal.name;
            mapVal.name = strVal + "7";
            floatArr1[0] = floatArr1[0] + 1;
        }
        http:Response res = new;
        _ = conn -> respond(res);
    }

    getResult(endpoint conn, http:Request req) {
        http:Response res = new;
        var strVal = <string> mapVal.name;
        res.setStringPayload(finalText1 + requestCount + price + person1.age + person1.address.line1
                                + floatArr1[0] + floatArr1[1] + strVal);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {}
service<http:Service> sample3 bind echoEP {

    string message = "";

    echo(endpoint conn, http:Request req) {
        lock {
            io:println("************** waiting inside first request");
            runtime:sleep(100);
            message = "sample Response";
            error err = {message:"error occurred"};
            throw err;
        }
        //http:Response res = new;
        //_ = conn -> respond(res);
    }

    getMsg(endpoint conn, http:Request req) {
        http:Response res = new;

        lock {
            message = "modified by second resource";
        }
        res.setStringPayload(message);
        _ = conn -> respond(res);
    }
}
