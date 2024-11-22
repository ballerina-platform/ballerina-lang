type Title record {
    string \#content;
    @xmldata:Attribute
    string lang;
};

type Genre record {
    string \#content;
    @xmldata:Attribute
    string category;
};

@xmldata:Name {value: "book"}
type Book record {
    string author;
    Title title;
    Genre genre;
    decimal price;
    string publish_date;
    string description;
};
