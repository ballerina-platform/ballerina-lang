@xmldata:Name {value: "book"}
type Book record {
    string title;
    string author;
    string genre;
    int published_year;
    string isbn;
};

@xmldata:Name {value: "catalog"}
type Catalog record {
    Book[] book;
};

@xmldata:Name {value: "opening_hours"}
type Opening_hours record {
    string weekday;
    string hours;
};

@xmldata:Name {value: "library_info"}
type Library_info record {
    string name;
    string location;
    Opening_hours[] opening_hours;
};

@xmldata:Name {value: "library"}
type Library record {
    Catalog catalog;
    Library_info library_info;
};
