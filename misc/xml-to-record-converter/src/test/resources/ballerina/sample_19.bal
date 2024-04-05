type Book_Author record {
    string name;
    string country;
};

type Meta_Reviews record {
    string[] review;
};

type Book_Book record {
    string title;
    Book_Author author;
    string genre;
    string publication_date;
    decimal rating;
    Meta_Reviews reviews;
};

@xmldata:Name {value: "library"}
type Library record {
    Book_Book book;
};
