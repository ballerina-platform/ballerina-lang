type ANY any;

// @type F1 < ANY
// @type F_INT < ANY
// @type F_INT < F1
type F1 function(1);
type F_INT function(int);

// @type F1_ret < ANY
// @type F_INT_ret < ANY
// @type F1_ret < F_INT_ret
type F1_ret function() returns 1;
type F_INT_ret function() returns int;
