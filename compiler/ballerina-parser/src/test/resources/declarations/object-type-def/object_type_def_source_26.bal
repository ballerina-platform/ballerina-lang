type foo service object {
    resource function get .() returns Person;
    resource function get elections/PE2020/Colombo(string elecName, string district, string div);
};
