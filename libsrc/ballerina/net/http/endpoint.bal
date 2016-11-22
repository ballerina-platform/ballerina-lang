package ballerina.net;

type HttpEndpoint {
  string uri;
}

native function setHeader (string name, value);
native function getHeader (string name) (string[] values);

// blocking calls
native function get (string path) (Message) throws IOException;
native function put (string path, Message m) (Message) throws IOException;
native function post (string path, Message m) (Message) throws IOException;
native function delete (string path) (Message) throws IOException;
native function executeMethod (string httpVerb, string path, Message m)
    (Message) throws IOException;

// non-blocking method initiation
native function sendGet (HttpEndpoint e, string path) (int) throws IOException;
native function sendGet (actor HttpEndpoint e, string path) (int) throws IOException;
native action sendGet (actor HttpEndpoint e, string path) (int) throws IOException;
native action sendGet (HttpEndpoint e, string path) (int) throws IOException;

native function sendPut (string path) (int) throws IOException;
native function sendPost (string path) (int) throws IOException;
native function sendDelete (string path) (int) throws IOException;
native function sendMethod (string httpVerb, string path, Message m)
    (int) throws IOException;

// receive a non-blocking response
native function receiveResponse (int key) (Message) throws IOException;
