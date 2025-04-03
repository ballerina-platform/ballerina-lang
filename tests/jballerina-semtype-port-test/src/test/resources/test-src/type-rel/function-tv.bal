type F function;

// @type F1 < F
// @type F1_bar < F
type F1 function(int);
type F1_bar function(int a);

// @type F2 < F
// @type F2_bar < F
type F2 function(int) returns boolean;
type F2_bar function(int a) returns boolean;

// @type F3 < F
// @type F3_bar < F
type F3 function(int...) returns boolean;
type F3_bar function(int... a) returns boolean;
