type NewRecord record {|
    string name;
    boolean checked;
    string id;

    record {|
        string name;
        int id;
        json...;
    |}[] colors;

    record {|
        int width;
        int height;
        json...;
    |} dimensions;
    json...;
|};
