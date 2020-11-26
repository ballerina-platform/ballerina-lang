public function func()  {
    string[] args = <@tainted> ["hello", "taint", "analyzer"];
    secureFunction(args[0]);
    secureFunction(args[0] + "");
}

function secureFunction (@untainted string sensitiveInput) {

}
