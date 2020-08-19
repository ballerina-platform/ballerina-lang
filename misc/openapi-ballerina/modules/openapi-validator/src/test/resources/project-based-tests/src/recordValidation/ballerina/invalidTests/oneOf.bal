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
type Category record {
    int id;
    string name;
};
type Cat record {
     string id;
     string name;
     string tag;
     string 'type;
     Category category;
};
service hello on new http:Listener(9090) {

    resource function sayHello(http:Caller caller,
        http:Request req, Dog| Cat| any body ) returns error? {
    }
}
