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
