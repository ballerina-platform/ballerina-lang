// F<:A
// S<:T

type F function() returns S;
type A function() returns any|error;

type S function(int?) returns string;
type T function(int) returns string?;
