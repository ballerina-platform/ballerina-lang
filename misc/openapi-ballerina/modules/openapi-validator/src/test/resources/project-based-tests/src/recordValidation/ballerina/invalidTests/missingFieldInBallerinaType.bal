import ballerina/http;

type MissingFieldInBallerinaType record {
    int id;
    string username;
    string firstName;
    string lastName;
    string email;
    string password;
    string phone;
    int userStatus;
};

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req, MissingFieldInBallerinaType body ) returns error? {
    }
}

