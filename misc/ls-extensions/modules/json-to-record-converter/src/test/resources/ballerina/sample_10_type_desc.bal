type Person record {|

    record {|
        decimal ppu;

        record {|
            record {|
                string id;
                string 'type;
                json...;
            |}[] batter;
            json...;
        |} batters;

        string name;
        string id;
        string 'type;

        record {|
            string id;
            string 'type;
            json...;
        |}[] topping;
        json...;
    |}[] personlist;
    json...;
|};
