package ballerina.net.uri;

import ballerina.doc;

@doc:Description { value:"Encodes the given URL"}
@doc:Param { value:"url: URL to be encoded" }
@doc:Return { value:"string: The encoded url" }
public native function encode (string url) (string);

