package ballerina.net.file;

import ballerina.doc;

@doc:Description { value:"This function acknowledges to the message sender that processing of the file has finished."}
@doc:Param { value:"message: message" }
native function acknowledge (message m);

