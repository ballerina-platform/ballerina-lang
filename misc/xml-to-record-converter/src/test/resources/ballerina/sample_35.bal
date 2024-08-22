@xmldata:Namespace {
    prefix: "ns2",
    uri: "example2.com"
}
type Ns2_Person record {
    int __text;
    @xmldata:Attribute
    string age;
    @xmldata:Namespace {
        prefix: "ns2",
        uri: "example2.com"
    }
    @xmldata:Attribute
    string name;
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
    string Message;
};

@xmldata:Namespace {
    uri: "example.com"
}
type AQ record {
    @xmldata:Namespace {
        uri: "example.com"
    }
    Data Data;
};
