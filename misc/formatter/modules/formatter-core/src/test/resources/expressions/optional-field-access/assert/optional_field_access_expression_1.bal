type Address record {
    string city;
};

type Person record {
    string name;
    int age?;
    Address? addr;
};

public function foo() {
    Person p1 = {name: "Anne", addr: ()};
    int? age = p1?.age;
    string? city2 = p1?.addr?.city;
    string defaultCity = "San Jose";
    string city = city2 ?: defaultCity;
}
