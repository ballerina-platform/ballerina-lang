import ballerina/io;
import ballerina/net.http;

function openFileSuccess(string path) returns (boolean | error) {
    return true;
}

function openFileFailure(string path) returns (boolean | error) {
    error  e = {message: "file not found error: " + path};
    return e;
}

function testSafeAssignmentBasics1 () returns (boolean | error) {
    boolean statusSuccess =? openFileSuccess("/home/sameera/foo.txt");
    io:println("there...success..");

    return statusSuccess;
}

function testSafeAssignmentBasics2 () returns (boolean | error) {
      boolean statusFailure =? openFileFailure("/home/sameera/bar.txt");
       io:println("there....failure.");
      return statusFailure;
}


function testSafeAssignmentBasics3 () {
    io:println("there....failure3. 0");
    boolean statusFailure =? openFileFailure("/home/sameera/bar.txt");
    io:println("there....failure3. 1");
}

function testSafeAssignmentBasics4 () returns (boolean){
    boolean statusFailure =? openFileFailure("/home/sameera/bar.txt");
    return statusFailure;
}

function testSafeAssignOpInAssignmentStatement1 () returns (boolean) {
    boolean b = false;
    int a = 0;
    b =? openFileSuccess("/home/sameera/foo.txt");
    return b;
}

function testSafeAssignOpInAssignmentStatement2 () returns (boolean|error) {
    boolean b = false;
    int a = 0;
    b =? openFileFailure("/home/sameera/foo.txt");
    return b;
}

function testSafeAssignOpInAssignmentStatement3 () returns (boolean|error) {
    FileOpenStatus fos = {};
    fos.status =? openFileSuccess("/home/sameera/foo.txt");
    return fos.status;
}

struct FileOpenStatus {
    boolean status = false;
}

function testSafeAssignOpInAssignmentStatement4 () returns (boolean|error) {
    boolean[] ba = [];
    ba[0] =? openFileSuccess("/home/sameera/foo.txt");
    ba[1] = false;
    return ba[0];
}

function testSafeAssignOpInAssignmentStatement5 () {
    io:println("there....failure3. 0");
    boolean statusFailure;
    int a = 10;
    statusFailure =? openFileFailure("/home/sameera/bar.txt");
    io:println("there....failure3. 1");
}

function testSafeAssignOpInAssignmentStatement6 () returns boolean {
    io:println("there....failure3. 0");
    int a = 10;
    var statusFailure =? openFileSuccess("/home/sameera/bar.txt");
    io:println("there....failure3. 1");
    return statusFailure;
}

struct person {
    string name;
}

public struct myerror {
    string message;
    error[] cause;
    int code;
}

function getPerson() returns person | myerror {
   //myerror e = {message:"ddd"};
    //return e;
    person p = {name:"Diayasena"};
    return  p;
}

function testSafeAssignOpInAssignmentStatement7 () returns string {
    var p =? getPerson();
    return p.name;
}
