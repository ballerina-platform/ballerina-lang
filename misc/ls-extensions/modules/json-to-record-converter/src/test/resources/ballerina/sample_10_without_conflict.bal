type BatterItem_01 record {
    string id;
    string 'type;
};

type Batters_01 record {
    BatterItem_01[] batter;
};

type ToppingItem_01 record {
    string id;
    string 'type;
};

type NewRecordItem record {
    string id;
    string' type;
    string name;
    decimal ppu;
    Batters_01 batters;
    ToppingItem_01[] topping;
};

type NewRecord_01 record {
    NewRecordItem[] newRecord;
};
