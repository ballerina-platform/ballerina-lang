type Bdd Node|boolean;

// @type Node < Bdd
type Node readonly & record {|
    int atom;
    Bdd left;
    Bdd middle;
    Bdd right;
|};
