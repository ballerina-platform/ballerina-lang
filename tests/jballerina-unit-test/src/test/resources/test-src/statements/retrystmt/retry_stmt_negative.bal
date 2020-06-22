import ballerina/io;

type MyRetryManager object {
    public function shouldRetry(error e) returns boolean {
        return true;
    }
};

type CustomRetryManager object {
    public function shouldItRetry(error e) returns boolean {
        return true;
    }
};

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
