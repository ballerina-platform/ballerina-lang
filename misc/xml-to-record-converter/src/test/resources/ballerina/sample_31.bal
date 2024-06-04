type St_Product record {
    string[] name;
};

type Mk_Brand record {
    string name;
    string counry;
    int year;
};

type Mk_Maker record {
    Mk_Brand[] brand;
};

type Pr_Cost record {
    int[] amount;
};

@xmldata:Name {value: "store"}
type Store record {
    St_Product product;
    Mk_Maker maker;
    Pr_Cost cost;
};
