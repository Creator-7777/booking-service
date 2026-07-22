// ==========================================
// sms.js
// Responsible ONLY for SMS verification
// ==========================================

const Sms = (() => {

    //-------------------------------------------------
    // Controls
    //-------------------------------------------------

    let phoneInput;
    let sendCodeBtn;
    let codeSection;
    let codeInput;

    //-------------------------------------------------
    // Initialize
    //-------------------------------------------------

    function init() {

        phoneInput = document.getElementById("phone");
        sendCodeBtn = document.getElementById("sendCodeBtn");
        codeSection = document.getElementById("codeSection");
        codeInput = document.getElementById("codeInput");

        if (!phoneInput) {
            console.error("Phone input not found");
            return;

        }

        phoneInput.addEventListener(
            "input",
            validatePhoneInput
        );

        phoneInput.addEventListener(
            "blur",
            checkVerifiedPhone
        );

        sendCodeBtn.addEventListener(
            "click",
            sendSMSCode
        );

    }

    //-------------------------------------------------
    // Validate phone while typing
    //-------------------------------------------------
    function validatePhoneInput() {
        const phone = Utils.normalizePhone(phoneInput.value);
        if (Utils.validPhone(phone)) {
            phoneInput.classList.remove("invalid");
        }
        else {
            phoneInput.classList.add("invalid");
        }

    }

    //-------------------------------------------------
    // Check if phone already verified
    //-------------------------------------------------
    async function checkVerifiedPhone() {
        const phone = Utils.normalizePhone(phoneInput.value);
        if (!Utils.validPhone(phone)) {
            sendCodeBtn.style.display = "block";
            codeSection.classList.add("hidden");
            return;
        }

        try {
            const verified =  await Utils.getJSON( "/api/sms/is-verified?phone=" +  encodeURIComponent(phone) );
            if (verified === true) {
                sendCodeBtn.style.display = "none";
                codeSection.classList.add("hidden");
            }

            else {
                sendCodeBtn.style.display = "block";
            }

        }

        catch (e) {
            console.error(e);
            sendCodeBtn.style.display = "block";

        }

    }

    //-------------------------------------------------
    // Send SMS
    //-------------------------------------------------
    async function sendSMSCode() {
        const phone = Utils.normalizePhone(phoneInput.value);
        if (!Utils.validPhone(phone)) {
            Utils.showError(
                t("phoneInvalid")
            );
            return;
        }

        try {
            //---------------------------------------------
            // Already verified ?
            //---------------------------------------------
            const verified = await Utils.getJSON( "/api/sms/is-verified?phone=" + encodeURIComponent(phone) );

            if (verified === true) {
                sendCodeBtn.style.display = "none";
                codeSection.classList.add("hidden");
                Utils.showSuccess( t("numberIsVerified")
                );
                return;
            }

            //---------------------------------------------
            // Send SMS
            //---------------------------------------------
            await Utils.postJSON( "/api/sms/send-code",
                {
                    phone: phone
                }
            );

            codeInput.value = "";
            codeSection.classList.remove("hidden");
            Utils.showSuccess(t("smsSent") );
        }

        catch (e) {
            console.error(e);
            Utils.showError( t("errorSendSMS")  );
        }
    }

    //-------------------------------------------------
    // Validate entered SMS code
    //-------------------------------------------------
    async function validateSMS() {
        const phone =  Utils.normalizePhone(phoneInput.value);
        //---------------------------------------------
        // Already verified?
        //---------------------------------------------
        const verified = await Utils.getJSON( "/api/sms/is-verified?phone=" + encodeURIComponent(phone) );

        if (verified === true) {
            return true;
        }

        //---------------------------------------------
        // Validate code
        //---------------------------------------------
        const code = codeInput.value.trim();
        if (code.length === 0) {
            Utils.showError(  t("enterCode") );
            return false;
        }

        const result =  await Utils.postJSON( "/api/sms/validate",
                {
                    phone: phone,
                    code: code
                }
            );

        if (!result.valid) {
            Utils.showError( t("wrongCode") );
            return false;

        }
        return true;
    }

    //-------------------------------------------------
    // Reset SMS controls
    //-------------------------------------------------
    function reset() {
        codeInput.value = "";
        codeSection.classList.add("hidden");
        sendCodeBtn.style.display = "block";
    }

    //-------------------------------------------------
    // Public API
    //-------------------------------------------------
    return {
        init,
        validateSMS,
        reset
    };

}) ();