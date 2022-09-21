type NewRecord record {
    string id;
    string 'type;
    string name;
    decimal ppu;
    record {(decimal|int|record {string id; string 'type; boolean fresh?;}|string)[] batter;} batters;
    (record {string id; string 'type;}|string|record {string id;string 'type;}[]|string[])[] topping;
};
