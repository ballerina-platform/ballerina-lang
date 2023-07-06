type foo service object {
    resource function get .() returns Person;
    resource function get elections/PE2020/Colombo(string elecName, string district, string div);
    resource function get elections/[int a]/Colombo/[float... b](string elecName, string district, string div);

    resource function get ["foo"|"bar"]() returns string;
    resource function get [string]() returns int;
    resource function get [string...]() returns int;
};
