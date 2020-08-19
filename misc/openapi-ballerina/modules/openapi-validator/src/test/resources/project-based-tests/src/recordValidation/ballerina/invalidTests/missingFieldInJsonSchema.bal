import ballerina/http;

type MissingFieldsInJsonSchema record {
    int id;
    string username;
    string firstName;
    string lastName;
    string email;
    string password;
    string phone2;
    int userStatus;
};

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req, MissingFieldsInJsonSchema body ) returns error? {
    }
}
