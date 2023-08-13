@xmldata:Name {
    value: "book"
}
type Book record {
    string author;
    string title;
    decimal price;
    @Attribute
    string id;
};

@xmldata:Name {
    value: "catalog"
}
type Catalog record {
    Book[] book;
};
