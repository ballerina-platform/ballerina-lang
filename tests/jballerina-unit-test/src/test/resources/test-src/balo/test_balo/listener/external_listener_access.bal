import listenerProject/bee;

service sample2 on bee:ep {

    resource function foo(string b) {
    }

    resource function bar(string b) {
    }
}

service sample3 on bee:ep1 {
    resource function foo(string b) {}
}

public function getStartAndAttachCount() returns string {
    return bee:getStartAndAttachCount();
}
