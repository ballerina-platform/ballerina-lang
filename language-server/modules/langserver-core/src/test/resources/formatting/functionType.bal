    function(string,string)returns(string) lambda =function (string x,string y)returns(string){
string r = x + y;return r;};

function name1 (function(string,string)returns(string) param) {
  string h = param("", "");
}

function name2 () {
    name1(function (string x,string y)returns(string){string r = x + y;
                  return r;});
}

function name3(
                 function (string, string) returns (string) param) {
    string h = param("", "");
}