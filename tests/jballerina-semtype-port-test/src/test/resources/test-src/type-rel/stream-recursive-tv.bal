// @type SR < S
type SR stream<SR?>;

type S stream;

// @type S1 < SR
// @type S1 < S
type S1 stream<()>;
