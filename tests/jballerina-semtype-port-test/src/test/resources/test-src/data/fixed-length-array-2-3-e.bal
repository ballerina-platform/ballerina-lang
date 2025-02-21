// EMPTY<:ISArray
// EMPTY<:U
// EMPTY<:V
// EMPTY<:W
// EMPTY<:X
// EMPTY<:Y
// EMPTY<:Z
// ISArray<:U
// ISArray<:V
// ISArray<:W
// ISArray<:X
// ISArray<:Y
// ISArray<:Z
// X<:Y
// X<:Z
// Y<:X
// Y<:Z
// Z<:X
// Z<:Y


type IS int|string;
type EMPTY [];
type ISArray IS[];

// @type ISArray < U
type U EMPTY|[IS|float, IS...];

// @type ISArray < V
type V EMPTY|[IS]|[IS, IS|float, IS...];

// @type ISArray < W
type W EMPTY|[IS]|[IS, IS|float]|[IS, IS, IS|float, IS...];

// @type ISArray < X
type X EMPTY|[IS]|[IS, IS|float]|[IS, IS, IS|float]|[IS, IS, IS, IS|float, IS...];

// @type ISArray < Y
type Y EMPTY|[IS, IS, IS, IS|float, IS...]|[IS, IS, IS|float]|[IS, IS|float]|[IS];

// @type ISArray < Z
type Z [IS, IS, IS, IS|float, IS...]|[IS, IS, IS|float]|[IS, IS|float]|[IS]|EMPTY;
