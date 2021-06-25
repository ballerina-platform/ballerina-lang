type Address record {
    string city;
    string state;
};

type Person record {
    string name;
    Address addr;
};

public function main() {
    Person p1 = {name: "Anne", addr: {city: "", state: ""}};
    
    string? s = p1.addr.s
}