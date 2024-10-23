type NewRecord_01 record {|
    string id;
    string 'type;
    string name;
    decimal ppu;
    record {|(decimal|int|record {|string id; string 'type; boolean fresh?; json...;|}|string)[] batter; json...;|} batters;
    (record {|string id; string 'type; string color?; json...;|}|string|record {|string id; string 'type; string color?; json...;|}[]|string[])[] topping;
    record {|stringid ; string 'type; stringcolor? ; json...;|}[][] base;
    json...;
|};
