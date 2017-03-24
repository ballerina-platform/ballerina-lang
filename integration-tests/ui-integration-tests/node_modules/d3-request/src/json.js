import type from "./type";

export default type("application/json", function(xhr) {
  return JSON.parse(xhr.responseText);
});
