class Person {
    int id;
    string name;

    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }
}

public client class PersonClient {
    remote function getRandomPerson(string name, int id) returns Person {
        return new Person(name,id);
    }
}

public function main() {
    PersonClient personClient = new ();
    Person person = personClient -> getRandomPerson(getRandomString(), 1)
}
