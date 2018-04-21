
@readonly public int globalInt = 10;

public type Person object {
    public {
        @readonly int age = 10,
        float salary = 100.5,
        string name = "john";
    }

    private {
        int id = 50;
        string ssn = "aaa";
    }
};

public function createPerson() returns (Person) {
    return new Person();
}

