// Test: using a reserved keyword (client) in a module import path without '^' escape
// should produce a diagnostic that includes the keyword in the module path,
// i.e. "cannot resolve module 'ballerinax/client.config as config'"
// NOT "cannot resolve module 'ballerinax/.config as config'"
import ballerinax/client.config;
