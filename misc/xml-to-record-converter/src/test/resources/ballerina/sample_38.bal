type Book_Title record {
    string __text;
    string edition;
    string format;
    string lang;
    string pages;
};

type Book_Price record {
    decimal __text;
    string currency;
};

type Book_Book record {
    @xmldata:Namespace {prefix: "book", uri: "http://example.com/books"}
    Book_Title title;
    @xmldata:Namespace {prefix: "book", uri: "http://example.com/books"}
    string author;
    @xmldata:Namespace {prefix: "book", uri: "http://example.com/books"}
    string publisher;
    @xmldata:Namespace {prefix: "book", uri: "http://example.com/books"}
    string publish_date;
    @xmldata:Namespace {prefix: "book", uri: "http://example.com/books"}
    int isbn;
    @xmldata:Namespace {prefix: "book", uri: "http://example.com/books"}
    Book_Price price;
    string available;
    string id;
    string shelf;
};

type Books record {
    string genre;
    @xmldata:Namespace {prefix: "book", uri: "http://example.com/books"}
    Book_Book book;
};

@xmldata:Name {value: "library"}
type Library record {
    Books books;
    string established;
    string genre;
    string location;
};
