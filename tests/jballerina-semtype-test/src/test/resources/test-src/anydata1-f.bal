type A anydata;
type TANY table<map<any>>;
// A is not a subtype of TANY and vice versa
// BUG #34696
