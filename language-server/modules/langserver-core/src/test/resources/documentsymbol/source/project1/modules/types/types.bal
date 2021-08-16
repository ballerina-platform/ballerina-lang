public enum UserType {
    ADMIN, CUSTOMER, SELLER
}

public enum STATE {
    SOUTH, WEST, NORTH, EAST
}

public type Address record {
    string addressLine1;
    string addressLine2;
    STATE state;
    int postalCode;
};

public type User object {
    public function getUserName() returns string;
    public function getEmail() returns string;
    public function getType() returns UserType;
    public function getAddress() returns Address;
};
