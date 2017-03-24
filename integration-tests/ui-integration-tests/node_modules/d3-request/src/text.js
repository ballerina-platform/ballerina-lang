import type from "./type";

export default type("text/plain", function(xhr) {
  return xhr.responseText;
});
