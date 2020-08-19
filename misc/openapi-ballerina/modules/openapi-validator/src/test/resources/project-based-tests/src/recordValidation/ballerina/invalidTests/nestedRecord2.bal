import ballerina/http;

type Order record {
        int id;
        int petId;
        int quantity;
        string shipDate;
        string status;
        boolean complete;
};
type Category record {
    int id;
    string name;
};
type User record {
    string id;
    string username;
    string firstName;
    string lastName;
    string email;
    string password;
    string phone2;
    int userStatus;
};
type Tag record {
    int id;
    string name;
};
type NestedRecord record {
    int id;
    Category category;
    string name;
    string [] photoUrls;
    Tag [] tags;
    string status;
};

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller,
        http:Request req, NestedRecord body ) returns error? {
    }
}
