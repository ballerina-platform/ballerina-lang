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

type NewRecord record {|
    decimal ppu;
    Batters batters;
    string name;
    string id;
    string 'type;
    ToppingItem[] topping;
    json...;
|};

type NewRecordList record {|
    NewRecord[] newrecordlist;
    json...;
|};
