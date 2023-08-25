const toggleButton = document.getElementById('toggleButton');
const infoContainer = document.getElementById('infoContainer');

let isInfoVisible = false;

toggleButton.addEventListener('click', () => {
    if (isInfoVisible) {
        infoContainer.style.display = 'none';
        toggleButton.textContent = 'Show Passengers';
    } else {
        infoContainer.style.display = 'block';
        toggleButton.textContent = 'Hide Passengers';
    }
    isInfoVisible = !isInfoVisible;
});