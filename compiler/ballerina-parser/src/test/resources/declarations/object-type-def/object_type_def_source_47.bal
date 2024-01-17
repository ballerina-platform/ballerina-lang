type foo service object {
    resource function get () returns Person;
    resource function get /elections/PE2020/Colombo^(string elecName string div);
    resource function get elections+PE2020/Colombo(string elecName, );
    resource function get elections/[int (a]/Colombo/[float & ... b]();
    
    resource function get ["foo"|"bar"() returns string;
    resource function get ["foo"|"bar" &]() returns string;
    resource function get [string 7755] () returns int;
    resource function get [string 55 ...] () returns;
};
