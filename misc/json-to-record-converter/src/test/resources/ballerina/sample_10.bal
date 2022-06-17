type BatterItem record {
    string id;
    string 'type;
};

type Batters record {
    BatterItem[] batter;
};

type ToppingItem record {
    string id;
    string 'type;
};

type NewRecordItem record {
    string id;
    string 'type;
    string name;
    decimal ppu;
    Batters batters;
    ToppingItem[] topping;
};

type NewRecordList record {
    NewRecordItem[] newRecord;
};
