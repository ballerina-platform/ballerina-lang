import ballerina/jwt.signature;

function testVerifyJWTSignature () returns (boolean) {
    string data = "ewogICJhbGciOiAiUlMyNTYiLAogICJ0eXAiOiAiSldUIgp9.ewogICJzdWIiOiAiMTIzNjU0IiwKICAibmFtZSI6ICJK" +
                  "b2huIiwKICAiaXNzIjogIndzbzIiLAogICJhdWQiOiAiYmFsbGVyaW5hIiwKICAiZXhwIjogMTUxOTk5NDU2NDI0OQp9";
    string signature = "X10zu93zSfo0TJQdyDrWZEr5RfX-8vA3dNuxkVRhhj_v51Q7FQ2WUP_rQpJGL2VyFpu23W1ypXXGiDMqDZodqQ8v" +
                       "cf1ElO_qIC6ls0Ay6fHzjpLQdVU7bkFfpuqoboXfOSLCxwzHnvKNIWqmVBHW7CE4jPjb7_11QpT1CxwIUSXtVFk2" +
                       "Z3gpCyfwCVe_JXtBwDbyCQGO_g2tKUSwHvvNDu3THgCcB2ALIS_JznaK9iPf55YmeNwB_KRGkaY-VLvQ5iUILWp2" +
                       "J5SF3QavfXMNhv8GoEDBe2ZfbQgH5E-TpakoL51Ix8vELiznVl7sbtAqlD97440hW3wXoq68kboCVQ==";
    string algorithm = "RS256";
    string keyAlias = "ballerina";
    return signature:verify(data, signature, algorithm, keyAlias);
}

function testSignJWTData () returns (boolean) {
    string data = "ewogICJhbGciOiAiUlMyNTYiLAogICJ0eXAiOiAiSldUIgp9.ewogICJzdWIiOiAiMTIzNjU0IiwKICAibmFtZSI6ICJK" +
                  "b2huIiwKICAiaXNzIjogIndzbzIiLAogICJhdWQiOiAiYmFsbGVyaW5hIiwKICAiZXhwIjogMTUxOTk5NDU2NDI0OQp9";
    string signature = "X10zu93zSfo0TJQdyDrWZEr5RfX-8vA3dNuxkVRhhj_v51Q7FQ2WUP_rQpJGL2VyFpu23W1ypXXGiDMqDZodqQ8v" +
                       "cf1ElO_qIC6ls0Ay6fHzjpLQdVU7bkFfpuqoboXfOSLCxwzHnvKNIWqmVBHW7CE4jPjb7_11QpT1CxwIUSXtVFk2" +
                       "Z3gpCyfwCVe_JXtBwDbyCQGO_g2tKUSwHvvNDu3THgCcB2ALIS_JznaK9iPf55YmeNwB_KRGkaY-VLvQ5iUILWp2" +
                       "J5SF3QavfXMNhv8GoEDBe2ZfbQgH5E-TpakoL51Ix8vELiznVl7sbtAqlD97440hW3wXoq68kboCVQ==";
    string algorithm = "RS256";
    return signature == signature:sign(data, algorithm, "ballerina", "ballerina");
}
