type Ns0_Address record {
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    string street;
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    string city;
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    string country;
};

type Ns0_Codes record {
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    int[] code;
};

@xmldata:Name {
    value: "bookStore"
}
@xmldata:Namespace {
    prefix: "ns0",
    uri: "http://sample.com/test"
}
type Ns0_BookStore record {
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    string storeName;
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    int postalCode;
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    boolean isOpen;
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    Ns0_Address address;
    @xmldata:Namespace {
        prefix: "ns0",
        uri: "http://sample.com/test"
    }
    Ns0_Codes codes;
    @xmldata:Attribute
    string status;
};
