@xmldata:Name {value: "item"}
type Item record {
    int ItemCode;
};

type OtherItem record {
    string ItemCode;
};

@xmldata:Name {value: "codes"}
type Codes record {
    (decimal|int|string)[] item;
    Item[] item;
    OtherItem OtherItem;
};

@xmldata:Name {value: "bookstore"}
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
