import ballerina/http;

type Pet record {
     int id;
     string name;
     string tag;
     string 'type;
};
type Dog record {
    *Pet;
     string bark;
};
type Cat record {
     string id;
     string name;
     string tag;
     string 'type;

};
service hello on new http:Listener(9090) {
    @http:ResourceConfig {
                methods:["POST"],
                path:"/pets/{petId}",
                body: "body"
                }
    resource function sayHello(http:Caller caller,
        http:Request req, Dog| Cat| any body ) returns error? {
    }
}
