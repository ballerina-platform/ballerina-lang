import ballerina/http;

type Pet record {
     int id;
     string name;
     string tag;
     string 'type;
};
type AllOfTest record {
    *Pet;
     boolean bark;
};
type Error record {
     int code;
     string message;
};
service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req, AllOfTest body ) returns error? {
    }
}
