type Address record {|
    string city;
    string country;
|};

type Supplier record {
    Person supplier_details;
    string title;
    string food;
    string email;
    int id;
    Address supplier_address;
};

type User record {|
    Person user;
    string email;
    int ID;

|};

type Person record {
    string name;
    int age;
    int id;
};

public function main() {
    Supplier supplier_1 = {
        supplier_details: {
            name: "Amala",
            age: 35,
            id: 1234
        },
        title: "Mr.",
        food: "Rice",
        email: "abc@gmail.com",
        id: 1234,
        supplier_address:{
            city: "Gampaha",
            country: "Sri Lanka"
        }
    };

    User user_1 = supplier_1;

}
