function attachEvents() {
    const showPasswordBtn = document.getElementsByClassName("show-password")[0];
    const showConfirmPasswordBtn = document.getElementsByClassName("show-confirm-password")[0];
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirm-password");

    console.log(showPasswordBtn);
    console.log(showConfirmPasswordBtn);
    console.log(passwordInput);
    console.log(confirmPasswordInput);

    showPasswordBtn.addEventListener('mousedown', showHideHandler)

    showConfirmPasswordBtn.addEventListener('mousedown', showHideConfirmHandler)

    function showHideConfirmHandler(e) {
        if (e) {
            e.preventDefault();
        }
        if (confirmPasswordInput.type === 'password') {
            confirmPasswordInput.type = 'text';
            showConfirmPasswordBtn.innerHTML = '<i class="fa-solid fa-eye-slash"></i>';
        } else {
            confirmPasswordInput.type = 'password';
            showConfirmPasswordBtn.innerHTML = '<i class="fa-solid fa-eye"></i>';
        }
    }

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
