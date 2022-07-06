type NewRecord record {
    string name;
    string school;
    int age;
    record {anydata[] donations; anydata subscription?;} contributions;
};
