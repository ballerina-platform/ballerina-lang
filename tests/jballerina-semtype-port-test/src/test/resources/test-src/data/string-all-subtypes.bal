// A<:Bar
// A<:Baz
// A<:C
// A<:S
// A<:SC
// Bar<:Baz
// Bar<:S
// Bar<:SC
// Baz<:S
// Baz<:SC
// C<:Baz
// C<:S
// C<:SC
// Foo<:Bar
// Foo<:Baz
// Foo<:S
// Foo<:SC
// S<:Baz
// S<:SC
// SC<:Baz
// SC<:S

type S string;
type C string:Char;
type A "A";
type Foo "Foo";
type Bar A|Foo;
type Baz Bar|S|C;
type SC S|C;
