type NewRecord record {|
    record {|string id; string 'type; string name; decimal ppu; record {|record {|string id; string 'type; json...;|}[] batter; json...;|} batters; record {|string id; string 'type; json...;|}[] topping; json...;|}[] newRecord;
    json...;
|};
