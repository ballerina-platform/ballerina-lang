
type X xml;

// @type X = Y
type Y xml<xml>;

// @type X = P
// @type Y = P
type P xml<xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text>;

// @type X = Q
type Q xml<P>;

// @type U < X
type U xml<xml:Element>;

// @type V < X
// @type U < V
type V xml<xml:Text|xml:Element>;

// @type N < X
// @type N < V
// @type N < U
type N xml<never>;

// @type N <> E
// @type E < V
// @type E < X
type E xml:Element;

// @type XE = X
type XE xml<P|E>;

// @type XEU = X
type XEU xml|E;

type T xml:Text;

// @type T = XT
type XT xml<T>;