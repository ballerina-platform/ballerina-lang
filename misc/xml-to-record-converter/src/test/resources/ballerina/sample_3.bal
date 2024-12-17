type Address record {
    string street;
    string city;
    string country;
};

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
};
