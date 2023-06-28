type Company record {
    string name;
    string city;
};

type Employee record {
    string name;
    string age;
};

public isolated client class EmpDetails {
    isolated remote function addDetails(Company company, Employee employee) {

    }
}

public function test() {
    EmpDetails details = new();
    details->addDetails({city: "Colombo", name: "WSO2"}, {name: "John", age: 24});
}
