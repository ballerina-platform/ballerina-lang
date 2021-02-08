An indication that there has been an error, with a string identifying the reason for the error, and a mapping giving additional details about the error.

An error value provides information about an error that has occurred. Error values belong to a separate basic type; this makes it possible for language constructs to handle errors differently from other values.

The error type is inherently immutable. An error value contains the following information:

- a message, which is a string containing a human-readable message describing the error
- a cause, which is either nil or another error value that was the cause of this error
- a detail, which is an immutable mapping providing additional information about the error
- a stack trace, which is an immutable snapshot of the state of the execution stack
