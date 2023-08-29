function attachEvents() {
    const resetBtn = document.getElementById("reset");
    const inputFields = document.querySelectorAll(".form input");
    const selectFields = document.querySelectorAll(".form select");


    resetBtn.addEventListener("click", resetFields)

    function resetFields(e) {
        debugger
        if (e) {
            e.preventDefault();
        }
        console.log("reset")
        for (const inputField of inputFields) {
            inputField.value = "";
        }
        for (const selectField of selectFields) {
            selectField.selectedIndex = 0;
        }
    }
}

attachEvents();
