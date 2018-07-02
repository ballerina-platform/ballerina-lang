import ballerina/io;

// Defines an object called 'Person'. It has a constructor that can be used to initialize the object. You can directly refer to object fields as constructor params.
type Person object {
    public int age,
    public string name,
    public string fullName,

    private string email = "default@abc.com",
    private int[] marks,

    new(age, name = "John", string firstname,
        string lastname = "Doe", int... scores) {
        marks = scores;
    }
};

function main(string... args) {
    // Initializing variable of object type 'Person'
    Person p1 = new(5, "John", 4, 5);
    io:println(p1);

    Person p2 = new(5, "Adam", name = "Adam", lastname = "Page", 3);
    io:println(p2);
}
