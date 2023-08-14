@xmldata:Name {
    value: "address"
}
type Address record {
    string street;
    string city;
    string country;
};

@xmldata:Name {
    value: "codes"
}
type Codes record {
    int[] item;
};

@xmldata:Name {
    value: "bookstore"
}
type Bookstore record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Address address;
    Codes codes;
    @xmldata:Attribute
    string status;
    @xmldata:Attribute
    string xmlns\:ns0;
};
