function foo() {
    xml x = xml `<??>`;
    xml x = xml `<?`;
    xml x = xml `<foo><?bar </foo>`;
}
