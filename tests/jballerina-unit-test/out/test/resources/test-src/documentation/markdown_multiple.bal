import ballerina/http;

# Documentation for Tst struct
# + a - `field a` documentation
# + b - `field b` documentation
# + c - `field c` documentation
type Tst record {
    string a;
    string b;
    string c;
};

# Documentation for Test annotation
annotation Test Tst;

# Documentation for Test struct
# + a - struct `field a` documentation
# + b - struct `field b` documentation
# + c - struct `field c` documentation
type Test record {
    int a;
    int b;
    int c;
};

type Person record {
    string firstName;
    string lastName;
    int age;
    string city;
};

type Employee record {
    string name;
    int age;
    string address;
    any ageAny;
};

# PizzaService HTTP Service
service PizzaService on new http:MockListener(9090){

    # Check orderPizza resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function orderPizza(http:Caller conn, http:Request req) {
        http:Response res = new;
        checkpanic conn->respond(res);
    }

    # Check status resource.
    # + conn - HTTP connection.
    # + req - In request.
    resource function checkStatus(http:Caller conn, http:Request req) {
        http:Response res = new;
        checkpanic conn->respond(res);
    }
}
