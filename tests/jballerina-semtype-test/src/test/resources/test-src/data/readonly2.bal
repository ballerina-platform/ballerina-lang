// E<:ER
// ER<:E
// IR<:Int
// Int<:IR
// LR1<:LR
// LR1<:LR2
// LR2<:LR
// LR2<:LR1
// MR1<:MR
// MR1<:MR2
// MR2<:MR
// MR2<:MR1
type E error;
type ER error<map<readonly>>;
type LR readonly[];
type MR map<readonly>;
// These two should be equivalent
type MR1 readonly & map<any|error>;
type MR2 readonly & map<readonly>;
// As should these
type LR1 readonly & (any|error)[];
type LR2 readonly & readonly[];
// As should these
type IR int & readonly;
type Int int;

