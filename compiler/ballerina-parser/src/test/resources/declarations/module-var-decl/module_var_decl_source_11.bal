isolated string s1 = "http://hello1.com";

final isolated string s2 = "http://hello2.com";

isolated final string s3 = "http://hello3.com";

// -------------------------------------------
// Following are to test ambiguity resolution.
//--------------------------------------------

// First isolated keyword is belong to the module variable declaration
// where as second one is belong to the type descriptor
isolated isolated object {} a = b;

isolated isolated function () a = b;

// Isolated keyword is belong to the module variable declaration
isolated object {} a = b;

isolated function () a = b;

// Since following are non initialized module variable declarations, isolated keyword is belong to the type descriptor
isolated object {} a;

isolated function () a;
