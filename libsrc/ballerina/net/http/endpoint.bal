package ballerina.net.http;

type HttpEndpoint {
  string uri;
}

// blocking calls
native function get (string path) (Message) throws exception;
native function put (string path, Message m) (Message) throws exception;
native function post (string path, Message m) (Message) throws exception;
native function delete (string path) (Message) throws exception;
native function executeMethod (string httpVerb, string path, Message m)
    (Message) throws exception;

// non-blocking method initiation
native function sendGet (HttpEndpoint e, string path) (int) throws exception;
native function sendGet (actor HttpEndpoint e, string path) (int) throws exception;
native action sendGet (HttpEndpoint e, string path) (int) throws exception;
native action sendGet (actor HttpEndpoint e, string path) (int) throws exception;

native function sendPut (string path) (int) throws exception;
native function sendPost (string path) (int) throws exception;
native function sendDelete (string path) (int) throws exception;
native function sendMethod (string httpVerb, string path, Message m)
    (int) throws exception;

// receive a non-blocking response
native function receiveResponse (int key) (Message) throws exception;
