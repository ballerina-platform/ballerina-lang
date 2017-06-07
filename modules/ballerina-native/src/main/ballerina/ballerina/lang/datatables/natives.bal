package ballerina.lang.datatables;

import ballerina.doc;

@doc:Description { value:"Releases the database connection."}
@doc:Param { value:"dt: The datatable object" }
native function close (datatable dt);

@doc:Description { value:"Retrieves the string value of the designated column in the current row"}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Return { value:"string: The column value as a string" }
native function getString (datatable dt, any column) (string);

@doc:Description { value:"Retrieves the int value of the designated column in the current row for the given column type: date, time, or timestamp"}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Param { value:"type: Database table column type. Supported values are date, time, timestamp" }
@doc:Return { value:"int: The column value as a int" }
native function getIntWithType (datatable dt, any column, string type) (int);

@doc:Description { value:"Retrieves the integer value of the designated column in the current row"}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Return { value:"int: The column value as an integer" }
native function getInt (datatable dt, any column) (int);

@doc:Description { value:"Outputs the dataset in XML format as a stream. This function will add 'results' and 'result' if the root wrapper and row wrapper elements are not provided. "}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"rootWrapper: The root wrapper element" }
@doc:Param { value:"rowWrapper: The row wrapper element" }
@doc:Return { value:"xml: The resulting dataset in XML format with given root wrapper and row wrapper. The default will be results and result if the wrapper isn't provided. " }
native function toXml (datatable dt, string rootWrapper, string rowWrapper) (xml);

@doc:Description { value:"Retrieves the string value of the designated column in the current row. The value of type blob and binary columns will return as a Base64Encoded string."}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Return { value:"string: The column value as a string" }
native function getValueAsString (datatable dt, any column) (string);

@doc:Description { value:"Retrieves the float value of the designated column in the current row"}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Return { value:"float: The column value as a float" }
native function getFloat (datatable dt, any column) (float);

@doc:Description { value:"Retrieves the base64encoded string value of the designated column in the current row for the given column type: blob, clob, nclob, or binary"}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Param { value:"type: Database table column type. Supported values are blob, clob, nclob, binary." }
@doc:Return { value:"string: The column value as a string" }
native function getStringWithType (datatable dt, any column, string type) (string);

@doc:Description { value:"Outputs the dataset in JSON format as a stream."}
@doc:Param { value:"dt: The datatable object" }
@doc:Return { value:"json: The resulting dataset in JSON format" }
native function toJson (datatable dt) (json);

@doc:Description { value:"Checks for a new row in the given datatable. If a new row is found, moves the cursor to it."}
@doc:Param { value:"dt: The datatable object" }
@doc:Return { value:"boolean: True if there is a new row; false otherwise" }
native function hasNext (datatable dt) (boolean);

@doc:Description { value:"Retrives the current row and return a struct with the data in the columns"}
@doc:Param { value:"dt: The datatable object" }
@doc:Return { value:"any: The resulting row as a struct" }
native function next (datatable dt) (any);

@doc:Description { value:"Retrieves arrays values of a given column index."}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Return { value:"map): The column value as map" }
native function getArray (datatable dt, any key) (map);

@doc:Description { value:"Retrieves the Boolean value of the designated column in the current row"}
@doc:Param { value:"dt: The datatable object" }
@doc:Param { value:"column: The column position of the result as index or name" }
@doc:Return { value:"boolean: The column value as a Boolean" }
native function getBoolean (datatable dt, any column) (boolean);

