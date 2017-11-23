package ballerina.user;

import ballerina.util;

@Description {value:"Returns the current user's country."}
@Return {value:"string: current user's country if it can be determined, an empty string otherwise"}
public native function getHome () (string);

@Description {value:"Returns the current user's home directory path."}
@Return {value:"string: current user's home directory if it can be determined, an empty string otherwise"}
public native function getName () (string);

@Description {value:"Returns the current user's language."}
@Return {value:"string: current user's language if it can be determined, an empty string otherwise"}
public native function getLanguage () (string);

@Description {value:"Returns the current user's name."}
@Return {value:"string: current user's name if it can be determined, an empty string otherwise"}
public native function getCountry () (string);

@Description {value:"Returns the current user's locale."}
@Return {value:"string: current user's locale"}
public native function getLocale () (util:Locale);
