
# The function to retrieve the string representation of the Ballerina class mapping the `FULL_CLASS_NAME` Java CLASS_TYPE.
#
# + return - The `string` form of the Java object instance.
ACCESS_MODIFIERfunction toString() returns string {
    return java:toString(self.jObj) ?: "";
}
