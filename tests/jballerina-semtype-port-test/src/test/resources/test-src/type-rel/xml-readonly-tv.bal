type R readonly;
type X xml;

// @type N < R
type N xml<never>;

// @type T < R
// @type N < T
type T xml:Text;

// @type N = RN
type RN readonly & N;

// @type RO_E < R
type RO_E xml:Element & readonly;

// @type RO_C < R
type RO_C xml:Comment & readonly;

// @type RO_P < R
type RO_P xml:ProcessingInstruction & readonly;

// @type RX < R
// @type RX < X
type RX readonly & X;

// @type RX = RO_XML
type RO_XML xml<N|RO_E|RO_C|RO_P|T>;

// @type RO_XML_INTERSECTION = RO_XML
// @type RO_XML_INTERSECTION = RX
type RO_XML_INTERSECTION RO_XML & readonly;

