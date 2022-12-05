public type User record {
    readonly string userId;
    string name;
    string contactNo;
    string email;
    Profile profile;
};


public type Kid record {
    *User;
    string level;
    Adult guardian;
};


public type Adult record {
    *User;
    string occupation;
    string officeContactNo?;
};


public type Profile record {
    readonly string id;
    string username;
    AnuualSub|MonthlySub subscription;
};

type AnuualSub record {
    int id;
    int 'limit;
    float fee;
};

type MonthlySub record {
    int id;
    float fee;
};