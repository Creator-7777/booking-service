// ==========================================
// calendar.js
// Responsible ONLY for Date & Time Slots
// ==========================================

const Calendar = (() => {

    //-------------------------------------------------
    // Private variables
    //-------------------------------------------------

    const timeSlots = [
        "10:00–12:00",
        "12:00–14:00",
        "14:00–16:00",
        "16:00–18:00"
    ];

    let dateInput;
    let timeSelect;
    let noSlots;

    //-------------------------------------------------
    // Initialize
    //-------------------------------------------------

    function init() {

        dateInput = document.getElementById("date");
        timeSelect = document.getElementById("time");
        noSlots = document.getElementById("no-slots");

        if (!dateInput || !timeSelect) {

            console.error("Calendar controls not found.");

            return;

        }

        //-------------------------------------------------
        // Minimum date = today
        //-------------------------------------------------

        dateInput.min = Utils.today();

        //-------------------------------------------------
        // Listen date change
        //-------------------------------------------------

        dateInput.addEventListener("change", loadAvailableSlots);

    }

    //-------------------------------------------------
    // Load free slots
    //-------------------------------------------------

    async function loadAvailableSlots() {

        const selectedDate = dateInput.value;

        if (!selectedDate) {
            return;
        }

        //-------------------------------------------------
        // Friday / Saturday
        //-------------------------------------------------

        if (Utils.isWeekend(selectedDate)) {

            Utils.showError(
                t("weekendNotAllowed") ||
                "Booking is unavailable on Friday and Saturday."
            );

            dateInput.value = "";

            resetTime();

            return;

        }

        //-------------------------------------------------
        // Loading...
        //-------------------------------------------------

        timeSelect.innerHTML = "";

        timeSelect.appendChild(
            Utils.createOption(
                "",
                t("loading")
            )
        );

        noSlots.classList.add("hidden");

        try {

            const booked =
                await Utils.getJSON(
                    "/api/bookings/booked-times?date=" +
                    encodeURIComponent(selectedDate)
                );

            fillAvailableSlots(booked);

        }

        catch (e) {

            console.error(e);

            Utils.showError(
                t("errorLoadingTimeSlots")
            );

            resetTime();

        }

    }

    //-------------------------------------------------
    // Fill dropdown
    //-------------------------------------------------

    function fillAvailableSlots(bookedSlots) {

        timeSelect.innerHTML = "";

        timeSelect.appendChild(

            Utils.createOption(

                "",

                t("chooseTime")

            )

        );

        let available = false;

        timeSlots.forEach(slot => {

            if (!bookedSlots.includes(slot)) {

                timeSelect.appendChild(

                    Utils.createOption(

                        slot,

                        slot

                    )

                );

                available = true;

            }

        });

        if (!available) {

            noSlots.classList.remove("hidden");

        }

        else {

            noSlots.classList.add("hidden");

        }

    }

    //-------------------------------------------------
    // Reset dropdown
    //-------------------------------------------------

    function resetTime() {

        timeSelect.innerHTML = "";

        timeSelect.appendChild(

            Utils.createOption(

                "",

                t("chooseTime")

            )

        );

        noSlots.classList.add("hidden");

    }

    //-------------------------------------------------
    // Public API
    //-------------------------------------------------

    return {

        init,

        resetTime,

        loadAvailableSlots

    };

}) ();