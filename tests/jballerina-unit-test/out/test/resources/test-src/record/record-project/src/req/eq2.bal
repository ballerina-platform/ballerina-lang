public type userPFoo record {
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
};

public type closedUserPFoo record {|
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "23468";
|};