function attachEvents() {
    const showOldPasswordBtn = document.getElementsByClassName("show-old-password")[0];
    const showNewPasswordBtn = document.getElementsByClassName("show-new-password")[0];
    const showConfirmPasswordBtn = document.getElementsByClassName("show-confirm-password")[0];
    const oldPasswordInput = document.getElementById("old-password");
    const newPasswordInput = document.getElementById("new-password");
    const confirmPasswordInput = document.getElementById("confirm-password");

    showOldPasswordBtn.addEventListener('click', showHideOldHandler)

    showNewPasswordBtn.addEventListener('click', showHideNewHandler)

    showConfirmPasswordBtn.addEventListener('click', showHideConfirmHandler)

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

    function showHideNewHandler(e) {
        if (e) {
            e.preventDefault();
        }
        if (newPasswordInput.type === 'password') {
            newPasswordInput.type = 'text';
            showNewPasswordBtn.innerHTML = '<i class="fa-solid fa-eye-slash"></i>';
        } else {
            newPasswordInput.type = 'password';
            showNewPasswordBtn.innerHTML = '<i class="fa-solid fa-eye"></i>';
        }
    }

    function showHideOldHandler(e) {
        if (e) {
            e.preventDefault();
        }
        if (oldPasswordInput.type === 'password') {
            oldPasswordInput.type = 'text';
            showOldPasswordBtn.innerHTML = '<i class="fa-solid fa-eye-slash"></i>';
        } else {
            oldPasswordInput.type = 'password';
            showOldPasswordBtn.innerHTML = '<i class="fa-solid fa-eye"></i>';
        }
    }

}

attachEvents();
