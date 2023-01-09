type BatterItem record {
    string id;
    string 'type;
    boolean fresh?;
};

type Batters record {
    (BatterItem|decimal|int|string)[] batter;
};

type ToppingItem record {
    string id;
    string 'type;
};

type NewRecord record {
    string id;
    string 'type;
    string name;
    decimal ppu;
    Batters batters;
    (ToppingItem|string|ToppingItem[]|string[])[] topping;
};
