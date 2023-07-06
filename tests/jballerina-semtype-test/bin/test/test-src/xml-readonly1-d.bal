 type R readonly;
 type X xml;

// @type RO_E < R
type RO_E xml:Element & readonly;
// Compiler crashes for above intersections.
// BUG #34760

// @type RO_C < R
type RO_C xml:Comment & readonly;

// @type RO_P < R
type RO_P xml:ProcessingInstruction & readonly;

type RX readonly & X;

// @type RX = RO_XML
type RO_XML xml<N|RO_E|RO_C|RO_P|T>;

// @type RO_XML_INTERSECTION = RO_XML
// @type RO_XML_INTERSECTION = RX
type RO_XML_INTERSECTION RO_XML & readonly;
