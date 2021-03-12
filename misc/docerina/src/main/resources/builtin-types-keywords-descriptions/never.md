The type descriptor never describes the type that does not contain any shapes. No value ever belongs to the never.

This can be useful to describe for the return type of a function, if the function never returns. It can also be useful as a type parameter. For example, xml<never> describes the an xml type that has no constituents, i.e. the empty xml value.
