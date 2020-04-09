This module provides functions to encapsulate multiple body parts, such as attachments in a single message. The
 communication of such messages follow the MIME (Multipurpose Internet Mail Extensions) specification as specified in
  the [RFC 2045 standard](https://www.ietf.org/rfc/rfc2045.txt).

> Entity refers to the header fields and the content of a message, or a part of the body in a multipart entity. 

### Modify and retrieve the data in an entity
The module provides functions to set and get an entity body from different kinds of message types, such as XML, text, JSON, byte[], and body parts. Headers can be modified through functions such as `addHeader()`, `setHeader()`, `removeHeader()`, etc. 

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations in http requests with multiparts see [Request with multiparts BBE](https://ballerina.io/v1-2/learn/by-example/request-with-multiparts.html) and for handling responses with multiparts see [Response with multiparts BBE](https://ballerina.io/v1-2/learn/by-example/response-with-multiparts.html).
