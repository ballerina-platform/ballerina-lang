
type NodeCredential record {|
    string ip;
    int port;
    string username;
    string[] alias;
|};

class Node {
    private NodeCredential[] routingTable = [];

    function addNeighbor(NodeCredential credential) {
        self.routingTable.
    }
}
