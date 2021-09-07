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

public Class AnotherItem {
    string code;

    # Init method
    #
    # + code - Item code.
    public function init(string code) {
        self.code = code;
    }

    # Returns the code of the item.
    # + return - address.
    public function getCode() returns string {
        return self.code;
    }
}

# Deprecated function.
# # Deprecated
# This is a deprecated function.
@deprecated 
public function deprecatedFunction() {
    
}
