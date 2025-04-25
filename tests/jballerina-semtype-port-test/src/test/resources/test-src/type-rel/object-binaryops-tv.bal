type TOP object {};
type O1 object {
    int a;
};

type O2 object {
    float b;
};

// @type O12 < TOP
// @type O1 < O12
// @type O2 < O12
type O12 O1|O2;

type O3 object {
    int a;
    float b;
    decimal c;
};

type O4 object {
    int a;
    float b;
    string c;
};

type O34 O3 & O4;

// @type OX < TOP
// @type O34 < OX
type OX object {
    int a;
    float b;
};
