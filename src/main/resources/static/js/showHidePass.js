function attachEvents() {
    const showPasswordBtn = document.getElementsByClassName("show-password")[0];
    const passwordInput = document.getElementById("password");


    showPasswordBtn.addEventListener('click', showHideHandler)


    function showHideHandler(e) {
        if (e) {
            e.preventDefault();
        }
        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            showPasswordBtn.innerHTML = '<i class="fa-solid fa-eye-slash"></i>';
        } else {
            passwordInput.type = 'password';
            showPasswordBtn.innerHTML = '<i class="fa-solid fa-eye"></i>';
        }

    }
}

attachEvents();
