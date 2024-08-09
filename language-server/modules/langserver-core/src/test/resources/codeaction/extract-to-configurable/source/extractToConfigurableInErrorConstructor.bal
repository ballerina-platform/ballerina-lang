type XErr distinct error;
type YErr distinct error;

type Err XErr|YErr;
Err err = error XErr("Whoops!");
