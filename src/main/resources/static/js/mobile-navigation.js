
/* =========================================================
   MOBILE BOTTOM NAVIGATION
   Главная / Запись / Цены / Мои записи
========================================================= */
document.addEventListener("DOMContentLoaded", () => {
    const navItems = document.querySelectorAll(".mobile-nav-item");
    navItems.forEach(item => {
        item.addEventListener("click", () => {
            const target = item.dataset.target;
            /* =========================================
               Active navigation item
            ========================================= */
            navItems.forEach(nav => {
                nav.classList.remove("active");
            });
            item.classList.add("active");

            /* =========================================
               HOME
            ========================================= */
            if (target === "top") {
                window.scrollTo({
                    top: 0,
                    behavior: "smooth"
                });
                return;
            }

            /* =========================================
               BOOKING
            ========================================= */
            if (target === "booking") {
                const bookingSection =
                    document.getElementById("booking-section");
                if (bookingSection) {
                    bookingSection.scrollIntoView({
                        behavior: "smooth",
                        block: "start"
                    });
                }
                return;
            }
            /* =========================================
               PRICES
            ========================================= */
            if (target === "prices") {
                const pricesSection =
                    document.getElementById("prices-section");

                if (pricesSection) {
                    pricesSection.scrollIntoView({
                        behavior: "smooth",
                        block: "start"
                    });
                }
                return;
            }
            /* =========================================
               MY APPOINTMENTS / CABINET
            ========================================= */
            if (target === "cabinet") {
                const cabinetWindow =
                    document.getElementById("cabinetWindow");
                if (!cabinetWindow) {
                    console.error(
                        "Cabinet window #cabinetWindow was not found"
                    );
                    return;
                }
                /*
                 * IMPORTANT:
                 * Do NOT use cabinetBtn here.
                 * The old top Cabinet button was removed.
                 */
                cabinetWindow.classList.remove("hidden");
                /*
                 * If the existing application has a function
                 * responsible for loading cabinet data, use it.
                 *
                 * We intentionally don't replace the existing
                 * cabinet functionality here.
                 */
                if (typeof loadCabinet === "function") {
                    loadCabinet();
                }
                return;
            }
        });
    });
    /* =========================================
       CLOSE CABINET
       Keep existing close functionality
    ========================================= */
    const closeCabinet =
        document.getElementById("closeCabinet");
    const cabinetWindow =
        document.getElementById("cabinetWindow");

    if (closeCabinet && cabinetWindow) {
        closeCabinet.addEventListener("click", () => {
            cabinetWindow.classList.add("hidden");
        });
    }
});