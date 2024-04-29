// @type F12 < F1
type F12 function(1|2);
type F1 function(1);

// @type F_ret1 < F_ret12
type F_ret12 function() returns 1|2;
type F_ret1 function() returns 1;
