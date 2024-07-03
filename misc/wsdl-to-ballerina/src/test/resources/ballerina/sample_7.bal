type Book record {
    (int|string) title;
    string author;
    string genre;
    int published_year;
    string isbn;
};

type Catalog record {
    Book[] book;
};

type Opening_hours record {
    string weekday;
    string hours;
};

type Library_info record {
    string name;
    string location;
    Opening_hours[] opening_hours;
};

@xmldata:Name {
    value: "library"
}
type Library record {
    Catalog catalog;
    Library_info library_info;
};
