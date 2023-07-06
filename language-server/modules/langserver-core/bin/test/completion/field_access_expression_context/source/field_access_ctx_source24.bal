
type NodeCredential record {|
    string ip;
    int port;
    string username;
    string[] alias;
|};

class Node {
    private NodeCredential[] routingTable = [];
    private int|error response;

    function addNeighbor(NodeCredential credential) {
        self.response.
    }
}
