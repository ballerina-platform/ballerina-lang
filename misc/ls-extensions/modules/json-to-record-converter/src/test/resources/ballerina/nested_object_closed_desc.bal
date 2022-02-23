type NewRecord record {|
    string name;
    boolean checked;
    string id;
    record {
        string name;
        int id;
    }[] colors;
    record {
        int width;
        int height;
    } dimensions;
|};
