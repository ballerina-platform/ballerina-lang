import listenerproject/test_listener;

service sample2 on test_listener:ep {

    resource function foo(string b) {
    }

    resource function bar(string b) {
    }
}

service sample3 on test_listener:ep1 {
    resource function foo(string b) {}
}

public function getStartAndAttachCount() returns string {
    return test_listener:getStartAndAttachCount();
}
