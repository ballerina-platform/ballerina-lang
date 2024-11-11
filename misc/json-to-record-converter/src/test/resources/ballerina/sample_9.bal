type BatterItem record {|
    string id;
    string 'type;
    boolean fresh?;
    json...;
|};

type Batters record {|
    (BatterItem|decimal|int|string)[] batter;
    json...;
|};

type ToppingItem record {|
    string id;
    string 'type;
    string color?;
    json...;
|};

type BaseItem record {|
    string id;
    string 'type;
    string color?;
    json...;
|};

type NewRecord record {|
    string id;
    string 'type;
    string name;
    decimal ppu;
    Batters batters;
    (ToppingItem|string|ToppingItem[]|string[])[] topping;
    BaseItem[][] base;
    json...;
|};
