type Contributions record {
    anydata[] donations;
    anydata subscription?;
};

type NewRecord record {
    string name;
    string school;
    int age;
    Contributions contributions;
};
