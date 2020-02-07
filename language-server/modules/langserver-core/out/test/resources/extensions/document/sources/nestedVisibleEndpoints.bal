import ballerina/http;

public function nestedEPFunctionOne() {
      if (true) {
        if (true) {
            http:Client clientEPInNestedIf = new("");
            var resp = clientEPInNestedIf->get("");
        }
        while (true) {
            http:Client clientEPInNestedWhile = new("");
            var resp = clientEPInNestedWhile->get("");
        }
    }
}

public function nestedEPFunctionTwo() {
      while (true) {
        if (true) {
            http:Client clientEPInNestedIf = new("");
            var resp = clientEPInNestedIf->get("");
        }
        while (true) {
             http:Client clientEPInNestedWhile = new("");
             var resp = clientEPInNestedWhile->get("");
        }
    }
}




