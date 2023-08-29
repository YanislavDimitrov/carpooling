function attachEvents() {
    const pageSpans = document.querySelectorAll("#middle-pages span")

    for (let i = 0; i < pageSpans.length; i++) {
        debugger
        let currentSpan = pageSpans[i];
        if (currentSpan.classList.contains("dots")) {
            if (i < pageSpans.length - 1 && pageSpans[i + 1].classList.contains("dots")) {
                currentSpan.remove();
            }
        }
    }
}

attachEvents();
