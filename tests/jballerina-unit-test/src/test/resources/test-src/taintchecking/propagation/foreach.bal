const FORWARD_SLASH = "/";
const EMPTY_STRING = "";

public function main (string... args) {
    string[] fruits = ["apple", "banana", "cherry"];

    foreach var v in fruits {
        secureFunction(v, v);
    }
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

function prepareUrl(string[] paths) returns string {
    string url = EMPTY_STRING;

    if (paths.length() > 0) {
        foreach var path in paths {
            if (!path.startsWith(FORWARD_SLASH)) {
                url = url + FORWARD_SLASH;
            }
            url = url + path;
        }
    }
    secureFunction(url, "");
    return url;
}

function usePrepared() {
    string[] strs = [EMPTY_STRING];
    string p = prepareUrl(strs);
    secureFunction(p, p);
}
