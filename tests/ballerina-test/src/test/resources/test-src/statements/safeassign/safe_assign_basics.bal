import ballerina.io;

function openFileSuccess(string path) returns (boolean | error) {
    //int k =? 5;
    return true;
}

function openFileFailure(string path) returns (boolean | error) {
    error e = {message: "file not found error: " + path};
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



function testSafeAssignmentBasics5 (){
}