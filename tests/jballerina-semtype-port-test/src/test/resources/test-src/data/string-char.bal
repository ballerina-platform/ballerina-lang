// C1<:C
// C1<:D
// C1<:S
// C<:C1
// C<:D
// C<:S
// D<:C
// D<:C1
// D<:S
type C string:Char;
type C1 string:Char;
type D C|C1;
type S string;
