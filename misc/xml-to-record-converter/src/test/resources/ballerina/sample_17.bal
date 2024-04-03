type Title record {
    string \#content;
    @xmldata:Attribute
    string lang;
};

@xmldata:Name {value: "book"}
type Book record {
    string author;
    Title title;
    string genre;
    decimal price;
    string publish_date;
    string description;
};
