import ballerina/http;

type TypeMisMatch record {
    string id;
    string username;
    string firstName;
    string lastName;
    string email;
    string password;
    string phone;
    int userStatus;
};

service hello on new http:Listener(9090) {

    resource function sayHello(http:Caller caller,
        http:Request req, TypeMisMatch body ) returns error? {

        //check caller->respond("Hello, World!");
    }
}

