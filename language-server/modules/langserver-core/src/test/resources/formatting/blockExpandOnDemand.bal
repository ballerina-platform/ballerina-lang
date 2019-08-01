type Address record {
    string city = "";
    string country = "";
}

type Person record {
    string name = "";
    int id = 0;
    Address address = {};
}

function testFunction() {
    json jw = {    "name": "Ballerina"};
    Person p1 = { name: "marcus",id: 0,address: {}};
    Person p2 = { name: "marcus",id: 0,address: { city: "colombo",country: "srilanka"}};
    Person p3 = { name: "marcus",id: 0,address: {city: "colombo",
    country: "srilanka"}
    };

    map<anydata> m = {var1: "A",
            var2: true};
        map<string> m2 = {var10: "B", var11: "C"};
}