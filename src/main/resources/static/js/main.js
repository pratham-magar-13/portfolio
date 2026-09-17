// Mobile nav + smooth UX helpers
(function () {
  var toggle = document.getElementById("navToggle");
  var links = document.getElementById("navLinks");
  if (toggle && links) {
    toggle.addEventListener("click", function () {
      links.classList.toggle("open");
    });
    links.querySelectorAll("a").forEach(function (a) {
      a.addEventListener("click", function () {
        links.classList.remove("open");
      });
    });
  }

  // Auto-scroll to contact form if there was a validation error or success flag
  if (window.location.hash === "#contact" || window.location.search.indexOf("sent=true") !== -1) {
    var el = document.getElementById("contact");
    if (el) {
      setTimeout(function () {
        el.scrollIntoView({ behavior: "smooth" });
      }, 100);
    }
  }
})();
