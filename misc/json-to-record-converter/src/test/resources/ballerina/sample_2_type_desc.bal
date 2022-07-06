type NewRecord record {
    string firstName;
    string lastName;
    record {
        string city;
        int zip;
        string country;
    } address;
    record {
        string firstName;
        string lastName;
        record {
            string city;
            string country;
            int zip?;
            int houseNo?;
        } address;
    } friend;
};
