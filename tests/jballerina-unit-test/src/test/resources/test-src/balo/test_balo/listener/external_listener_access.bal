import listenerProject/bee;

service /sample2 on bee:ep {

    resource function get foo(string b) {
    }

    resource function get bar(string b) {
    }
}

service /sample3 on bee:ep1 {
    resource function get foo(string b) {}
}

public function getStartAndAttachCount() returns string {
    return bee:getStartAndAttachCount();
}
