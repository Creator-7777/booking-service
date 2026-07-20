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

    //-------------------------------------------------
    // Initialize
    //-------------------------------------------------

    function init() {

        form = document.getElementById("form");
        status = document.getElementById("status");

        if (!form) {

            console.error("Booking form not found");

            return;

        }

        form.addEventListener(

            "submit",

            submitBooking

        );

    }

    //-------------------------------------------------
    // Collect booking data
    //-------------------------------------------------

    function buildBooking() {

        return {

            name:
                form.name.value.trim(),

            phone:
                Utils.normalizePhone(form.phone.value),

            service:
                Array.from(form.service.selectedOptions)
                    .map(option => option.value)
                    .join(", "),

            date:
                form.date.value,

            time:
                form.time.value

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

            Utils.showError(

                t("nameRequired") || "Enter your name"

            );

            return false;

        }

        //------------------------------------------
        // Phone
        //------------------------------------------

        if (!Utils.validPhone(booking.phone)) {

            Utils.showError(

                t("phoneInvalid")

            );

            return false;

        }

        //------------------------------------------
        // Date
        //------------------------------------------

        if (!booking.date) {

            Utils.showError(

                t("chooseDate")

            );

            return false;

        }

        //------------------------------------------
        // Time
        //------------------------------------------

        if (!booking.time) {

            Utils.showError(

                t("chooseTime")

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

        //------------------------------------------
        // Validation
        //------------------------------------------

        if (!validateBooking(booking)) {

            return;

        }

        try {

            //--------------------------------------
            // SMS validation
            //--------------------------------------

            const smsValid = await Sms.validateSMS();

            if (!smsValid) {

                return;

            }

            //--------------------------------------
            // Save booking
            //--------------------------------------

            await Utils.postJSON(

                "/api/bookings",

                booking

            );

            //--------------------------------------
            // SUCCESS
            //--------------------------------------

            status.textContent =

                t("bookingSuccess");

            //--------------------------------------
            // Reset Form
            //--------------------------------------

            reset();

        }

        catch (e) {

            console.error(e);

            Utils.showError(

                e.message

            );

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

        init

    };

}) ();