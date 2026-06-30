const timeSlots = [
    "10:00–12:00",
    "12:00–14:00",
    "14:00–16:00",
    "16:00–18:00"
];

const dateInput = document.querySelector('input[name="date"]');
const timeSelect = document.getElementById("time");
const noSlots = document.getElementById("no-slots");

if (dateInput) {
    dateInput.min = new Date().toISOString().split("T")[0];
}

// Загрузка свободного времени
dateInput.addEventListener("change", () => {
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
        '<option>Загрузка...</option>';
    noSlots.style.display = "none";
    fetch("/api/bookings/booked-times?date=" + encodeURIComponent(selectedDate))
        .then(r => r.json())
        .then(booked => {
            timeSelect.innerHTML =
                '<option value="">Выберите время</option>';
            let available = false;
            timeSlots.forEach(slot => {
                if (!booked.includes(slot)) {
                    const option = document.createElement("option");
                    option.value = slot;
                    option.textContent = slot;
                    timeSelect.appendChild(option);
                    available = true;
                }
            });

            if (!available) {
                noSlots.style.display = "block";
            }
        });
});

// Проверка телефона
const phoneInput = document.getElementById("phone");
const phoneError =  document.getElementById("phone-error");
phoneInput.addEventListener("input", () => {
    const ok =  /^05\d\d{7}$/.test(phoneInput.value);
    phoneError.style.display =
        ok ? "none" : "inline";
});

// Нормализация номера
function normalizePhone(phone) {
    phone = phone.replace(/[^\d]/g, "");
    if (phone.startsWith("972")) {
        return "0" + phone.substring(3);
    }

    if (phone.startsWith("+972")) {
        return "0" + phone.substring(4);
    }

    if (!phone.startsWith("0")) {
        return "0" + phone;
    }
    return phone;
}

// Отправка SMS

document.getElementById("sendCodeBtn")
.addEventListener("click", () => {
    const phone = normalizePhone(phoneInput.value);
    if (!/^05\d\d{7}$/.test(phone)) {
        alert("Введите телефон правильно");
        return;
    }

    fetch("/api/sms/send-code", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify({
            phone: phone
        })
    })

    .then(() => {
        document.getElementById("codeSection")
           .classList.remove("hidden");
        document.getElementById("codeInput").value = "";
        alert("Код отправлен");
    });
});

// Проверка SMS и отправка формы
document.getElementById("form")
.addEventListener("submit", async function(e){
    e.preventDefault();
    const code =   document.getElementById("codeInput").value;

    if (!code){
        alert("Введите код");
        return;
    }

    const validation = await fetch("/api/sms/validate",{
        method:"POST",
        headers:{
            "Content-Type":"application/json"
        },

        body:JSON.stringify({
            phone: phoneInput.value,
            code: code
        })
    });

    const result = await validation.json();
    if (!result.valid){
        alert("Неверный код");
        return;
    }

    const form = this;
    const booking = {
        name: form.name.value,
        phone: form.phone.value,
        service: Array
            .from(form.service.selectedOptions)
            .map(x=>x.value)
            .join(", "),
        date: form.date.value,
        time: form.time.value
    };

    await fetch("/api/bookings",{
        method:"POST",
        headers:{
            "Content-Type":"application/json"
        },
        body:JSON.stringify(booking)
    });

    document.getElementById("status").textContent =  "✅ Заявка успешно отправлена";
    form.reset();

    document.getElementById("codeSection")
        .classList.add("hidden");
});