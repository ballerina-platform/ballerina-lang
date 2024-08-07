type Title record {
    string __text;
    string lang;
};

type Price record {
    decimal __text;
    string currency;
};

type Book record {
    Title title;
    string author;
    int year;
    Price price;
    string category;
};

@xmldata:Name {value: "bookstore"}
type Bookstore record {
    Book book;
};
