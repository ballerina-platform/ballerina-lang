import ballerina/http;

public client class InternalClient {
    public string url;

    isolated function init(string url) returns error? {
        self.url = url;
    }

    remote isolated function getAll(@untainted string path) returns string|error? {
        return "Hello";
    }
}

ExternalClient|error exEp0 = new ("http://example.com/0");

public function main() returns error? {
    ExternalClient exEp1 = check new ("http://example.com/1");
    var localRes = check exEp1->getAll("");
    boolean|error status = secondFunc(exEp1);

    if true {
        ExternalClient exEp2 = check new ("http://example.com/2");
    }

    any functionResult = function() {
        ExternalClient|error exEp3 = new ("http://example.com/3");
        return ();
    };
}

function secondFunc(ExternalClient exEpP1) returns boolean|error {
    int count = 0;
    while count > 5 {
        ExternalClient exEp4 = check new ("http://example.com/4");
    }

    do {
        ExternalClient exEp5 = check new ("http://example.com/5");
    }

    ExternalClient exEp6 = check new ("http://example.com/6");

    if count == 5 {
        _ = check exEp6->getAll("4");
    } else {
        ExternalClient exEp7 = check new ("http://example.com/7");
    }

    return true;
}

function thirdFunc() returns boolean|error {
    ExternalClient exEp8 = check new ("http://example.com/8");
    if true {
        ExternalClient exEp9 = check new ("http://example.com/9");
    }
    ExternalClient exEp6 = check new ("http://example.com/6/copy");

    return true;
}

function fourthFunc(ExternalClient exEpP2) returns boolean|error {
    ExternalClient temp = exEpP2;
    ExternalClient|error exEp10 = new ("http://example.com/10");

    if exEp10 is ExternalClient {
        temp = exEp10;
    }

    InternalClient inEp1 = check new ("http://example.com/internal/1");
    var localRes1 = check inEp1->getAll("");
    var localRes2 = check exEpOut->getAll("");

    return true;
}

service / on new http:Listener(9090) {
    InternalClient inEp2;

    function init() returns error? {
        self.inEp2 = check new InternalClient("http://example.com/internal/1");
    }

    resource function get repos(string orgName, int max = 5) returns string|error? {
        string|error? inRes = self.inEp2->getAll("service");
        return  inRes;
    }

    resource function post repos(string orgName) returns string|error? {
        ExternalClient exEp11 = check new ("http://example.com/11");
        int count = 0;
        if count > 5 {}
    }
}

function fifthFunc() {

}

service /next on new http:Listener(9090) {

    resource function get repos(string url) returns string|error? {
        return  url;
    }

}
