type User record {|
    string username;
    string firstName;
    string lastName;
    string email;
|};

type ExtendedUser record {|
    string username;
    string firstName;
    string lastName;
    string email;
|};

public function main() returns error? {
    ExtendedUser[] extusers = [];
    User[] users = extusers;
}
