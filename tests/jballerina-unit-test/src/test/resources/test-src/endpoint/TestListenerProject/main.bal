import endpoint_pkg.a;

service sample2 on a:ep {

    resource function foo(string b) {
    }

    resource function bar(string b) {
    }
}

public function getStartAndAttachCount() returns string {
    return a:getStartAndAttachCount();
}
