type X xml;
type RX readonly & X;
type NEVER never;

type RWX X & !readonly;

type RX_MINUS_RO RX & RWX; // error: intersection must not be empty
