class Person {
    private string firstName;
    private string lastName;

    function init(string firstName, string lastName) {
        self.firstName = firstName;
        self.lastName = lastName;
    }

    function fullName() returns string {
        return self.firstName + self.lastName;
    }
}

function testFunction() {
    Person person = new("Elon", "Musk");
    string fullName = person.fullName();
}
