public class Person {
    int id;
    string name;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
}

public client class PersonClient {
    remote function getRandomPerson(string name, int id) returns Person|error {
        if verify(name) {
            return new Person(name, id);
        }
        return error("Invalid name");
    }
}

function verify(string name) returns boolean {
    return true;
}
