type Address record {
    string city;
    string state?;
    string streetAddress?;
};

type School record {
    string name;
    Address address;
};

type Work record {
    string name;
    Address address;
};

type NewRecord record {
    int salary;
    Address address;
    School school;
    Work work;
};
