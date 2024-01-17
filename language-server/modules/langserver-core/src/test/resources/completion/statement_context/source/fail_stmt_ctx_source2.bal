type XErr distinct error;
type YErr distinct error;

type Err XErr|YErr;
Err subErr = error XErr("test error");

function testFunction() {
    int testInt = 1;
    error testErr = error("test error");
    do {
        fail ;
    }
}
