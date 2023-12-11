type Item record {
    int ItemCode;
};

type OtherItem record {
    string ItemCode;
};

type Codes record {
    (Item|decimal|int|string)[] item;
    OtherItem otherItem;
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
