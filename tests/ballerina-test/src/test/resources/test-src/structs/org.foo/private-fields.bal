package org.foo;

public type userFoo {
    int age;
    string name;
    string address;
    string zipcode = "23468";
    string ssn;
};

public type user {
    int age;
    string name;
    string address;
    string zipcode = "23468";
};

public type person  {
    int age;
    string name;
    string ssn;
    int id;
};

type student {
    int age;
    string name;
    string ssn;
    int id;
    int schoolId;
};

public function newPerson() returns (person) {
    person p = {};
    p.age = 12;
    p.name = "mad";
    p.ssn = "234-90-8887";
    return p;
}

public function newUser() returns (user) {
    user u = {};
    u.age = 56;
    u.name = "mal";
    u.zipcode = "23126";
    return u;
}