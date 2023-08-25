function toggleLabel() {
    var button = document.getElementById("dynamic-button");
    if (button.innerHTML === "Apply for travel") {
        button.innerHTML = "Cancel your request";
    } else {
        button.innerHTML = "Apply for travel";
    }
}