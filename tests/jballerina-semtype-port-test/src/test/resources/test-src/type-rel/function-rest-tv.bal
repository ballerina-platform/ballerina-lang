// @type FInt < F1
// @type FInt < F2
// @type FInt < F3
// @type F3 < F2
type FInt function(int...);
type F1 function(int);
type F2 function(int, int);
type F3 function(int, int, int...);
