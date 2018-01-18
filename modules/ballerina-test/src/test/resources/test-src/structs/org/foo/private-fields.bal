package org.foo;

public struct user {
    int age;
    string name;
    string address;
    string zipcode = "23468";
}

public struct person {
    int age;
    string name;
    private:
        string ssn;
        int id;
}

struct student {
    int age;
    string name;
    private:
        string ssn;
        int id;
        int schoolId;
}

public function newPerson() (person) {
    return {age:12, name:"mad", ssn:"234-90-8887"};
}

public function newUser() (user) {
    return {age:56, name:"mal", zipcode:"23126"};
}
