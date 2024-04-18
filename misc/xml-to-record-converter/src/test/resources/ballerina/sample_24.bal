type Book_BookItem record {
    string[] authorName;
};

type Book_BookList record {
    Book_BookItem bookItem;
};

@xmldata:Name {value: "library"}
type Example_Library record {
    Book_BookList bookList;
};
