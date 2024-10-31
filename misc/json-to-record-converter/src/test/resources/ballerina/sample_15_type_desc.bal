type NewRecord record {|
    string id;
    record {|
        string name;
        (record {|
            string id?;
            string description?;
            string cid?;
            json...;
        |}|string) value;
        json...;
    |}[] characteristic;
    json...;
|};
