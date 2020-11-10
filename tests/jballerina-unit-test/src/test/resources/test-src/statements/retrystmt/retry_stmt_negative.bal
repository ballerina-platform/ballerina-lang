import ballerina/io;

class MyRetryManager {
    public function shouldRetry(error e) returns boolean {
        return true;
    }
}

class CustomRetryManager {
    public function shouldItRetry(error e) returns boolean {
        return true;
    }
}

function testInvalidRetryManagerType() {
    retry<CustomRetryManager> {
        io:println("Retry task triggered!");
    }
}

function testRetryWithUnreachableCode(int i) returns string {
    string str = "start";
    retry {
        str += " within retry block";
        return str;
    }
    str += " end";
    return str;
}
