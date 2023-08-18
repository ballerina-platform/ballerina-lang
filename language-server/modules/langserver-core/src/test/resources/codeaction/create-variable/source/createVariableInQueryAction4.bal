public function func() {
    xml x1 = xml `<book>The Lost World</book>`;
    from xml element in x1 select element.toBalString();
}
