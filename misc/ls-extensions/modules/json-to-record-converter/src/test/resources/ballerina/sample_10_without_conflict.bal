type Batter_01Item record {
    string id;
    string 'type;
};

type Batters_01 record {
    Batter_01Item[] batter;
};

type Topping_01Item record {
    string id;
    string 'type;
};

type NewRecordItem record {
    string id;
    string' type;
    string name;
    decimal ppu;
    Batters_01 batters;
    Topping_01Item[] topping;
};

type NewRecord_01 record {
    NewRecordItem[] newRecord;
};
