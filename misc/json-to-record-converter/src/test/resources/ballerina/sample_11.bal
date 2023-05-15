type Special\ object record {
    string name;
    int age;
};

type Special\\array\-\?Item record {
    string date;
    int value;
    string 'type?;
    string[] \2\ favourite\-colors?;
};

type NewRecord record {
    string first\ Name;
    string last\+Name\?;
    string \007;
    int ϼ\ \+\-\+;
    boolean ōŊĖ;
    Special\ object special\ object;
    (Special\\array\-\?Item[]|Special\\array\-\?Item[][])[] special\\array\-\?;
};
