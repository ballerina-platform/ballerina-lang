package ballerina.lang.strings;


@Description { value:"Returns a string representation of an integer argument"}
@Param { value:"value: An integer argument" }
@Return { value:"string: String representation of the specified integer argument" }
public native function valueOf (any value) (string);