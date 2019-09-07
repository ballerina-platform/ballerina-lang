public type Pet record { 
    int id;
    string name;
    string tag;
    string 'type;
};
public type Dog record { 
    boolean bark;
};
public type Pets record { 
    Pet[] pet;
};
public type Error record { 
    int code;
    string message;
};
