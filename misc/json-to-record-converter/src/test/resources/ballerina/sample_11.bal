type Contributions record {
    any[] donations;
    any subscription?;
};

type NewRecord record {
    string name;
    string school;
    int age;
    Contributions contributions;
};
