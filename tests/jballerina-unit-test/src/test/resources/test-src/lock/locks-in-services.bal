import ballerina/io;
import ballerina/http;
import ballerina/runtime;

listener http:MockListener echoEP = new(9090);

int sampleRequestCount = 0;

@http:ServiceConfig {}
service sample on echoEP {

    resource function echo(http:Caller conn, http:Request req) {
        lock {
            sampleRequestCount = sampleRequestCount + 1;
        }
        http:Response res = new;
        checkpanic conn->respond(res);
    }

    resource function getCount(http:Caller conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("count - " + sampleRequestCount.toString());
        checkpanic conn->respond(res);
    }
}

int sample1RequestCount = 0;
float sample1Price = 0.0;
string sample1FinalText = "";
float[] sample1FloatArr = [1.0, 2.0];
Person sample1Person = {age:2, name:"a", address:{no:5, line1:"", line2:"ppp"}};
map<string> sample1MapVal = {name:""};

@http:ServiceConfig {}
service sample1 on echoEP {

    resource function echo(http:Caller conn, http:Request req) {
        lock {
            sample1RequestCount = sample1RequestCount + 1;
            sample1Price = sample1Price + 2;
            sample1Person.age = sample1Person.age + 1;
            sample1Person.address.line1 = sample1Person.address.line1 + "5";
            sample1FinalText = sample1FinalText + "3";
            sample1FloatArr[0] = sample1FloatArr[0] + 1;
            sample1FloatArr[1] = sample1FloatArr[1] + 2;
            var strVal = <string> sample1MapVal["name"];
            sample1MapVal["name"] = strVal + "7";
        }
       http:Response res = new;
        checkpanic conn->respond(res);
    }

    resource function echo1(http:Caller conn, http:Request req) {
        lock {
            sample1FloatArr[1] = sample1FloatArr[1] + 2;
            sample1FinalText = sample1FinalText + "3";
            sample1FloatArr[0] = sample1FloatArr[0] + 1;
            sample1RequestCount = sample1RequestCount + 1;
            var strVal = <string> sample1MapVal["name"];
            sample1MapVal["name"] = strVal + "7";
            sample1Person.address.line1 = sample1Person.address.line1 + "5";
            sample1Person.age = sample1Person.age + 1;
            sample1Price = sample1Price + 2;
        }
         http:Response res = new;
        checkpanic conn->respond(res);
    }

    resource function echo2(http:Caller conn, http:Request req) {
        lock {
            sample1Person.age = sample1Person.age + 1;
            sample1RequestCount = sample1RequestCount + 1;
            sample1Person.address.line1 = sample1Person.address.line1 + "5";
            sample1FloatArr[1] = sample1FloatArr[1] + 2;
            sample1FinalText = sample1FinalText + "3";
            sample1Price = sample1Price + 2;
            var strVal = <string> sample1MapVal["name"];
            sample1MapVal["name"] = strVal + "7";
            sample1FloatArr[0] = sample1FloatArr[0] + 1;
        }
        http:Response res = new;
        checkpanic conn->respond(res);
    }

    resource function getResult(http:Caller conn, http:Request req) {
        http:Response res = new;
        var strVal = <string> sample1MapVal["name"];
        res.setTextPayload(sample1FinalText + sample1RequestCount.toString() + sample1Price.toString() + sample1Person.age.toString() + sample1Person.address.line1
                             + sample1FloatArr[0].toString() + sample1FloatArr[1].toString() + strVal);
        checkpanic conn->respond(res);
    }
}

type Person record {
    int age = 0;
    string name = "";
    Address address = {};
};

type Address record {
    int no = 0;
    string line1 = "";
    string line2 = "";
};

string finalText1 = "";
Person person1 = {age:2, name:"a", address:{no:5, line1:"", line2:"ppp"}};
float[] floatArr1 = [1.0, 2.0];
int sample2RequestCount = 0;
float sample2Price = 0.0;
map<string> sample2MapVal = {name:""};

@http:ServiceConfig {}
service sample2 on echoEP {



    resource function echo(http:Caller conn, http:Request req) {
        io:println("************************* echo");
        lock {
            sample2RequestCount = sample2RequestCount + 1;
            sample2Price = sample2Price + 2;
            person1.age = person1.age + 1;
            person1.address.line1 = person1.address.line1 + "5";
            finalText1 = finalText1 + "3";
            floatArr1[0] = floatArr1[0] + 1;
            floatArr1[1] = floatArr1[1] + 2;
            var strVal  = <string> sample2MapVal["name"];
            sample2MapVal["name"] = strVal + "7";
        }
        http:Response res = new;
        checkpanic conn->respond(res);
    }

    resource function echo1(http:Caller conn, http:Request req) {
        io:println("************************* echo1");
        lock {
            floatArr1[1] = floatArr1[1] + 2;
            finalText1 = finalText1 + "3";
            floatArr1[0] = floatArr1[0] + 1;
            sample2RequestCount = sample2RequestCount + 1;
            var strVal = <string> sample2MapVal["name"];
            sample2MapVal["name"] = strVal + "7";
            person1.address.line1 = person1.address.line1 + "5";
            person1.age = person1.age + 1;
            sample2Price = sample2Price + 2;
        }
        http:Response res = new;
        checkpanic conn->respond(res);
    }

    resource function echo2(http:Caller conn, http:Request req) {
        io:println("************************* echo2");
        lock {
            person1.age = person1.age + 1;
            sample2RequestCount = sample2RequestCount + 1;
            person1.address.line1 = person1.address.line1 + "5";
            floatArr1[1] = floatArr1[1] + 2;
            finalText1 = finalText1 + "3";
            sample2Price = sample2Price + 2;
            var strVal = <string> sample2MapVal["name"];
            sample2MapVal["name"] = strVal + "7";
            floatArr1[0] = floatArr1[0] + 1;
        }
        http:Response res = new;
        checkpanic conn->respond(res);
    }

    resource function getResult(http:Caller conn, http:Request req) {
        http:Response res = new;
        var strVal = <string> sample2MapVal["name"];
        res.setTextPayload(finalText1 + sample2RequestCount.toString() + sample2Price.toString() + person1.age.toString() + person1.address.line1
                                + floatArr1[0].toString() + floatArr1[1].toString() + strVal);
        checkpanic conn->respond(res);
    }
}

string message = "";

@http:ServiceConfig {}
service sample3 on echoEP {

    resource function echo(http:Caller conn, http:Request req) {
        lock {
            io:println("************** waiting inside first request");
            runtime:sleep(100);
            message = "sample Response";
            error err = error("error occurred");
            panic err;
        }
        //http:Response res = new;
        //checkpanic conn->respond(res);
    }

    resource function getMsg(http:Caller conn, http:Request req) {
        http:Response res = new;

        lock {
            message = "modified by second resource";
        }
        res.setTextPayload(message);
        checkpanic conn->respond(res);
    }
}

//Test when there is a field access within a lock
@http:ServiceConfig {}
service sample4 on echoEP {

    resource function echo(http:Caller conn, http:Request req) {

       Person p = {};
       workerFunc(p);

        http:Response res = new;
        res.setTextPayload(p.age.toString());
        checkpanic conn->respond(res);
    }
}

function workerFunc(Person param) {

    worker w1 {
        increment(param);
    }

    runtime:sleep(10);
    increment(param);


}

function increment(Person param) {
   lock {
       foreach var i in 1 ... 1000 {
           param.age = param.age + i;
       }
    }
}
