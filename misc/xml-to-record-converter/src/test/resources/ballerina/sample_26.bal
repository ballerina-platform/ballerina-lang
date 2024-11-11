type Title record {
    string \#content;
    @xmldata:Attribute
    string lang;
};

type Price record {
    decimal \#content;
    @xmldata:Attribute
    string currency;
};

@xmldata:Name {value: "book"}
type Book record {
    int id;
    Title title;
    string author;
    string genre;
    Price price;
    string publication_date;
    string description;
};
