type BatterItem_01 record {|
    string id;
    string 'type;
    json...;
|};

type Batters_01 record {|
    BatterItem_01[] batter;
    json...;
|};

type ToppingItem_01 record {|
    string id;
    string 'type;
    json...;
|};

type NewRecordItem_01 record {|
    string id;
    string 'type;
    string name;
    decimal ppu;
    Batters_01 batters;
    ToppingItem_01[] topping;
    json...;
|};

type NewRecord_01 record {|
    NewRecordItem_01[] newRecord;
    json...;
|};
