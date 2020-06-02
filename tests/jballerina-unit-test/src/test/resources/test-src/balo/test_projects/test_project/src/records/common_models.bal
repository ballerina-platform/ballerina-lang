type Name record {
    string name = "";
};

type AgedPerson record {
    *Name;
    int|string age = 127;
};
