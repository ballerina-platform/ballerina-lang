package ballerina.user;

import ballerina.doc;

@doc:Description {value:"Returns the current user's country."}
@doc:Return {value:"string: current user's country if it can be determined, an empty string otherwise"}
native function getHome () (string);

@doc:Description {value:"Returns the current user's home directory path."}
@doc:Return {value:"string: current user's home directory if it can be determined, an empty string otherwise"}
native function getName () (string);

@doc:Description {value:"Returns the current user's language."}
@doc:Return {value:"string: current user's language if it can be determined, an empty string otherwise"}
native function getLanguage () (string);

@doc:Description {value:"Returns the current user's name."}
@doc:Return {value:"string: current user's name if it can be determined, an empty string otherwise"}
native function getCountry () (string);
