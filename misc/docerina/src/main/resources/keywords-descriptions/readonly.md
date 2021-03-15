A shape belongs to the type readonly if its read-only bit is on.

A value belonging to an inherently immutable basic type will always have its read-only bit on. These basic types are:

all simple types
- nil
- boolean
- int
- float
- decimal
- string
- error
- function
- typedesc
- handle

A value belonging to a selectively immutable basic type may have its read-only bit on. These basic types are:

- xml
- list
- mapping
- table
- object
