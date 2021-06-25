import ballerina/module1;

type Foo record {|
    int f1;
    string f2?;
|};

public type Message record {|
    string message;
|};

public type Err error<Message>;

module1:TestRecord1 rec = {}

public type MyTable table<Foo> key(f1);

public type NodeCredential record {|
    string ip;
    int port;
    string username;
    stream files;
|};

public class Node {
  public function init(NodeCredential credentials, int myId, string & readonly ref) {

  }
}

type MyConfig record {|
    module1:TestRecord1 record1;
    Err err1;
    Foo record2;
    MyTable myTable;
    xml myXML;
    map<string> myMap;
    error myError;
    NodeCredential credentials;
    Node node;
    error | float randomNum;
    boolean isAlive;
    string[][] keywords;
    map<module1:TestRecord1> myMap;
|}

MyConfig config = {}
