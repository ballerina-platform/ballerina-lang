type myRecord record {
    int a;
    string...;
};

type myRecord2 record {
    T...;
};

type myRecord2 record {
    string b?;
    xml...;
    *myRecord2;
    float a = 4.5;
};
