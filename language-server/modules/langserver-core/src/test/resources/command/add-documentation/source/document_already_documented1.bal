
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
