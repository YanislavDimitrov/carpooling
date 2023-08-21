function attachEvents() {
    const showHideBtn = document.getElementsByClassName("show-password")[0];
    const passwordInput = document.getElementById("password");
    console.log(showHideBtn);
    console.log(passwordInput);

    showHideBtn.addEventListener('click', showHideHandler)

    function showHideHandler(e) {
        if (e) {
            e.preventDefault();
        }
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            showHideBtn.innerHTML = '<i class="fa-solid fa-eye-slash"></i>';
        } else {
            passwordInput.type = 'password';
            showHideBtn.innerHTML = '<i class="fa-solid fa-eye"></i>';
        }

    }

}

attachEvents();
