// -----------------------------
//  MENÚ LATERAL DESPLEGABLE
// -----------------------------


document.getElementById("toggleMenu").addEventListener("click", () => {
    const sidebar = document.getElementById("sidebar");
    const menuContenido = document.getElementById("menuContenido");

    // Si el menú está abierto → cerrarlo y limpiar contenido
    if (sidebar.style.left === "0px") {
        sidebar.style.left = "-250px";
        menuContenido.innerHTML = ""; // limpiar contenido
    } 
    // Si el menú está cerrado → abrirlo
    else {
        sidebar.style.left = "0px";
    }
});


// Mostrar contenido debajo de cada opción del menú
function mostrarSeccion(id) {

    // Obtener el panel completo
    const panel = document.getElementById(id);

    // Obtener TODO el contenido del panel (p, ul, ol, etc.)
    const contenido = panel.innerHTML;

    // Insertarlo dentro del menú
    const menuContenido = document.getElementById("menuContenido");
    menuContenido.innerHTML = contenido;
}


// -----------------------------
//  API
// -----------------------------

document.getElementById("btnEnviar").addEventListener("click", async () => {

    const titulo = document.getElementById("titulo").value;
    const texto = document.getElementById("texto").value;

    if (!titulo.trim() || !texto.trim()) {
        alert("Por favor escribe un título y un texto antes de clasificar.");
        return;
    }

    try {
        const response = await fetch("COLOCAR ENDPOINT AQUI", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                titulo: titulo,
                texto: texto
            })
        });

        const data = await response.json();

        // Mostrar categoría
        document.getElementById("categoria").textContent = data.categoria;

        // Mostrar probabilidad en porcentaje
        document.getElementById("probabilidad").textContent =
            (data.probabilidad * 100).toFixed(2) + "%";

        // Mostrar información adicional
        const lista = document.getElementById("infoAdicional");
        lista.innerHTML = ""; // limpiar lista

        data.informacion_adicional.forEach(item => {
            const li = document.createElement("li");
            li.textContent = item;
            lista.appendChild(li);
        });

        // Mostrar el bloque de resultados
        document.getElementById("resultado").classList.remove("oculto");

    } catch (error) {
        alert("Error al conectar con la API. Verifica que esté activa.");
        console.error(error);
    }
});


