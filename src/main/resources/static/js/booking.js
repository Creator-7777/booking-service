
// ==========================================
// booking.js
// Responsible ONLY for booking appointments
// ==========================================

const Booking = (() => {

    //-------------------------------------------------
    // Controls
    //-------------------------------------------------
    let form;
    let status;
    let cabinetWindow;
    let cabinetContent;
    let closeCabinet;

    //-------------------------------------------------
    // Initialize
    //-------------------------------------------------

    function init() {

        form = document.getElementById("form");
        status = document.getElementById("status");
        // Cabinet elements
        cabinetWindow = document.getElementById("cabinetWindow");
        cabinetContent = document.getElementById("cabinetContent");
        closeCabinet = document.getElementById("closeCabinet");

        if (!form) {
            console.error("Booking form not found");
            return;
        }
        form.addEventListener("submit", submitBooking);
        // Close cabinet
        if (closeCabinet) {
            closeCabinet.addEventListener("click", () => {
                cabinetWindow.classList.add("hidden");
            });
        }
    }

    //-------------------------------------------------
    // Cabinet
    //-------------------------------------------------
    async function openCabinet() {
        console.log("OPEN CABINET");
        // --------------------------------------------
        // Get phone from booking form
        // --------------------------------------------
        const phoneInput = document.getElementById("phone");
        if (!phoneInput) {
            console.error("Phone input not found");
            return;
        }

        const phone = Utils.normalizePhone(phoneInput.value);

        // --------------------------------------------
        // No phone
        // --------------------------------------------

        if (phone === "") {
            alert("Введите номер телефона - Insert your phone number");
            return;
        }

        // --------------------------------------------
        // Validate phone
        // --------------------------------------------

        if (!Utils.validPhone(phone)) {
            alert("Введите корректный номер телефона - Enter a valid phone number");
            return;
        }

        try {

            // ----------------------------------------
            // Get booking history
            // ----------------------------------------
            const response = await fetch( "/api/cabinet/history?phone=" + encodeURIComponent(phone)
            );

            if (!response.ok) {
                throw new Error( "Не удалось получить записи - Failed to load bookings");
            }

            const bookings = await response.json();
            console.log("CABINET BOOKINGS:", bookings);

            // ----------------------------------------
            // No bookings
            // ----------------------------------------
            if (!Array.isArray(bookings) || bookings.length === 0) {

                // Do not open empty cabinet
                cabinetWindow.classList.add("hidden");
                alert( "Для этого номера записей нет - No bookings found");
                return;
            }

            // ----------------------------------------
            // Render bookings
            // ----------------------------------------
            renderHistory(bookings);
            // ----------------------------------------
            // Show cabinet
            // ----------------------------------------
            cabinetWindow.classList.remove("hidden");
        }
        catch (e) {
            console.error("CABINET ERROR:", e);
            cabinetWindow.classList.add("hidden");
            alert( "Не удалось загрузить записи - Failed to load bookings");
        }
    }


    //-------------------------------------------------
    // Booking status
    //-------------------------------------------------

    function statusText(status) {
        switch (status) {

            case "UPCOMING":
                return "🟢 Предстоящая";

            case "COMPLETED":
                return "🔵 Завершена";

            case "CANCELLED":
                return "🔴 Отменена";

            default:
                return status;
        }
    }


    //-------------------------------------------------
    // Render booking history
    //-------------------------------------------------
    function renderHistory(bookings) {
        if (!cabinetContent) {
            console.error("Cabinet content not found");
            return;
        }

        cabinetContent.innerHTML = "";
        bookings.forEach(booking => {
            let cancelButton = "";
            // Only upcoming bookings can be cancelled
            if (booking.status === "UPCOMING") {
                cancelButton = `
                    <button
                        class="cancel-booking-btn"
                        onclick="Booking.cancelBooking(${booking.id})">
                        ❌ ${t("cancelBooking") || "Cancel"}
                    </button>
                `;
            }

            cabinetContent.innerHTML += `
                <div class="cabinet-card">
                    <h3>${booking.service}</h3>
                    <p>📅 ${booking.date}</p>
                    <p>🕒 ${booking.time}</p>
                    <div class="status-badge ${booking.status.toLowerCase()}">
                        ${statusText(booking.status)}
                    </div>
                    ${cancelButton}
                </div>
            `;
        });
    }
    //-------------------------------------------------
    // Cancel Booking
    //-------------------------------------------------
    async function cancelBooking(id) {
        if (
            !confirm(t("confirmCancelBooking") || "Cancel this booking?")
        ) {
            return;
        }

        try {
            const response = await fetch(
                "/api/bookings/cancel",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        id: id
                    })
                }
            );

            if (!response.ok) {
                throw new Error(await response.text());
            }
            alert( t("bookingCancelled") || "Booking cancelled" );
            // Refresh cabinet
            await openCabinet();
        }
        catch (e) {
            console.error(e);
            alert(e.message);
        }
    }


    //-------------------------------------------------
    // Collect booking data
    //-------------------------------------------------
    function buildBooking() {
        return {
            name: form.name.value.trim(),
            phone: Utils.normalizePhone(form.phone.value),
            service:  Array.from(form.service.selectedOptions).map(option => option.textContent).join(", "),
            date:  form.date.value,
            time:  form.time.value
        };
    }

    //-------------------------------------------------
    // Validate booking
    //-------------------------------------------------
    function validateBooking(booking) {
        //------------------------------------------
        // Name
        //------------------------------------------
        if (booking.name.length === 0) {
            Utils.showError( t("nameRequired") || "Enter your name"  );
            return false;
        }

        //------------------------------------------
        // Phone
        //------------------------------------------
        if (!Utils.validPhone(booking.phone)) {
            Utils.showError(  t("phoneInvalid") ||  "Invalid phone number"  );
            return false;
        }

        //------------------------------------------
        // Date
        //------------------------------------------
        if (!booking.date) {
            Utils.showError(  t("chooseDate") ||  "Choose date" );
            return false;
        }

        //------------------------------------------
        // Time
        //------------------------------------------
        if (!booking.time) {
            Utils.showError(
                t("chooseTime") ||
                "Choose time"
            );
            return false;
        }
        return true;
    }

    //-------------------------------------------------
    // Submit booking
    //-------------------------------------------------
    async function submitBooking(event) {
        event.preventDefault();
        status.textContent = "";
        const booking = buildBooking();

        // ----------------------------------------
        // Check whether customer is already verified
        // ----------------------------------------
        const verifiedResponse = await fetch( "/api/sms/is-verified?phone=" +  encodeURIComponent(booking.phone) );
        const alreadyVerified =  await verifiedResponse.json();

        // ----------------------------------------
        // New customer → validate SMS
        // ----------------------------------------
        if (!alreadyVerified) {
            const code =  document .getElementById("codeInput") .value .trim();
            if (code.length === 0) {
                alert( "Введите SMS код - Insert the code" );
                return;
            }

            const validationResponse =
                await fetch(  "/api/sms/validate",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },

                        body: JSON.stringify({
                            phone: booking.phone,
                            code: code,
                            name: booking.name
                        })
                    }
                );

            const validation =  await validationResponse.json();

            if (!validation.valid) {
                alert(
                    "Неверный SMS код - Invalid code"
                );
                return;
            }
        }
        // ----------------------------------------
        // Validation
        // ----------------------------------------
        if (!validateBooking(booking)) {
            return;
        }

        try {
            // --------------------------------------
            // SMS validation
            // --------------------------------------
            const smsValid = await Sms.validateSMS();
            if (!smsValid) {
                return;
            }

            // --------------------------------------
            // Save booking
            // --------------------------------------

            await Utils.postJSON( "/api/bookings", booking );

            // --------------------------------------
            // SUCCESS
            // --------------------------------------
            status.textContent = t("bookingSuccess");

            // --------------------------------------
            // Reset Form
            // --------------------------------------
            reset();
        }
        catch (e) {
            console.error(e);
            Utils.showError(e.message);
        }
    }

    //-------------------------------------------------
    // Reset form
    //-------------------------------------------------
    function reset() {
        form.reset();
        Sms.reset();
        Calendar.resetTime();
    }

    //-------------------------------------------------
    // Public API
    //-------------------------------------------------
    return {
        init,
        // IMPORTANT:
        // mobile-navigation.js uses this
        openCabinet,
        cancelBooking
    };
})();
