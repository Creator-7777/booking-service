document.addEventListener("DOMContentLoaded", () => {

    const navItems = document.querySelectorAll(".mobile-nav-item");
    const bookingSection = document.getElementById("booking-section");
    const pricesSection =  document.getElementById("prices-section");
    const cabinetWindow =

    document.getElementById("cabinetWindow");
    navItems.forEach(item => {
        item.addEventListener("click", () => {
            const target = item.dataset.target;

            /* =========================
               Active menu item
            ========================= */
            navItems.forEach(nav =>
                nav.classList.remove("active")
            );
            item.classList.add("active");
            /* =========================
               HOME
            ========================= */
            if (target === "top") {
                window.scrollTo({
                    top: 0,
                    behavior: "smooth"
                });
                return;
            }
            /* =========================
               BOOKING
            ========================= */
            if (target === "booking") {
                if (bookingSection) {
                    bookingSection.scrollIntoView({
                        behavior: "smooth",
                        block: "start"
                    });
                }
                return;
            }
            /* =========================
               PRICES
            ========================= */

            if (target === "prices") {
                if (pricesSection) {
                    pricesSection.scrollIntoView({
                        behavior: "smooth",
                        block: "center"
                    });
                }
                return;
            }
            /* =========================
               MY BOOKINGS
            ========================= */
            if (target === "cabinet") {
                const cabinetBtn =
                    document.getElementById("cabinetBtn");
                if (cabinetBtn) {
                    /*
                     * Use existing cabinet functionality.
                     * We don't duplicate its logic here.
                     */
                    cabinetBtn.click();
                }
            }
        });
    });
});