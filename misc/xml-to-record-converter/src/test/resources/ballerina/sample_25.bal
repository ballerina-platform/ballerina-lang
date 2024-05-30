type Book_Title record {
    string __text;
    @xmldata:Attribute
    string edition;
    @xmldata:Attribute
    string lang;
};

type Book_Author record {
    string __text;
    @xmldata:Attribute
    string gender;
    @xmldata:Attribute
    string nationality;
};

type Book_Book record {
    Book_Title title;
    Book_Author author;
    string publish_date;
    string description;
};

type Books record {
    string genre;
    Book_Book book;
};

@xmldata:Name {value: "library"}
type Library record {
    Books books;
    @xmldata:Attribute
    string genre;
};
