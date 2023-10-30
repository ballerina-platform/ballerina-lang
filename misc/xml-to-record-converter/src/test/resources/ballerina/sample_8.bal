type Ns0Address record {
    string ns0\:street;
    string ns0\:city;
    string ns0\:country;
};

type Ns0Codes record {
    int[] ns0\:code;
};

@xmldata:Name {
    value: "bookStore"
}
@xmldata:Namespace {
    prefix: "ns0",
    uri: "http://sample.com/test"
}
type Ns0BookStore record {
    string ns0\:storeName;
    int ns0\:postalCode;
    boolean ns0\:isOpen;
    Ns0Address ns0\:address;
    Ns0Codes ns0\:codes;
    @xmldata:Attribute
    string status;
};
