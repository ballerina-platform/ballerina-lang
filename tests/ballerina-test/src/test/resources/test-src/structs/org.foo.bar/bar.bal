package org.foo.bar;

public type officer {
    int age;
    string name = "hidden, private";
    string address;
    string zipcode = "23468";
    private:
        int ssn;
}

public type userBar {
    int age;
    string name = "hidden, private";
    string address;
    string zipcode = "23468";
    private:
        int ssn;
}

public function <userBar ub> getName() returns (string) {
    return ub.name;
}

function <userBar ub> getAge() returns (int) {
    return ub.age;
}

function <userBar ub> getZipcode() returns (string) {
    return ub.zipcode;
}
