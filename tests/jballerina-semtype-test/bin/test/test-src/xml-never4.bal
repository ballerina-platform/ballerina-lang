// N<:PS
// ES<:ENS
// ENS<:ES
// N<:ENS
// N<:ES

type PS xml<xml:ProcessingInstruction>;

type ENS xml<xml:Element|never>;

type ES xml<xml:Element>;

type N xml<never>;
