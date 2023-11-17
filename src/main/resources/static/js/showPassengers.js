const toggleButton = document.getElementById('toggleButton');
const infoContainer = document.getElementById('infoContainer');

let isInfoVisible = false;

toggleButton.addEventListener('click', () => {
    const passengersCount = document.getElementsByClassName("passenger").length;
    if (isInfoVisible) {
        infoContainer.style.display = 'none';
        toggleButton.innerHTML = 'Show requests ' + '(' + passengersCount + ') ';
    } else {
        infoContainer.style.display = 'block';
        toggleButton.innerHTML = 'Hide passengers ';
    }
    isInfoVisible = !isInfoVisible;
});