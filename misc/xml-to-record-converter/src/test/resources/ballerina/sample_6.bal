type Item record {
    (int|string) ItemCode;
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
type Bookstore record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Codes codes;
    @xmldata:Attribute
    string status;
};
