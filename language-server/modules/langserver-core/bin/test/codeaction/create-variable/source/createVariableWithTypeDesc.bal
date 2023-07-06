import ballerina/lang.'typedesc;

type Error distinct error;

function func() {
   Error.typeIds();
}
