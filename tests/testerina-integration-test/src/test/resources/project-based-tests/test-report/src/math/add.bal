# Adds two strings.
#
#
# + return - concatenated string
public function joinStrings() returns string {
    string a = "test1";
    string b = "test2";
    any c = 2;
    string result;
    if (c is string) {
        result = a+b+c;
    } else {
        result = a+b;
    }
    return result;
}
