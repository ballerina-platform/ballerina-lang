type N xml<never>;

// @type N = NS
type NS xml<N>;

// @type N <> E
type E xml:Element;

// @type N < T
type T xml:Text;

// @type N <> C
type C xml:Comment;

// @type N <> P
type P xml:ProcessingInstruction;

// @type N < ES
type ES xml<xml:Element>;

// @type N < CS
type CS xml<xml:Comment>;

// @type N < TS
type TS xml<xml:Text>;

// @type N < PS
type PS xml<xml:ProcessingInstruction>;

// @type ENS = ES
type ENS xml<xml:Element|never>;
