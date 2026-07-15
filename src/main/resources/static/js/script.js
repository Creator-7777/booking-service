// ==========================================
// Booking Frontend
// ==========================================

const timeSlots = [
    "10:00–12:00",
    "12:00–14:00",
    "14:00–16:00",
    "16:00–18:00"
];

// ==========================================
// DOM
// ==========================================

const form = document.getElementById("form");

const dateInput = document.querySelector('input[name="date"]');
const phoneInput = document.getElementById("phone");
const timeSelect = document.getElementById("time");
const noSlots = document.getElementById("no-slots");

const sendCodeBtn = document.getElementById("sendCodeBtn");
const codeSection = document.getElementById("codeSection");
const codeInput = document.getElementById("codeInput");

const status = document.getElementById("status");

// ==========================================
// Helpers
// ==========================================

function normalizePhone(phone) {

    phone = phone.replace(/\D/g, "");

    if (phone.startsWith("972")) {
        phone = "0" + phone.substring(3);
    }

    if (!phone.startsWith("0")) {
        phone = "0" + phone;
    }

    return phone;
}

function validPhone(phone) {
    return /^05\d\d{7}$/.test(phone);
}

// ==========================================
// Date validation
// ==========================================

if (dateInput) {

    dateInput.min =
        new Date().toISOString().split("T")[0];

}

// ==========================================
// Load free slots
// ==========================================

dateInput.addEventListener("change", async () => {

    const selectedDate = dateInput.value;

    const day =
        new Date(selectedDate).getDay();

    // Friday / Saturday

    if (day === 5 || day === 6) {

        alert("❌ Запись невозможна в пятницу и субботу.");

        dateInput.value = "";

        timeSelect.innerHTML =
            '<option value="">Выберите время</option>';

        noSlots.style.display = "none";

        return;
    }

    timeSelect.innerHTML =
        "<option>Загрузка...</option>";

    noSlots.style.display = "none";

    try {

        const response =
            await fetch(
                "/api/bookings/booked-times?date="
                + encodeURIComponent(selectedDate)
            );

        const booked =
            await response.json();

        timeSelect.innerHTML =
            '<option value="">Выберите время</option>';

        let available = false;

        timeSlots.forEach(slot => {

            if (!booked.includes(slot)) {
                const option =   document.createElement("option");
                option.value = slot;
                option.textContent = slot;
                timeSelect.appendChild(option);
                available = true;

            }

        });

        if (!available) {
            noSlots.style.display = "block";

        }

    } catch (e) {
        console.error(e);
        alert("Ошибка загрузки свободных интервалов.");

    }
});
// ==========================================
// Phone validation
// ==========================================

phoneInput.addEventListener("input", () => {

    const phone = normalizePhone(phoneInput.value);

    if (validPhone(phone)) {

        phoneInput.classList.remove("invalid");

    } else {

        phoneInput.classList.add("invalid");

    }

});

// ==========================================
// Check whether phone is already verified
// ==========================================

phoneInput.addEventListener("blur", async () => {

    const phone = normalizePhone(phoneInput.value);

    if (!validPhone(phone)) {

        sendCodeBtn.style.display = "block";
        codeSection.classList.add("hidden");

        return;

    }

    try {

        const response =
            await fetch(
                "/api/sms/is-verified?phone=" +
                encodeURIComponent(phone));

        const verified =
            await response.json();

        console.log("Phone verified =", verified);

        if (verified === true) {

            sendCodeBtn.style.display = "none";

            codeSection.classList.add("hidden");

        } else {

            sendCodeBtn.style.display = "block";

        }

    } catch (e) {

        console.error(e);

        sendCodeBtn.style.display = "block";

    }

});

// ==========================================
// Send SMS Code
// ==========================================

sendCodeBtn.addEventListener("click", async () => {

    const phone =
        normalizePhone(phoneInput.value);

    if (!validPhone(phone)) {

        alert("Введите правильный номер телефона.");

        return;

    }

    try {

        //
        // Check once again
        //

        const response =
            await fetch(
                "/api/sms/is-verified?phone=" +
                encodeURIComponent(phone));

        const verified =
            await response.json();

        if (verified === true) {

            sendCodeBtn.style.display = "none";

            codeSection.classList.add("hidden");

            alert("Этот номер уже подтвержден.");

            return;

        }

        //
        // Send SMS
        //

        const smsResponse =
            await fetch("/api/sms/send-code", {

                method: "POST",

                headers: {

                    "Content-Type":
                        "application/json"

                },

                body: JSON.stringify({
                    phone: phone
                })
            });

        if (!smsResponse.ok) {
            throw new Error("SMS sending failed");
        }

        codeInput.value = "";
        codeSection.classList.remove("hidden");
        alert("Код подтверждения отправлен.");
    }

    catch (e) {
        console.error(e);
        alert("Ошибка отправки SMS.");
    }
});

// ==========================================
// Booking submit
// ==========================================

form.addEventListener("submit", async (e) => {

    e.preventDefault();

    status.textContent = "";

    //-----------------------------------------------------
    // Collect booking data
    //-----------------------------------------------------

    const booking = {

        name: form.name.value.trim(),

        phone: normalizePhone(form.phone.value),

        service: Array
            .from(form.service.selectedOptions)
            .map(x => x.value)
            .join(", "),

        date: form.date.value,

        time: form.time.value

    };

    //-----------------------------------------------------
    // Phone validation
    //-----------------------------------------------------

    if (!validPhone(booking.phone)) {

        alert("Введите правильный номер телефона.");

        return;

    }

    //-----------------------------------------------------
    // Date
    //-----------------------------------------------------

    if (!booking.date) {

        alert("Выберите дату.");

        return;

    }

    //-----------------------------------------------------
    // Time
    //-----------------------------------------------------

    if (!booking.time) {

        alert("Выберите время.");

        return;

    }

    try {

        //-------------------------------------------------
        // Check whether phone already verified
        //-------------------------------------------------

        const verifiedResponse =
            await fetch(
                "/api/sms/is-verified?phone="
                + encodeURIComponent(booking.phone));

        const alreadyVerified =
            await verifiedResponse.json();

        //-------------------------------------------------
        // New customer -> validate SMS
        //-------------------------------------------------

        if (!alreadyVerified) {

            const code =
                codeInput.value.trim();

            if (code.length === 0) {

                alert("Введите SMS код.");

                return;

            }

            const validationResponse =
                await fetch("/api/sms/validate", {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json"

                    },

                    body: JSON.stringify({

                        phone: booking.phone,

                        code: code

                    })

                });

            const validation =
                await validationResponse.json();

            if (!validation.valid) {

                alert("Неверный SMS код.");

                return;

            }

        }

        //-------------------------------------------------
        // Save booking
        //-------------------------------------------------

        const saveResponse =
            await fetch("/api/bookings", {

                method: "POST",

                headers: {

                    "Content-Type":
                        "application/json"

                },

                body: JSON.stringify(booking)

            });

        if (!saveResponse.ok) {

            const message =
                await saveResponse.text();

            throw new Error(message);

        }

        //-------------------------------------------------
        // SUCCESS
        //-------------------------------------------------

        status.textContent =
            "✅ Запись успешно создана.";

        form.reset();

        //-------------------------------------------------
        // Reset controls
        //-------------------------------------------------

        codeInput.value = "";

        codeSection.classList.add("hidden");

        sendCodeBtn.style.display = "block";

        noSlots.style.display = "none";

        timeSelect.innerHTML =
            '<option value="">Выберите время</option>';

    }

    catch (e) {

        console.error(e);

        alert(

            "Ошибка:\n\n"

            + e.message

        );

    }

});



