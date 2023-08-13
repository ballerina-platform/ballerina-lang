@xmldata:Name {
    value: "codes"
}
type Codes record {
    (decimal|int|string)[] item;
};

@xmldata:Name {
    value: "bookstore"
}
type Bookstore record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Codes codes;
    @Attribute
    string status;
    @Attribute
    string xmlns\:ns0;
};
