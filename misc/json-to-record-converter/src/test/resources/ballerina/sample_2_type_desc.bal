type NewRecord record {
    string firstName;
    string lastName;
    record {
        string city;
        string country;
        int zip?;
        int houseNo?;
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
