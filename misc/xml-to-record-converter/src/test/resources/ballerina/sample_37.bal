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
    Book_Title title;
    string author;
    string publisher;
    string publish_date;
    int isbn;
    Book_Price price;
    string available;
    string id;
    string shelf;
};

type Books record {
    string genre;
    Book_Book book;
};

@xmldata:Name {value: "library"}
type Library record {
    Books books;
    string established;
    string genre;
    string location;
};
