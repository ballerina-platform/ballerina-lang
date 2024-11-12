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

type Book_Book record {
    @xmldata:Namespace {
        prefix: "book",
        uri: "http://example.com/books"
    }
    Book_Title title;
    @xmldata:Namespace {
        prefix: "book",
        uri: "http://example.com/books"
    }
    Book_Author author;
    @xmldata:Namespace {
        prefix: "book",
        uri: "http://example.com/books"
    }
    string publish_date;
    @xmldata:Namespace {
        prefix: "book",
        uri: "http://example.com/books"
    }
    string description;
};

@xmldata:Name {value: "library"}
type Library record {
    @xmldata:Namespace {
        prefix: "book",
        uri: "http://example.com/books"
    }
    Book_Book book;
    string genre;
};
