import type from "./type";

export default type("text/html", function(xhr) {
  return document.createRange().createContextualFragment(xhr.responseText);
});
