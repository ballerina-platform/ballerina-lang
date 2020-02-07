import ballerina/http;

# Description
public type Person abstract object {
};

type Student object {
    *Person;
};