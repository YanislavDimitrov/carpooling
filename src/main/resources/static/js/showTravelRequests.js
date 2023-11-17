const toggleButtonTravelRequests = document.getElementById('toggle');
const informationContainer = document.getElementById('information-container');

let isInformationVisible = false;

toggleButtonTravelRequests.addEventListener('click', () => {
    const requestsCount = document.getElementsByClassName("travel-request").length;
    if (isInformationVisible) {
        informationContainer.style.display = 'none';
        // toggleButtonTravelRequests.innerHTML = 'Show requests (${requestsCount}) <i id="show-hide-icon" class="fa-solid fa-angle-down"></i>'
        toggleButtonTravelRequests.innerHTML = 'Show requests ' + '(' + requestsCount + ') ';
    } else {
        informationContainer.style.display = 'block';
        toggleButtonTravelRequests.innerHTML = 'Hide requests ';
    }
    isInformationVisible = !isInformationVisible;
});