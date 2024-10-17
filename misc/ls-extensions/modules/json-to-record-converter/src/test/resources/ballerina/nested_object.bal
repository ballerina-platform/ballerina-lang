type ColorsItem record {|
    string name;
    int id;
    json...;
|};

type Dimensions record {|
    int width;
    int height;
    json...;
|};

type NewRecord record {|
    string name;
    boolean checked;
    string id;
    ColorsItem[] colors;
    Dimensions dimensions;
    json...;
|};

