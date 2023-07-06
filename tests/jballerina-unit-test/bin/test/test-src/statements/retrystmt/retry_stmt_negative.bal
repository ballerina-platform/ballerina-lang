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
        // do nothing
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
