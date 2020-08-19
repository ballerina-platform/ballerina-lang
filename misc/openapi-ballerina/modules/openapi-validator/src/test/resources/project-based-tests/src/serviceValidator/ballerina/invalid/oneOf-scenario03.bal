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
     string place02;
     string mealType;
     boolean canFly;
};
type Bird record {
     string birdRegion;
     boolean canFly;
};
service hello on new http:Listener(9090) {
    @http:ResourceConfig {
            methods:["POST"],
            path:"/oneOfRequestBody",
            body: "body"
        }
    resource function sayHello(http:Caller caller,
        http:Request req, Dog| Bird| any body ) returns error? {
    }
}
