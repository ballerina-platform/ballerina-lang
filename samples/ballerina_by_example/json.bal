import ballerina.lang.system;
import ballerina.lang.json;

function main (string[] args) {

    json msg;
    json msg1;
    json jsonStringArray;
    string jsonPath;

    json message;
    string fname;
    string lname;
    string name;
    json msg2;

    msg = `{"name":{"fname":"Jack","lname":"Taylor"}, "state":"CA", "age":20}`;
    msg1 = `{"name":{"fname":"Jack","lname":"Taylor"}, "state":"CA", "age":20}`;
    jsonStringArray = `{"users":["Jack", "Peter"]}`;

    //set a value to a json path.
    //value can be a string,int,float,double,boolean,json
    json:set(msg, "$.name.fname", "Paul");
    system:println(json:getString(msg, "$.name.fname"));

    //add to object.
    json:add(msg, "$.name", "nickName", "Paul");
    system:println(json:toString(msg));

    //add to array.
    json:add(jsonStringArray, "$.users", "Jos");
    system:println(json:toString(jsonStringArray));

    //remove an object from a json.
    json:remove(msg, "$.name");
    system:println(json:toString(msg));

    //rename an object in a json.
    json:rename(msg1, "$.name", "fname", "firstName");
    jsonPath = "$.name.firstName";
    system:println(json:getString(msg1, jsonPath));



    fname = "John";
    lname = "Smith";
    name = "name";

    //variable access within json value (string or int) template.
    message = `{"name":${fname}}`;
    system:println(json:toString(message));

    //enrich json with a json expression.
    msg2 = `{"name":"John"}`;
    message = `${msg2}`;
    system:println(json:toString(message));

    //multiple variable access.
    msg2 = `{"name":{"first_name":${fname}, "last_name":${lname}}}`;
    message = `${msg2}`;
    system:println(json:toString(message));

    //json variable parts access.
    message = `${name}${lname}`;
    system:println(json:toString(message));

    //convert a json to string.
    system:println(json:toString(msg1));

}