type User record {|
    string username;
    string firstName;
    string lastName;
    string email;
|};

public function main() returns error? {
    int[] ids = [1, 2, 3];
    User[] users = ids;
}
