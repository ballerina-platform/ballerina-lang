import a;

service /sample2 on a:ep {

    resource function get foo(string b) {
    }

    resource function get bar(string b) {
    }
}

public function getStartAndAttachCount() returns string {
    return a:getStartAndAttachCount();
}
