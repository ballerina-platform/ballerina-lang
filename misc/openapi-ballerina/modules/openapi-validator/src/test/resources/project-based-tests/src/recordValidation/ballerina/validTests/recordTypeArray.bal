import ballerina/http;


type Tag record {
    int id;
    string name;
};
type RecordTypeArray record {
    int id;
    string category;
    string name;
    string photoUrls;
    Tag [] tags;
    string status;
};

service hello on new http:Listener(9090) {

    resource function sayHello(http:Caller caller,
        http:Request req, RecordTypeArray body ) returns error? {
    }
}
