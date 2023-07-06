type A anydata;
type TERR table<map<error>>;
// A is not a subtype of TERR and vice versa
// BUG #34696
