import ballerina.lang.system;
import ballerina.lang.json;

function main (string[] args) {

    json msg;
    json msg1;
    json jsonStringArray;
    string jsonPath;

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
    json:getString(msg1, jsonPath);

    //convert a json to string.
    system:println(json:toString(msg1));

}