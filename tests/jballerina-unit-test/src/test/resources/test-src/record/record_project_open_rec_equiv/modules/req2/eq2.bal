public type userPB record {
    int age = 0;
    string name = "";
    string address = "";
};

public type closedUserPB record {|
    int age = 0;
    string name = "";
    string address = "";
    string zipcode = "";
|};