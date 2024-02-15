type Item record {
    int id;
    string name;
    decimal cost?;
    decimal price?;
    int quantity?;
    string code?;
    string description?;
    string discount?;
};

@xmldata:Name {
    value: "root"
}
type Root record {
    Item[] item;
};
