type NewRecord record {|
    record {|
        json[] donations;
        json subscription;
        json...;
    |} contributions;
    string school;
    string name;
    int age;
    json...;
|};
