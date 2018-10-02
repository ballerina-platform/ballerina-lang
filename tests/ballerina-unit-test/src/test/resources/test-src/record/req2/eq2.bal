public type userPB record {
    int age;
    string name;
    string address;
};

public type closedUserPB record {
    int age;
    string name;
    string address;
    !...
};