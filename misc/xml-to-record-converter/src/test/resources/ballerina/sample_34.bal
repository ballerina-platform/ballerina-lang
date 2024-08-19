@xmldata:Namespace {prefix: "n", uri: "http://www.example.org/personName"}
type N_Name record {
    string __text;
    @xmldata:Attribute
    string 'type;
};

@xmldata:Namespace {prefix: "p", uri: "http://www.example.org/personData"}
type P_Address record {
    @xmldata:Namespace {prefix: "p", uri: "http://www.example.org/personData"}
    string street;
    @xmldata:Namespace {prefix: "p", uri: "http://www.example.org/personData"}
    string city;
};

@xmldata:Namespace {prefix: "p", uri: "http://www.example.org/personData"}
type P_Occupation record {
    string __text;
    @xmldata:Attribute
    string country;
};

@xmldata:Name {value: "person"}
@xmldata:Namespace {prefix: "p", uri: "http://www.example.org/personData"}
type P_Person record {
    @xmldata:Namespace {prefix: "n", uri: "http://www.example.org/personName"}
    N_Name name;
    @xmldata:Namespace {prefix: "p", uri: "http://www.example.org/personData"}
    P_Address address;
    @xmldata:Namespace {prefix: "p", uri: "http://www.example.org/personData"}
    P_Occupation occupation;
    @xmldata:Attribute
    string age;
};
