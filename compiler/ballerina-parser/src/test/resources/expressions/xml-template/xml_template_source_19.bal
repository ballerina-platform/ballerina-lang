function foo() {
    var x = xml `<!--hello--world-->`;
    var x = xml `<!--- -->`;
    var x = xml `<!-- --->`;
    var x = xml `<!-- ---->`;
    var x = xml `<!----->`;
}
