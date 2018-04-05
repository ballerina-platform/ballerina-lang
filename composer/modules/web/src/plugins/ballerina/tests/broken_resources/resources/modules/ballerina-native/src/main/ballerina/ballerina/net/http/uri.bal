package ballerina.http;

import ballerina/doc;

@doc:Description { value:"Gets a query parameter value for a given key"}
@doc:Param { value:"m: The message object " }
@doc:Param { value:"key: The key of the query param to get from the message" }
@doc:Return { value:"string: The query parameter" }
native function getQueryParam (message m, string key) (string);

@doc:Description { value:"Encodes the given URL"}
@doc:Param { value:"url: URL to be encoded" }
@doc:Return { value:"string: The encoded url" }
native function encode (string url) (string);

