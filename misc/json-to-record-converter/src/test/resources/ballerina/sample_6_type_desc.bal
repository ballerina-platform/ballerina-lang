type NewRecord record {
    string name;
    string school;
    int age;
    record {anydata sport; string position?; boolean reserve?; string game?; string 'type?;}[] sports;
    record {string name; record {string name; string country; anydata period; anydata language?;} author; int publishedYear?; decimal price?;}[] books;
    string year;
    boolean honors;
    record {string number; string street; string neighborhood; string city; record {string name; string code;} state;} address;
};
