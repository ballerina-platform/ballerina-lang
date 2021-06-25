Model defaultModel = 

public type Person record {|
    string name;
    string country;
    int age;
|};

public type Driver record {|
    *Person;
    string drivingLicenceNo;
|};

public class Vehicle {
    private string brand;
    private Model model;
    private int year;

    public function init(string brand, Model model, int year) {
        self.brand = brand;
        self.model = model;
        self.year = year;
    }
}

public class Model {
    private string name;

    public function init(string name) {
        self.name = name;
    }
}

public function createVehicle(Model model) returns Vehicle {
    return new ("Tesla", model, 2020);
}

public function createVehicleModel(string name) returns Model {
    return new (name);
}
