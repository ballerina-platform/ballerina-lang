invalidtok configurable int a = 5;

54 configurable string b = "Dulmina";
configurable string b = && "Dulmina";
// configurable declaration with required expression
configurable string c = && ?;
configurable int d 543 = e + f;

final 5 configurable isolated int g = h;

// const declarations cannot be configurable
const configurable int i = 6;
// configurable variable must be initialized
configurable int j;
// isolated and final qualifiers are not allowed with configurable
final isolated configurable string f = ?;
final configurable isolated string g = ?;
configurable isolated string h = ?;

configurable // test recovery against EOF token
