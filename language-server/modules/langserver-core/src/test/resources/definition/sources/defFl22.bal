import ballerina/http;

# Description
public type Person object {
};

type Student object {
    *Person;
};