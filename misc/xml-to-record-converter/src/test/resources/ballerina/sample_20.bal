type Book_Title record {
    string \#content;
    @xmldata:Attribute
    string edition;
    @xmldata:Attribute
    string lang;
};

type Book_Author record {
    string \#content;
    @xmldata:Attribute
    string gender;
    @xmldata:Attribute
    string nationality;
};

type Genre_Genre record {
    string \#content;
    @xmldata:Attribute
    string category;
    @xmldata:Attribute
    string 'type;
};

type Currency_Price record {
    string \#content;
    @xmldata:Attribute
    string currency;
    @xmldata:Attribute
    string discounted;
};

type Book_Book record {
    Book_Title title;
    Book_Author author;
    Genre_Genre genre;
    Currency_Price price;
    string publish_date;
    string description;
};

@xmldata:Name {value: "library"}
type Library record {
    Book_Book book;
};
