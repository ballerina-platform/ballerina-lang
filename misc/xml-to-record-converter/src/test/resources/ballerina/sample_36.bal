@xmldata:Namespace {
    prefix: "ns2",
    uri: "example3.com"
}
type Ns2_Person record {
    int __text;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "example2.com"
    }
    @xmldata:Attribute
    string age;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "example2.com"
    }
    @xmldata:Attribute
    string name;
};

type Ns1_Details record {
    @xmldata:Namespace {
        prefix: "ns1",
        uri: "example1.com"
    }
    string Info;
    @xmldata:Namespace {
        prefix: "ns1",
        uri: "example1.com"
    }
    string Status;
};

type Data record {
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "example2.com"
    }
    Ns2_Person Person;
    @xmldata:Namespace {
        uri: "example.com"
    }
    string C;
    @xmldata:Namespace {
        prefix: "ns1",
        uri: "example1.com"
    }
    Ns1_Details Details;
    @xmldata:Namespace {
        uri: "example.com"
    }
    string B;
};

@xmldata:Namespace {
    uri: "example.com"
}
type Test record {
    @xmldata:Namespace {
        uri: "example.com"
    }
    Data Data;
};
