// E<:Cloneable
// E<:EM
// E<:ER
// EM<:Cloneable
// EM<:E
// EM<:ER
// ER<:Cloneable
// ER<:E
// ER<:EM
type E error;
type ER error<map<readonly>>;
type Cloneable readonly|xml|Cloneable[]|map<Cloneable>;
type EM error<map<Cloneable>>;

