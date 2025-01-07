type F1 function(1|2);
type F2 function(2|3);

// @type F1 < F
// @type F2 < F
// @type F < Fx
type F F1|F2;
type Fx function(2);
