type Book record {
    string author;
    string title;
    decimal price;
    @xmldata:Attribute
    string id;
};

@xmldata:Name {
    value: "catalog"
}
type Catalog record {
    Book[] book;
};
