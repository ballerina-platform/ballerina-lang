
# Given the last used ID, this will return the next ID to be used
# for required purposes
#
# + lastId - Last used ID
# + return - Next ID to be used
public function generateId(int lastId) returns int {
    return lastId + 1;
}

# A class to perform encryption and decryption.
# Another line of comment
public class Encryptor {

    # Encrypts the provided plain text
    #
    # + plainText - plain text to be encrypted
    public function encrypt(string plainText, boolean padding) returns string {
        return "<encrypted>";
    }
}

# Returns a value given some string
#
# + content - Parameter Description
# + round - Parameter Description
# + return - Return Value Description
# # Deprecated
# Deprecated Description
function getValue(string content, boolean round) returns float {
    return 1.1;
}

# A service to obtain a greeting message
service / on new http:Listener(8080) {

    #  Given the name of a person, this will return the greeting to be used
    #
    # + name - Name of person
    # + return - Greeting message to be used
    resource function get greeting/[string name]() returns string {
        return "Hello, " + name;
    }
}

@MyAnnot
annotation varAnnotation;

# Description
public const annotation Data MyAnnot;

# Description
public type Data record {};
