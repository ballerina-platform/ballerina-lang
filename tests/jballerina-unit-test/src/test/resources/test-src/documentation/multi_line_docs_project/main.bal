import ballerina/jballerina.java;

# Returns a formatted string using the specified format string and arguments. Following format specifiers are allowed.
#
# b - boolean
#
# B - boolean (ALL_CAPS)
#
# d - int
#
# f - float
#
# x - hex
#
# X - HEX (ALL_CAPS)
#
# s - string (This specifier is applicable for any of the supported types in Ballerina.
#             These values will be converted to their string representation.)
#
# ```ballerina
# string s8 = io:sprintf("%s scored %d for %s and has an average of %.2f.", name, marks, subjects[0], average);
# ```
#
# + format - A format string
# + return - The formatted string
public function multilineDocsFunction(string format) returns string {
    return "hello " + format;
}

# Returns a formatted string using the specified format string and arguments. Following format specifiers are allowed.
#
# b - boolean
#
# B - boolean (ALL_CAPS)
#
# d - int
#
# f - float
#
# x - hex
#
# X - HEX (ALL_CAPS)
#
# s - string (This specifier is applicable for any of the supported types in Ballerina.
#             These values will be converted to their string representation.)
#
# ```ballerina
# string s8 = io:sprintf("%s scored %d for %s and has an average of %.2f.", name, marks, subjects[0], average);
# ```
#
# + format - A format string
# + return - The formatted string
# # Deprecated
@deprecated
public function deprecatedMultilineDocsFunction(string format) returns string {
    return "hello " + format;
}

public function main() {
    println(multilineDocsFunction("world"));
}

// helper functions

function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
