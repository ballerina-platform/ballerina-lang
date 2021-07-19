import ballerina/module1;

public isolated service class testClass {
    table<Country> 
}

public type Country record {
    int code;
    string name;
};
