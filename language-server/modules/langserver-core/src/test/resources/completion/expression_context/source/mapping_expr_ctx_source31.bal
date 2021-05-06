type Address record {|
    string street;
    string city;
    string postalCode;
|};

type Person record {|
    string name;
    int age;
    Address address;
|};

class Country {
    function addPerson(string id, Person person, int timestamp) {

    }
}

public function func() {
    Country country = new ();
    country.addPerson(id = "1", person = {});
}
