// Fun<:FunOrNever
// FunOrNever<:Fun
// Int<:IntOrNever
// IntOrNever<:Int
// Key<:KeyOrNever
// KeyOrNever<:Key
// Never<:Fun
// Never<:FunOrNever
// Never<:Int
// Never<:IntOrNever
// Never<:Key
// Never<:KeyOrNever
type Never never;
type Int int;
type Fun function(int);
type FunOrNever Fun|never;
type IntOrNever Int|never;
type Key "key";
type KeyOrNever Key|never;


