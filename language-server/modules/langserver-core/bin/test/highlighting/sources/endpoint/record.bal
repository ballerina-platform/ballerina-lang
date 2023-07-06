import ballerina/http;

type Student record {
    string name;
    int age;
    http:Client myClient = new ("");

    record {|
        string city;
        string country;
        http:Client myClient2;
    |} address;
};

