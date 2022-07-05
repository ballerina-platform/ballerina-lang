type Person record {
    record {
        record {
            string country;
            string period;
            string name;
        } author;
        string name;
    }[] books;

    record {
        string number;
        string city;
        string street;
        string neighborhood;
        record {
            string code;
            string name;
        } state;
    } address;

    record {
        string position;
        string sport;
    }[] sports;

    string school;
    string year;
    string name;
    int age;
    boolean honors;
};
