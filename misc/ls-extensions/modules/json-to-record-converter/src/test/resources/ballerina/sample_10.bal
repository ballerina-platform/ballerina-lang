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

type NewRecord record {
    decimal ppu;
    Batters batters;
    string name;
    string id;
    string 'type;
    ToppingItem[] topping;
};

type NewRecordList record {
    NewRecord[] newrecordlist;
};
