import ballerina/io;

// Defines a object called 'Person'. It contains public and private fields along with their types. <br/>Optionally, for fields, the default value can be defined. If not, the default value for that type will get set.
type Person object {
    public {
        int age,
        string name,
    }
    private {
        string email = "default@abc.com",
    }
};

function main (string... args) {
    // There are three ways to initialize this object
    Person p1 = new;
    io:println(p1);

    Person p2 = new ();
    io:println(p2);

    // Below initialization is usefull when need to initialize different type in right hand side
    Person p3 = new Person();
    io:println(p2);
}
