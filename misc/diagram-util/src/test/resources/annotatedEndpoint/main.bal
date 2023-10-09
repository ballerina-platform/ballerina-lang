import ballerina/http;

service /user on new http:Listener(9090) {

    http:Client httpEp;

    private http:Client httpEpPvt;

    @display {
        label: "Annotated Http Endpoint"
    }
    http:Client httpEpAnt;

    @display {
        label: "Annotated Private Http Endpoint"
    }
    private http:Client httpEpAntPvt;

    @display {
        label: "Annotated Internal Client Endpoint",
        id: "InternalClient-b704fd5e-06b8-47df-89fe-6c462b5cdf4e"
    }
    public InternalClient internalEpAnt;

    function init() returns error? {
        self.httpEp = check new (url = "");
        self.httpEpPvt = check new (url = "");
        self.httpEpAnt = check new (url = "");
        self.httpEpAntPvt = check new (url = "");

        self.internalEpAnt = check new InternalClient("");
    }

    resource function post .() returns error? {

    }
}

// Internal client ( connector )
public client class InternalClient {
    public string url;

    isolated function init(string url) returns error? {
        self.url = url;
    }

    remote isolated function getAll(@untainted string path) returns string|error? {
        return "Hello";
    }
}
