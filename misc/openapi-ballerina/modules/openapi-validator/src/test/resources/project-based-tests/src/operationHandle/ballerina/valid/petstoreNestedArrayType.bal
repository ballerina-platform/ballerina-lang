import ballerina/http;

type TypeMisMatchArrayType record {
    int id;
    string username;
    string firstName;
    string lastName;
    string email;
    string password;
    int [][][][] phone;
};
service hello on new http:Listener(9090) {
    @http:ResourceConfig {
            methods:["POST"],
            path:"/user",
            body: "body"
    }
    resource function sayHello(http:Caller caller,
        http:Request req, TypeMisMatchArrayType body ) returns error? {
    }
}
