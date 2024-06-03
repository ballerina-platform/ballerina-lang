@xmldata:Namespace {prefix: "n", uri: "[www.example.org/personName](http://www.example.org/personName)"}
type N_Name record {
    string __text;
    @xmldata:Attribute
    string n = "[www.example.org/personName](http://www.example.org/personName)";
};

@xmldata:Namespace {prefix: "c", uri: "[www.example.org/addressCity](http://www.example.org/addressCity)"}
type C_City record {
    string __text;
    @xmldata:Attribute
    string c = "[www.example.org/addressCity](http://www.example.org/addressCity)";
};

@xmldata:Namespace {prefix: "s", uri: "[www.example.org/addressState](http://www.example.org/addressState)"}
type S_State record {
    string __text;
    @xmldata:Attribute
    string s = "[www.example.org/addressState](http://www.example.org/addressState)";
};

@xmldata:Namespace {prefix: "ct", uri: "[www.example.org/addressCountry](http://www.example.org/addressCountry)"}
type Ct_Country record {
    string __text;
    @xmldata:Attribute
    string ct = "[www.example.org/addressCountry](http://www.example.org/addressCountry)";
};

@xmldata:Namespace {prefix: "a", uri: "[www.example.org/personAddress](http://www.example.org/personAddress)"}
type A_Address record {
    @xmldata:Namespace {prefix: "c", uri: "[www.example.org/addressCity](http://www.example.org/addressCity)"}
    C_City city;
    @xmldata:Namespace {prefix: "s", uri: "[www.example.org/addressState](http://www.example.org/addressState)"}
    S_State state;
    @xmldata:Namespace {prefix: "ct", uri: "[www.example.org/addressCountry](http://www.example.org/addressCountry)"}
    Ct_Country country;
};

@xmldata:Name {value: "person"}
@xmldata:Namespace {prefix: "p", uri: "[www.example.org/personData](http://www.example.org/personData)"}
type P_Person record {
    @xmldata:Namespace {prefix: "n", uri: "[www.example.org/personName](http://www.example.org/personName)"}
    N_Name name;
    @xmldata:Namespace {prefix: "a", uri: "[www.example.org/personAddress](http://www.example.org/personAddress)"}
    A_Address address;
    @xmldata:Attribute
    string age;
};
