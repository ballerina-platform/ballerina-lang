type F1 function(1|2|3);
type F2 function(2|3|4);

// @type F < F1
// @type F < F2
type F F1&F2;
