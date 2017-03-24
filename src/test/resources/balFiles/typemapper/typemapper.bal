import ballerina.doc;

@doc:Description {value:"Converts JSON to a string"}
@doc:Param {value:"j: JSON value to be converted" }
@doc:Return {value:"int: String representation of the given JSON" }
typemapper xyz(string j) (int) {
    return 0;
}