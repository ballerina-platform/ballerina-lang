import ballerina/http;

type ValidTypeMisMatchNestedArray record {
    int id;
    string username;
    string firstName;
    string lastName;
    string email;
    string password;
    int [][][][] phone;
    int userStatus;
};

service hello on new http:Listener(9090) {

    resource function sayHello(http:Caller caller,
        http:Request req, ValidTypeMisMatchNestedArray body ) returns error? {
    }
}

