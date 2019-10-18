import listenerProject/bee;

service sample2 on bee:ep {

    resource function foo(string b) {
    }

    resource function bar(string b) {
    }
}

public function getStartAndAttachCount() returns string {
    return bee:getStartAndAttachCount();
}
