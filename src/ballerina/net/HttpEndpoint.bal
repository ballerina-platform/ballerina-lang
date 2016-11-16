package ballerina.net;

native function setHeader (string name, value);
native function getHeader (string name) (string[] values);

native function get (string path) (Message) throws IOException;
native function put (string path, Message m) (Message) throws IOException;
native function post (string path, Message m) (Message) throws IOException;
native function delete (string path) (Message) throws IOException;
native function executeMethod (string httpVerb, string path, Message m)
    (Message) throws IOException;
