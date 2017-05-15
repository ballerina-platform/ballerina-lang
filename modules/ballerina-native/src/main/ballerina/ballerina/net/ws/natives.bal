package ballerina.net.ws;

import ballerina.doc;

@doc:Description { value:"This pushes text from server to the the same client who sent the message."}
@doc:Param { value:"message: message" }
@doc:Param { value:"text: Text which should be sent" }
native function pushText (message m, string text);

