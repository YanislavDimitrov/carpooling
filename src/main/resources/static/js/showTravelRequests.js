const toggleButtonTravelRequests = document.getElementById('toggle');
const informationContainer = document.getElementById('information-container');

let isInformationVisible = false;

toggleButtonTravelRequests.addEventListener('click', () => {
    if (isInformationVisible) {
        informationContainer.style.display = 'none';
        toggleButtonTravelRequests.textContent = 'Show Travel Requests'
    } else {
        informationContainer.style.display = 'block';
        toggleButtonTravelRequests.textContent = 'Hide Travel Requests'
    }
    isInformationVisible = !isInformationVisible;
});