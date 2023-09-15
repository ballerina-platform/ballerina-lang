type Codes record {
    (decimal|int|string)[] item;
};

@xmldata:Name {
    value: "bookstore"
}
@xmldata:Namespace {
    prefix: "ns0",
    uri: "http://sample.com/test"
}
type Bookstore record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Codes codes;
    @xmldata:Attribute
    string status;
};
