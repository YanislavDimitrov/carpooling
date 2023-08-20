function attachEvents() {
    const showHidBtn = document.getElementById("avatar");
    const dropdown = document.getElementsByClassName("dropdown")[0];
    showHidBtn.addEventListener("click", showOrHideDropdown);
    dropdown.style.display = 'none'

    window.addEventListener('click', function (event) {
        console.log(event.target)
        if ((!event.target.classList.contains('avatar'))
            && dropdown.style.display === 'block') {
            dropdown.style.display = 'none';
        }
    });

    function showOrHideDropdown(e) {
        if (dropdown.style.display === 'none') {
            dropdown.style.display = 'block';
        } else {
            dropdown.style.display = 'none';
        }
    }
}

attachEvents();
