# Documentation and Comments

Many programming languages treat documentation as an after thought with some languages using the approach of introducing structure to comments to insert structured documentation into the language.

Ballerina has a structured approach for both documentation and random comments which are not related to documenting per se but simply commenting about the logic of the program.

## Documenting with Annotations

Ballerina uses annotations for documentation. It comes with a set of standard annotations which must be used to document all the Ballerina constructs. These annotations and then processed by the `docerina` tool to create structured documents that describes the Ballerina code in a standardized way.

> NOTE: To be written to explain docerina architecture. See docerina architecture document in the meantime.

## Commenting Code

Comments are quite different in Ballerina in comparison to other languages. Ballerina allows one to insert comments any place where statements are valid.  Any statement that starts with the characters `//` is considered a comment until the end of the line. Note that Ballerina does not have any provision for placing comments outside of its main constructs.

> NOTE: We are considering a multi-line comment block mechanism (using the widely used /* .. */ approach) as well as reducing some of the constraints on comments. Feedback welcome.
