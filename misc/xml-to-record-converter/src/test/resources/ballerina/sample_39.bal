type Book record {
    string title;
    string author;
    int year;
    decimal price;
};

@xmldata:Name {value: "bookstore"}
type Bookstore record {
    Book book;
};
