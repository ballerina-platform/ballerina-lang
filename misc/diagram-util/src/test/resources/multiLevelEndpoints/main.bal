public client class ExternalClient {
    public string url;

    isolated function init(string url) returns error? {
        self.url = url;
    }

    remote isolated function getAll(@untainted string path) returns string|error? {
        return "Hello";
    }
}

ExternalClient exEp0 = new ("http://example.com/0");

public function main() returns error? {
    ExternalClient exEp1 = new ("http://example.com/1");

    var localRes = check exEp1->getAll("");
    var moduleRes = check exEp0->getAll("");
    boolean status = secondFunc(exEp1);

    if true {
        ExternalClient exEp2 = new ("http://example.com/2");
    }

    any functionResult = function() {
        ExternalClient exEp3 = new ("http://example.com/3");
        return ();
    };
}

function secondFunc(ExternalClient exEpP1) returns boolean {

    while exEp {
        ExternalClient exEp4 = new ("http://example.com/4");
    }

    do {
        ExternalClient exEp5 = new ("http://example.com/5");
    }

    ExternalClient exEp6 = new ("http://example.com/6");

    if false {
        string localRes2 = check exEp4->getAll("4");
    } else {
        ExternalClient exEp7 = new ("http://example.com/7");
    }

    return true;
}

function thirdFunc() returns boolean {
    ExternalClient exEp8 = new ("http://example.com/7");
    if true {
        ExternalClient exEp9 = new ("http://example.com/2");
    }

    return true;
}

function fourthFunc(ExternalClient exEpP2) returns boolean {
    var temp;
    temp = exEpP2;

    ExternalClient|error exEp10 = new ("http://example.com/2");

    return true;
}
