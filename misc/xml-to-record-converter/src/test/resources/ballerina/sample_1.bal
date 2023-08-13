@xmldata:Name {
    value: "book"
}
type Book record {
    string author;
    string title;
    string genre;
    decimal price;
    string publish_date;
    string description;
    @Attribute
    string id;
};

@xmldata:Name {
    value: "catalog"
}
type Catalog record {
    Book book;
};
