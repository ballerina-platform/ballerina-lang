
@readonly public int globalInt = 10;

public type Person object {
    @readonly public int age = 10;
    public float salary = 100.5;
    public string name = "john";

    private int id = 50;
    private string ssn = "aaa";
};

public function createPerson() returns (Person) {
    return new Person();
}

