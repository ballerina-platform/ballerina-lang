type Dimensions record {|
    int width;
    int height;
    json...;
|};

type NewRecord record {|
    boolean checked;
    Dimensions dimensions;
    int id;
    string name;
    json...;
|};

