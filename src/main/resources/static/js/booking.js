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

        const cabinetBtn = document.getElementById("cabinetBtn");
        const cabinetWindow = document.getElementById("cabinetWindow");
        const cabinetContent = document.getElementById("cabinetContent");
        const closeCabinet = document.getElementById("closeCabinet");

        if (!form) {
            console.error("Booking form not found");
            return;

        }

        form.addEventListener("submit",submitBooking );
    }

    //-------------------------------------------------
    // Cabinet
    //-------------------------------------------------

    cabinetBtn.addEventListener("click", openCabinet);
    closeCabinet.addEventListener("click", () => {
        cabinetWindow.classList.add("hidden");
    });

    async function openCabinet(){
        const phone =  Utils.normalizePhone( document.getElementById("phone").value);
        if(phone===""){
            alert("Введите телефон.");
            return;
        }
        const response =  await fetch( "/api/cabinet/history?phone="  + encodeURIComponent(phone));
        const bookings =  await response.json();
        renderHistory(bookings);
        cabinetWindow.classList.remove("hidden");
    }

    function renderHistory(bookings){
        cabinetContent.innerHTML="";
        if(bookings.length===0){
            cabinetContent.innerHTML=  "<p>История отсутствует.</p>";
            return;
        }

        bookings.forEach(booking=>{
            cabinetContent.innerHTML+=`
            <div class="cabinet-card">
                <h3>${booking.service}</h3>
                <p>📅 ${booking.date}</p>
                <p>🕒 ${booking.time}</p>
                <p>✔ ${booking.status}</p>
            </div> `;
        });
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
                    //.map(option => option.value)
                    .map(option => option.textContent)
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
            Utils.showError( t("nameRequired") || "Enter your name" );
            return false;

        }

        //------------------------------------------
        // Phone
        //------------------------------------------

        if (!Utils.validPhone(booking.phone)) {
            Utils.showError( t("phoneInvalid"));
            return false;
        }

        //------------------------------------------
        // Date
        //------------------------------------------

        if (!booking.date) {
            Utils.showError( t("chooseDate") );
            return false;
        }

        //------------------------------------------
        // Time
        //------------------------------------------

        if (!booking.time) {
            Utils.showError(  t("chooseTime") );
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

        // Check whether customer is already verified
        const verifiedResponse = await fetch( "/api/sms/is-verified?phone=" + encodeURIComponent(booking.phone) );
        const alreadyVerified = await verifiedResponse.json();

        // New customer → validate SMS
        if (!alreadyVerified) {
            const code = document.getElementById("codeInput") .value.trim();
            if (code.length === 0) {
                alert("Введите SMS код");
                return;
            }
            const validationResponse = await fetch("/api/sms/validate", {
                    method: "POST", headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        phone: booking.phone,
                        code: code,
                        name: booking.name
                    })
            });

            const validation = await validationResponse.json();
            if (!validation.valid) {
                alert("Неверный SMS код");
                return;
            }
        }

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

            await Utils.postJSON( "/api/bookings", booking );

            //--------------------------------------
            // SUCCESS
            //--------------------------------------
            status.textContent = t("bookingSuccess");
            //--------------------------------------
            // Reset Form
            //--------------------------------------
            reset();
        }
        catch (e) {
            console.error(e);
            Utils.showError( e.message );
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