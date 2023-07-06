
# Given the last used ID, this will return the next ID to be used
#
# + lastId - Last used ID
# + return - Next ID to be used
public function generateId(int lastId) returns int {
    return lastId + 1;
}
