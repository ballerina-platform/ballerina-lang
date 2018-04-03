package org.bar;

public type Person object {
    public {
        @readonly int age = 10,
        string name = "john";
    }

    private {
        int id = 50;
        string ssn = "aaa";
    }
}

public function createPerson() returns (Person) {
    return new Person();
}

