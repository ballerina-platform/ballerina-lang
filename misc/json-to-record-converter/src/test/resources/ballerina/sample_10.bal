type BatterItem record {|
    string id;
    string 'type;
    json...;
|};

type Batters record {|
    BatterItem[] batter;
    json...;
|};

type ToppingItem record {|
    string id;
    string 'type;
    json...;
|};

type NewRecordItem record {|
    string id;
    string 'type;
    string name;
    decimal ppu;
    Batters batters;
    ToppingItem[] topping;
    json...;
|};

type NewRecord record {|
    NewRecordItem[] newRecord;
    json...;
|};
