document.addEventListener("DOMContentLoaded", () => {

    const mainImage =  document.getElementById("galleryMainImage");
    const prevButton =  document.getElementById("galleryPrev");
    const nextButton =  document.getElementById("galleryNext");
    const counter =  document.getElementById("galleryCounter");
    const thumbnails = document.getElementById("galleryThumbnails");


    /*
     * ==========================================
     * MASTER WORKS
     * ==========================================
     */

    const galleryImages = [

    "https://i.imgur.com/LSExOos.jpeg",
    "https://i.imgur.com/5ukYEXt.jpeg",
    "https://i.imgur.com/htjjFDI.jpeg",
    "https://i.imgur.com/HmUITUZ.jpeg",
    "https://i.imgur.com/edWSIk9.jpeg",
    "https://i.imgur.com/iVw4fPs.jpeg",
    "https://i.imgur.com/0wxEGTi.jpeg",
    "https://i.imgur.com/8e1IrkP.jpeg",
    "https://i.imgur.com/WNC5PDQ.jpeg",
    "https://i.imgur.com/lKq22XZ.jpeg",
    "https://i.imgur.com/kdGvPSp.jpeg",
    "https://i.imgur.com/cTv15uz.jpeg"

    ];


    let currentIndex = 0;

    /*
     * ==========================================
     * SHOW IMAGE
     * ==========================================
     */

    function showImage(index) {
        if (!galleryImages.length) {
            return;
        }

        currentIndex =
            (index + galleryImages.length)
            % galleryImages.length;

        /*
         * Fade animation
         */
        mainImage.classList.add("gallery-changing");
        setTimeout(() => {
            mainImage.src =
                galleryImages[currentIndex];
            mainImage.classList.remove(
                "gallery-changing"
            );
        }, 100);
        /*
         * Counter
         */
        counter.textContent =
            `${currentIndex + 1} / ${galleryImages.length}`;
        /*
         * Active thumbnail
         */
        document
            .querySelectorAll(".gallery-thumbnail")
            .forEach((thumbnail, index) => {
                thumbnail.classList.toggle(
                    "active",
                    index === currentIndex
                );
            });
    }
    /*
     * ==========================================
     * CREATE THUMBNAILS
     * ==========================================
     */
    galleryImages.forEach((image, index) => {

        const thumbnail =
            document.createElement("button");
        thumbnail.type = "button";
        thumbnail.className =  "gallery-thumbnail";
        const img =  document.createElement("img");
        img.src = image;
        img.alt = `Master work ${index + 1}`;

        thumbnail.appendChild(img);
        thumbnail.addEventListener(
            "click",
            () => showImage(index)
        );
        thumbnails.appendChild(thumbnail);
    });
    /*
     * ==========================================
     * NEXT / PREVIOUS
     * ==========================================
     */
    nextButton.addEventListener(
        "click",
        () => showImage(currentIndex + 1)
    );
    prevButton.addEventListener(
        "click",
        () => showImage(currentIndex - 1)
    );
    /*
     * ==========================================
     * MOBILE SWIPE
     * ==========================================
     */

    let touchStartX = 0;
    let touchEndX = 0;
    mainImage.addEventListener(
        "touchstart",
        event => {

            touchStartX =
                event.changedTouches[0].screenX;
        },
        { passive: true }
    );
    mainImage.addEventListener(
        "touchend",
        event => {
            touchEndX =
                event.changedTouches[0].screenX;
            const difference =
                touchStartX - touchEndX;
            /*
             * Swipe left
             */

            if (difference > 50) {
                showImage(currentIndex + 1);
            }
            /*
             * Swipe right
             */
            if (difference < -50) {
                showImage(currentIndex - 1);
            }
        },
        { passive: true }
    );
    /*
     * ==========================================
     * INITIAL IMAGE
     * ==========================================
     */
    showImage(0);
});