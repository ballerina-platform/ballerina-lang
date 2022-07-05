type X xml;
type RX readonly & X;
type RWX X & !readonly;

// @type RX_UNION_RO = X
type RX_UNION_RO RX | RWX;
