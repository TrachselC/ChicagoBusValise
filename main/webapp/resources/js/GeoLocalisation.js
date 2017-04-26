var map;
var timedTask = setInterval(refreshMap, 30000);
var markers = [];
/**
 * Recherche de toutes les commandes de la page,
 * les commandes sont trouvées en fonction de l'attribut title sur les balises contenant les valeurs
 * @returns Un tableau composé de commandes
 */
function getBusList() {
    var busList = [];
    $("#form\\:busTable_data tr").each(function () {
        $this = $(this);
        var bus = {
            numero: jQuery(this).find(".numero").text(),
            direction: jQuery(this).find(".direction").text(),
            lat: jQuery(this).find(".lat").text(),
            lng: jQuery(this).find(".lng").text()
        };
        busList.push(bus);
    });
    return busList;
}
/**
 * Initialise la nouvelle map et appelle la fonction de génération des marqueurs
 *
 */
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 41.980262, lng: -87.668452},
        mapTypeId: 'satellite',
        zoom: 13
    });

    var office = {lat: 41.980262, lng: -87.668452};
    var marker = new google.maps.Marker({
        position: office,
        map: map,
        title: 'Bureau',
        icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
    });

    var busStop = {lat: 41.984982, lng: -87.668999};
    var marker = new google.maps.Marker({
        position: busStop,
        map: map,
        title: 'Arrêt de bus',
        icon: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
    });
    var busStopLugage = {lat:41.97979, lng: -87.668452};
    var marker = new google.maps.Marker({
        position: busStopLugage,
        map: map,
        title: 'Arrêt de bus bureau',
        icon: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png'
    })

    refreshMap();
}
function refreshMap() {
    deleteMarkers();

    var busList = getBusList();
    busList.forEach(function (bus) {
        var busLocation = {lat: Number(bus.lat), lng: Number(bus.lng)};
        if (bus.numero == $(".goodBus .numero").text()) {
            var marker = new google.maps.Marker({
                position: busLocation,
                map: map,
                title: bus.numero + bus.direction,
                icon: 'http://maps.google.com/mapfiles/ms/icons/orange-dot.png'
            });
            var distanceLat = bus.lat - 41.984982;
            var distanceMile = distanceLat * 69 / 1;
            if (distanceMile < 0.3 && bus.direction === 'Southbound' && bus.lat > 41.980262) {
                alert("You should go and catch your suitcase to the bus stop");
            }
        } else {
            var marker = new google.maps.Marker({
                position: busLocation,
                map: map,
                title: bus.numero + bus.direction,
                icon: 'http://maps.google.com/mapfiles/ms/icons/yellow-dot.png'
            });
        }
        markers.push(marker);
    });

    showMarkers();
}

// Sets the map on all markers in the array.
function setMapOnAll(map) {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(map);
    }
}

// Removes the markers from the map, but keeps them in the array.
function clearMarkers() {
    setMapOnAll(null);
}

// Shows any markers currently in the array.
function showMarkers() {
    setMapOnAll(map);
}

// Deletes all markers in the array by removing references to them.
function deleteMarkers() {
    clearMarkers();
    markers = [];
}

jQuery(document).ready(function () {
    initMap();
});

