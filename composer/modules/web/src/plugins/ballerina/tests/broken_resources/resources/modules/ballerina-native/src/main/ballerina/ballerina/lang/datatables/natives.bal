
import ballerina/doc;

@doc:Description { value:"Releases the database connection."}
@doc:Param { value:"dt: The datatable object" }
native function close (datatable dt);

@doc:Description { value:"Checks for a new row in the given datatable. If a new row is found, moves the cursor to it."}
@doc:Param { value:"dt: The datatable object" }
@doc:Return { value:"boolean: True if there is a new row; false otherwise" }
native function hasNext (datatable dt) (boolean);

@doc:Description { value:"Retrives the current row and return a struct with the data in the columns"}
@doc:Param { value:"dt: The datatable object" }
@doc:Return { value:"any: The resulting row as a struct" }
native function next (datatable dt) (any);


