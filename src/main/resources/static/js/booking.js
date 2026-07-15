const timeSlots = [
    "10:00–12:00",
    "12:00–14:00",
    "14:00–16:00",
    "16:00–18:00"
];

const form = document.getElementById("form");
const dateInput = document.querySelector('input[name="date"]');
const phoneInput = document.getElementById("phone");
const timeSelect = document.getElementById("time");
const noSlots = document.getElementById("no-slots");
const sendCodeBtn = document.getElementById("sendCodeBtn");
const codeSection = document.getElementById("codeSection");
const codeInput = document.getElementById("codeInput");
const status = document.getElementById("status");

let phoneAlreadyVerified = false;


// Helpers
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


// Date limits
if (dateInput) {
    dateInput.min = new Date().toISOString().split("T")[0];
}

// Date changed
dateInput.addEventListener("change", async () => {

    const selectedDate = dateInput.value;

    const day = new Date(selectedDate).getDay();

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
            await fetch("/api/bookings/booked-times?date="
                + encodeURIComponent(selectedDate));

        const booked = await response.json();

        timeSelect.innerHTML =
            '<option value="">Выберите время</option>';

        let available = false;

        timeSlots.forEach(slot => {

            if (!booked.includes(slot)) {

                const option =
                    document.createElement("option");

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

        alert("Ошибка загрузки свободного времени.");
    }

});


// Send SMS
sendCodeBtn.addEventListener("click", async () => {
    const phone = normalizePhone(phoneInput.value);

    if (!validPhone(phone)) {
        alert("Введите телефон правильно");
        return;
    }

    const response = await fetch( "/api/sms/is-verified?phone=" + encodeURIComponent(phone));
    const alreadyVerified = await response.json();

    if (alreadyVerified) {
        sendCodeBtn.style.display = "none";
        codeSection.classList.add("hidden");
        alert("Этот номер уже подтвержден.");
        return;
    }

    try {
        await fetch("/api/sms/send-code", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                phone: phone
            })
        });

        codeInput.value = "";
        codeSection.classList.remove("hidden")
        alert("Код отправлен");

    } catch (e) {
        console.error(e);
        alert("Ошибка отправки SMS.");
    }
});


// Submit
form.addEventListener("submit", async function (e) {
    e.preventDefault();
    const phone = normalizePhone(phoneInput.value);

    if (!validPhone(phone)) {
        alert("Введите правильный телефон");
        return;
    }


    // SMS verification only for new customers
    if (!phoneAlreadyVerified) {
        const code = codeInput.value;
        if (!code) {
            alert("Введите код");
            return;
        }

        const validation =
            await fetch("/api/sms/validate", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    phone: phone,
                    code: code
                })
            });

        const result =
            await validation.json();

        if (!result.valid) {
            alert("Неверный код");
            return;
        }

        // phone becomes verified
        phoneAlreadyVerified = true;
    }


    // Booking
    const booking = {
        name: form.name.value,
        phone: phone,
        service:
            Array.from(form.service.selectedOptions)
                .map(x => x.value)
                .join(", "),

        date: form.date.value,
        time: form.time.value
    };

    try {
        const response =   await fetch("/api/bookings",
                method: "POST",
              headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(booking)
            });
        if (!response.ok) {
            throw new Error("Booking failed");
        }

        status.textContent = "✅ Заявка успешно отправлена";
        form.reset();
        codeInput.value = "";
        codeSection.classList.add("hidden");
        sendCodeBtn.style.display = "block";
        phoneAlreadyVerified = false;
        timeSelect.innerHTML =  '<option value="">Выберите время</option>';
        noSlots.style.display = "none";

    } catch (e) {
        console.error(e);
        alert("Ошибка при создании записи.");
    }
});