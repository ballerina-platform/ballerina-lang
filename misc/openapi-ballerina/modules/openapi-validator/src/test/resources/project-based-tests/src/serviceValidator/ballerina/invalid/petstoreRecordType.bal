import ballerina/http;


type Tag record {
    int id;
    string name;
};
type RecordTypeArray record {
    string id;
    string category;
    string name;
    string photoUrls;
    Tag [] tags;
    string status;
};

service hello on new http:Listener(9090) {
    @http:ResourceConfig {
        methods:["POST"],
        path:"/pet",
        body: "body"
    }
    resource function sayHello(http:Caller caller,
        http:Request req, RecordTypeArray body ) returns error? {
    }
}
