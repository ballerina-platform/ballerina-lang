type Person record {

    record {
        decimal ppu;

        record {
            record {
                string id;
                string 'type;
            }[] batter;
        } batters;

        string name;
        string id;
        string 'type;

        record {
            string id;
            string 'type;
        }[] topping;
    }[] personlist;
};
