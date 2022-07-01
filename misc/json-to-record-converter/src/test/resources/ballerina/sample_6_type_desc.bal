type NewRecord record {
    string name;
    string school;
    int age;
    record {string sport; string position;}[] sports;
    record {string name; record {string name; string country; string period;} author;}[] books;
    string year;
    boolean honors;
    record {string number; string street; string neighborhood; string city; record {string name; string code;} state;} address;
};
