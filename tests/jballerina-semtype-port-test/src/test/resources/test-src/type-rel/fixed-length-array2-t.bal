type IntArray int[];
type ISArray (int|string)[];
type ISTArray (1|2|3)[];

type Int4 int[4];
type Int1 int[1];
type Int14 Int4|Int1;
type NegInt14 (!Int14 & IntArray);

// -@type I4A = IntArray
// -@type I4A < ISArray
type I4A Int4|(!Int4 & IntArray);

type Int10000 int[100000];

// -@type ISTArray < I10000A
type I10000A Int10000|(!Int10000 & IntArray);

// -@type IA = IntArray
// -@type IA < ISArray
type IA Int14|NegInt14;

type IS int|string;
type EMPTY [];
type IS1 IS[1];
type IS2 IS[2];
type IS3 IS[3];

// @type ALL = ISArray
type ALL EMPTY|IS1|IS2|IS3|[IS, IS, IS, IS, IS...];

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