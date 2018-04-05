package org.foo;

public type userFoo {
    int age;
    string name;
    string address;
    string zipcode = "23468";
}

public function <userFoo u> getName() returns (string) {
    return u.name;
}

public function <userFoo u> getAge() returns (int) {
    return u.age;
}

public type user {
    int age;
    string name;
    string address;
    string zipcode = "23468";
}

public type person {
    int age;
    string name;
    private:
        string ssn;
        int id;
}

type student {
    int age;
    string name;
    private:
        string ssn;
        int id;
        int schoolId;
}

public function newPerson() returns (person) {
    return {age:12, name:"mad", ssn:"234-90-8887"};
}

public function newUser() returns (user) {
    return {age:56, name:"mal", zipcode:"23126"};
}
