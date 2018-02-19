package ballerina.user;

import ballerina.util;

@Description {value:"Returns the current user's country."}
@Return { value:"Current user's country if it can be determined, an empty string otherwise"}
documentation {
Returns the current user's country.
- #country Current user's country if it can be determined, an empty string otherwise
}
public native function getCountry () (string country);

@Description {value:"Returns the current user's home directory path."}
@Return { value:"Current user's home directory if it can be determined, an empty string otherwise"}
documentation {
Returns the current user's home directory path.
- #home Current user's home directory if it can be determined, an empty string otherwise
}
public native function getHome () (string home);

@Description {value:"Returns the current user's language."}
@Return { value:"Current user's language if it can be determined, an empty string otherwise"}
documentation {
Returns the current user's language.
- #lang Current user's language if it can be determined, an empty string otherwise
}
public native function getLanguage () (string lang);

@Description {value:"Returns the current user's name."}
@Return { value:"Current user's name if it can be determined, an empty string otherwise"}
documentation {
Returns the current user's name.
- #country Current user's name if it can be determined, an empty string otherwise
}
public native function getName () (string name);

@Description {value:"Returns the current user's locale."}
@Return { value:"Current user's locale"}
documentation {
Returns the current user's locale.
- #locale Current user's locale if it can be determined, an empty string otherwise
}
public native function getLocale () (util:Locale locale);
