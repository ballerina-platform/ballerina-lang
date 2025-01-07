// @type F1 < F2
type F1 function (typedesc<anydata> td) returns td;

type F2 function (typedesc<anydata> td) returns anydata;

// @type Fu1 < Fu2
type Fu1 function (typedesc<anydata> td) returns td|error;

type Fu2 function (typedesc<anydata> td) returns anydata|error;
