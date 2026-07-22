// ==========================================
// language.js
// Responsible ONLY for localization
// ==========================================

let translations = {};

// ------------------------------------------
// Translation helper
// ------------------------------------------

function t(key) {
    return translations[key] || key;
}

// ------------------------------------------
// Load language
// ------------------------------------------

async function loadLanguage(lang) {

    try {
        const response = await fetch("/api/i18n/" + lang);
        if (!response.ok) {
            throw new Error("Cannot load language " + lang);
        }
        translations = await response.json();
        applyTranslations();
        await loadServices(lang);
        localStorage.setItem("lang", lang);
    } catch (e) {
        console.error("Language loading failed", e);
    }
}
// ------------------------------------------
// Load Service
// ------------------------------------------
async function loadServices(lang){
    const response = await fetch("/api/services/" + lang);
    const services = await response.json();
    const select = document.getElementById("service");
    select.innerHTML = "";
    services.forEach(service => {
        const option = document.createElement("option");
        option.value = service.id;
        option.textContent = `${service.name} — ${service.price}₪`;
        option.dataset.price = service.price;
        select.appendChild(option);
    });
}

// ------------------------------------------
// Apply translations to page
// ------------------------------------------

function applyTranslations() {
    //---------------------------------------------------
    // Text
    //---------------------------------------------------
    document.querySelectorAll("[data-i18n]").forEach(element => {
        const key = element.dataset.i18n;
        /*if (translations[key]) {
            element.textContent = translations[key];
        }*/

        if (translations[key]) {
            if ( element.tagName === "INPUT" || element.tagName === "TEXTAREA" ) {
                element.placeholder = translations[key];
            } else {
                element.innerHTML = translations[key];
            }
        }
    });

    //---------------------------------------------------
    // Placeholders
    //---------------------------------------------------

    document.querySelectorAll("[data-i18n-placeholder]").forEach(element => {
        const key = element.dataset.i18nPlaceholder;

/*        if (translations[key]) {
            element.placeholder = translations[key];
        }*/

         if (translations[key]) {
            if ( element.tagName === "INPUT" || element.tagName === "TEXTAREA" ) {
                element.placeholder = translations[key];
            } else {
                element.innerHTML = translations[key];
            }
        }

    });

}

// ------------------------------------------
// Language menu
// ------------------------------------------
function initializeLanguageSelector() {
    const languageBtn = document.getElementById("languageBtn");
    const languageMenu = document.getElementById("languageMenu");
    if (!languageBtn || !languageMenu) {
        console.error("Language selector not found.");
        return;
    }

    //---------------------------------------------------
    // Open / Close menu
    //---------------------------------------------------

    languageBtn.addEventListener("click", function (event) {
        event.stopPropagation();
        languageMenu.classList.toggle("hidden");

    });

    //---------------------------------------------------
    // Select language
    //---------------------------------------------------

    document.querySelectorAll("[data-lang]").forEach(button => {
        button.addEventListener("click", function () {
            const lang = this.dataset.lang;
            loadLanguage(lang);
            languageMenu.classList.add("hidden");

        });

    });

    //---------------------------------------------------
    // Close if clicked outside
    //---------------------------------------------------

    document.addEventListener("click", function (event) {
        if (!languageMenu.contains(event.target)
            && !languageBtn.contains(event.target)) {
            languageMenu.classList.add("hidden");

        }

    });

}

// ------------------------------------------
// Initialization
// ------------------------------------------
function initializeLanguage() {
    initializeLanguageSelector();
    const lang = localStorage.getItem("lang") || "ru";
    loadLanguage(lang);

}