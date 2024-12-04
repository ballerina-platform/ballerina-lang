// EMPTY<:IntArray
// EMPTY<:P
// EMPTY<:Q
// EMPTY<:R
// EMPTY<:S
// EMPTY<:T
// EMPTY<:T1
// IntArray<:P
// IntArray<:Q
// IntArray<:R
// IntArray<:S
// IntArray<:T
// IntArray<:T1
// P<:Q
// P<:T
// P<:T1
// R<:Q
// S<:Q
// S<:R
// S<:T1
// T1<:Q
// T<:Q

type IntArray int[];
type IS int|string;
type EMPTY [];

// @type IntArray < P
type P EMPTY|[int]|[IS, int]|[IS, IS, IS, IS...];

// @type IntArray < Q
type Q EMPTY|[int]|[IS, IS]|[IS, IS, IS|float, IS...];

// @type IntArray < R
type R EMPTY|[int]|[IS, IS]|[int, int, IS|float, IS...];

// @type IntArray < S
type S EMPTY|[int]|[IS, IS]|[int, int, int, IS...];

// @type IntArray < T
type T EMPTY|[int]|[IS, int]|[IS, IS, IS|float, IS...];

// @type IntArray < T1
type T1 EMPTY|[int]|[IS, IS, string...]|[IS, IS, IS, IS...];
