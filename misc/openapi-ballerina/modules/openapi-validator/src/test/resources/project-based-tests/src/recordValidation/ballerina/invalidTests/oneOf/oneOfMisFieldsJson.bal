import ballerina/http;

type Pet record {
     int id;
     string name;
     string tag;
     string 'type;
};
type Dog record {
    *Pet;
     boolean bark;
};
type Cat record {
     int id;
     string name;
     string tag;
     string 'type;
     string place;
};
service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req, Cat| Dog| any body ) returns error? {
    }
}
