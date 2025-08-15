// Llamada GET asíncrona
function loadGetMsg() {
    let nameVar = document.getElementById("name").value;
    fetch(`/hello?name=${encodeURIComponent(nameVar)}`)
        .then(response => response.text())
        .then(data => {
            document.getElementById("getrespmsg").innerText = data;
        })
        .catch(error => console.error("Error en GET:", error));
}

// Llamada POST asíncrona
function loadPostMsg() {
    let nameVar = document.getElementById("postname").value;
    fetch(`/hellopost?name=${encodeURIComponent(nameVar)}`, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById("postrespmsg").innerText = data;
    })
    .catch(error => console.error("Error en POST:", error));
}
