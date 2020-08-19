import ballerina/http;

type FourNestedComponent record {
     int id;
     string shipDate;
     Category category;
};
type Category record {
     string name;
     Tag tag;
};
type Tag record {
     int id;
     string name;
     Year date;
};
type Year record {
     int month;
     int year2;
};

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req, FourNestedComponent body ) returns error? {
    }
}
