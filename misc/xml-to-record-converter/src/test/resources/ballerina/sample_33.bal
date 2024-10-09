@xmldata:Namespace {prefix: "a", uri: "[www.example.org/personAddress](http://www.example.org/personAddress)"}
type A_Address record {
    @xmldata:Namespace {prefix: "c", uri: "[www.example.org/addressCity](http://www.example.org/addressCity)"}
    string city;
    @xmldata:Namespace {prefix: "s", uri: "[www.example.org/addressState](http://www.example.org/addressState)"}
    string state;
    @xmldata:Namespace {prefix: "ct", uri: "[www.example.org/addressCountry](http://www.example.org/addressCountry)"}
    string country;
};

@xmldata:Name {value: "person"}
@xmldata:Namespace {prefix: "p", uri: "[www.example.org/personData](http://www.example.org/personData)"}
type P_Person record {
    @xmldata:Namespace {prefix: "n", uri: "[www.example.org/personName](http://www.example.org/personName)"}
    string name;
    @xmldata:Namespace {prefix: "a", uri: "[www.example.org/personAddress](http://www.example.org/personAddress)"}
    A_Address address;
    @xmldata:Attribute
    string age;
};
