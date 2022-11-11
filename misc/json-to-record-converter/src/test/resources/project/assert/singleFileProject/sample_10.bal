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

type NewRecord_01Item record {
    string id;
    string 'type;
    string name;
    decimal ppu;
    Batters_01 batters;
    Topping_01Item[] topping;
};

type NewRecord_01 record {
    NewRecord_01Item[] newRecord;
};
