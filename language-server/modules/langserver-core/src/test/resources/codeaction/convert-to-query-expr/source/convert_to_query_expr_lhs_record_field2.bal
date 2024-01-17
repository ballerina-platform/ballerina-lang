type User record {|
    string username;
    string firstName;
    string lastName;
    string email;
|};

type UserList record {|
    User[] list;
    int count;
|};

public function main() returns error? {
    User[] users = [];
    UserList list = {
        count: users.length(),
        list: []
    };
    list.list = users;
}
