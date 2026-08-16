
/* =========================================================
   MOBILE BOTTOM NAVIGATION
   Главная / Запись / Цены / Мои записи
========================================================= */

document.addEventListener("DOMContentLoaded", () => {
    const navItems =
        document.querySelectorAll(".mobile-nav-item");

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

                /*
                 * IMPORTANT:
                 *
                 * Do NOT open cabinet directly here.
                 *
                 * Booking.openCabinet() contains the
                 * existing business logic:
                 *
                 * 1. Get phone number
                 * 2. Normalize phone
                 * 3. Check phone
                 * 4. Request booking history
                 * 5. Render bookings
                 * 6. Show cabinet
                 *
                 */

                if (
                    typeof Booking !== "undefined" &&
                    typeof Booking.openCabinet === "function"
                ) {
                    Booking.openCabinet();
                } else {
                    console.error(
                        "Booking.openCabinet() is not available"
                    );
                }
                return;
            }
        });
    });

    /* =========================================
       CLOSE CABINET
    ========================================= */
    const closeCabinet =
        document.getElementById("closeCabinet");

    const cabinetWindow =
        document.getElementById("cabinetWindow");


    if (closeCabinet && cabinetWindow) {
        closeCabinet.addEventListener(
            "click",
            () => {
                cabinetWindow.classList.add("hidden");
            }
        );
    }
});
