// Boolean<:RO
// Decimal<:RO
// Error<:RO
// Float<:RO
// Handle<:RO
// Int<:RO
// Nil<:RO
// String<:RO
type RO readonly;
type Nil ();
type String string;
type Boolean boolean;
type Int int;
type Float float;
type Decimal decimal;
type Error error;
type Handle handle;
type List (any|error)[];
type Mapping map<any|error>;
