// ==========================================
// utils.js
// Common helper functions
// ==========================================

const Utils = (() => {

    //-------------------------------------------------
    // Normalize Israeli phone number
    //-------------------------------------------------
    function normalizePhone(phone) {

        if (!phone) return "";

        phone = phone.replace(/\D/g, "");

        if (phone.startsWith("972")) {
            phone = "0" + phone.substring(3);
        }

        if (!phone.startsWith("0")) {
            phone = "0" + phone;
        }

        return phone;
    }

    //-------------------------------------------------
    // Validate Israeli phone
    //-------------------------------------------------
    function validPhone(phone) {

        return /^05\d\d{7}$/.test(normalizePhone(phone));

    }

    //-------------------------------------------------
    // Today's date
    //-------------------------------------------------
    function today() {

        return new Date().toISOString().split("T")[0];

    }

    //-------------------------------------------------
    // Friday / Saturday
    //-------------------------------------------------
    function isWeekend(date) {

        const day = new Date(date).getDay();

        return day === 5 || day === 6;

    }

    //-------------------------------------------------
    // Create option
    //-------------------------------------------------
    function createOption(value, text) {

        const option = document.createElement("option");

        option.value = value;
        option.textContent = text;

        return option;

    }

    //-------------------------------------------------
    // Show alert
    //-------------------------------------------------
    function showError(message) {

        alert("❌ " + message);

    }

    //-------------------------------------------------
    // Show success
    //-------------------------------------------------
    function showSuccess(message) {

        alert("✅ " + message);

    }

    //-------------------------------------------------
    // HTTP GET JSON
    //-------------------------------------------------
    async function getJSON(url) {

        const response = await fetch(url);

        if (!response.ok) {

            throw new Error(response.statusText);

        }

        return await response.json();

    }

    //-------------------------------------------------
    // HTTP POST JSON
    //-------------------------------------------------
    async function postJSON(url, data) {

        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }
        const text = await response.text();
        return text ? JSON.parse(text) : {};
        //return await response.json();
    }

    //-------------------------------------------------
    // Return public API
    //-------------------------------------------------
    return {

        normalizePhone,

        validPhone,

        today,

        isWeekend,

        createOption,

        showError,

        showSuccess,

        getJSON,

        postJSON

    };

}) ();