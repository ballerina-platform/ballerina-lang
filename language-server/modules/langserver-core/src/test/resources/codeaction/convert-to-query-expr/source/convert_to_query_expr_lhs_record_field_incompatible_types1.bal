type User record {|
    string username;
    string firstName;
    string lastName;
    string email;
|};

type Name record {|
    string firstName;
    string lastName;
|};

type Response record {|
    Name[] names;
|};

public function main() returns error? {
    User[] users = [];
    Response r = {
        names: users
    }
}