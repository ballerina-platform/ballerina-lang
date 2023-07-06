type ColorsItem record {
    string name;
    int id;
};

type Dimensions record {
    int width;
    int height;
};

type NewRecord record {
    string name;
    boolean checked;
    string id;
    ColorsItem[] colors;
    Dimensions dimensions;
};

