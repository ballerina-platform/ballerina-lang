import ballerina/io;

// Defines a object called 'Person'. It has a constructor which can be used to initialize<br/> the object, you can directly refer object fields as constructor params.
type Person object {
    public {
        int age,
        string name,
        string fullName,
    }

    private {
        string email = "default@abc.com",
        int[] marks,
    }

    new (age, name = "John", string firstname, string lastname = "Doe", int... scores) {
        marks = scores;
    }
};

function main (string... args) {
    // Initializing variable of object type Person
    Person p1 = new (5, "John", 4, 5);
    io:println(p1);

    Person p2 = new (5, "Adam", name = "Adam", lastname = "Page", 3);
    io:println(p2);
}
