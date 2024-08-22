@xmldata:Namespace {
    prefix: "ns1",
    uri: "example3.com"
}
type Ns1_Person record {
    int __text;
    @xmldata:Attribute
    string age;
    @xmldata:Attribute
    string city;
    @xmldata:Namespace {
        prefix: "ns1",
        uri: "example1.com"
    }
    @xmldata:Attribute
    string name;
};

type Ns2_Details record {
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "example.com"
    }
    string Info;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "example.com"
    }
    string Status;
    @xmldata:Attribute
    string number;
};

type Data record {
    @xmldata:Namespace {
        prefix: "ns1",
        uri: "example1.com"
    }
    Ns1_Person Person;
    @xmldata:Namespace {
        uri: "example2.com"
    }
    string Name;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "example.com"
    }
    Ns2_Details Details;
    @xmldata:Namespace {
        uri: "example2.com"
    }
    string Description;
};

@xmldata:Namespace {
    uri: "example2.com"
}
type Countries record {
    @xmldata:Namespace {
        uri: "example2.com"
    }
    Data Data;
};
