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

// New function saveVerifiedPhone 15-07-2026
function saveVerifiedPhone(phone, name) {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("VerifiedPhones");
  const data = sheet.getDataRange().getValues();
  phone = normalizePhone(phone);
  const exists = data.slice(1).some(row => normalizePhone(row[0]) === phone);
  if (exists) {
      return;
  }
  sheet.appendRow([
      phone,
      name,
      new Date()
  ]);
}

// Проверка телефона
const phoneInput = document.getElementById("phone");

//const phoneError =  document.getElementById("phone-error");
//phoneInput.addEventListener("input", () => {
//    const ok =  /^05\d\d{7}$/.test(phoneInput.value);
//    phoneError.style.display =
//        ok ? "none" : "inline";
//});

// New style for verification 15-07-2026
phoneInput.addEventListener("blur", async () => {
    const phone = normalizePhone(phoneInput.value);
    if (!/^05\d\d{7}$/.test(phone))
        return;
    const response =  await fetch( "/api/sms/is-verified?phone="  + encodeURIComponent(phone));
    const verified = await response.json();
    if (verified) {
        document.getElementById("sendCodeBtn").style.display = "none";
        document.getElementById("codeSection").classList.add("hidden");
    } else {
        document.getElementById("sendCodeBtn").style.display = "block";
    }
});


// Отправка SMS
document.getElementById("sendCodeBtn").addEventListener("click", () => {
    const phone = normalizePhone(phoneInput.value);
    if (!/^05\d\d{7}$/.test(phone)) {
        alert("Введите телефон правильно");
        return;
    }

    // Verify for existing customer 15-07-2026
    fetch("/api/customers/verified?phone=" + encodeURIComponent(phone)).then(r => r.json()).then(isVerified => {
        if (isVerified) {
            document.getElementById("codeSection").classList.add("hidden");
            alert("Номер уже подтвержден");
            return;
        }
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
document.getElementById("form").addEventListener("submit", async function(e){
    e.preventDefault();
    const code =   document.getElementById("codeInput").value;

    if (!code){
        alert("Введите код");
        return;
    }

    // Phone number verification 15-07-2026
    const phone = normalizePhone(phoneInput.value);
    const verified = await fetch("/api/customers/verified?phone=" + encodeURIComponent(phone));
    const alreadyVerified = await verified.json();
    if (alreadyVerified){
        const booking = {
            name: form.name.value,
            phone: form.phone.value,
            service: Array.from(form.service.selectedOptions).map(x=>x.value).join(", "),
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

        document.getElementById("status").textContent = "✅ Заявка успешно отправлена";
        form.reset();
        return;
    }

    // Сначала узнаём, подтвержден ли телефон. 15-07-2026
    const verifiedResponse = await fetch( "/api/sms/is-verified?phone=" + encodeURIComponent( normalizePhone(phoneInput.value)));
    const verified = await verifiedResponse.json();

    if (!verified) {
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