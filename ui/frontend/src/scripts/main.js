const formError = document.getElementById("formError");

function mensajeDeValidacion(titulo, texto) {
    const faltaTitulo = !titulo.trim();
    const faltaTexto = !texto.trim();

    if (faltaTitulo && faltaTexto) return "Se debe llenar el título y la descripción.";
    if (faltaTitulo) return "Se debe llenar el título.";
    if (faltaTexto) return "Se debe llenar la descripción.";
    return null;
}

function mostrarError(mensaje) {
    formError.textContent = mensaje;
    formError.classList.remove("oculto");
}

function ocultarError() {
    formError.textContent = "";
    formError.classList.add("oculto");
}

document.getElementById("btnEnviar").addEventListener("click", async () => {

    const titulo = document.getElementById("titulo").value;
    const texto = document.getElementById("texto").value;

    const mensaje = mensajeDeValidacion(titulo, texto);
    if (mensaje) {
        mostrarError(mensaje);
        return;
    }

    ocultarError();

    try {
        const apiUrl = import.meta.env.PUBLIC_API_URL ?? "http://localhost:8080";

        const response = await fetch(`${apiUrl}/contenido`, {
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

        if (!response.ok) {
            const detalles = Array.isArray(data.detalles) ? data.detalles.join(" ") : "";
            mostrarError([data.mensaje, detalles].filter(Boolean).join(" "));
            return;
        }

        document.getElementById("categoria").textContent = data.categoria;

        document.getElementById("probabilidad").textContent =
            (data.probabilidad * 100).toFixed(2) + "%";

        const lista = document.getElementById("infoAdicional");
        lista.innerHTML = "";

        data.informacionAdicional.forEach(item => {
            const li = document.createElement("li");
            li.textContent = item;
            lista.appendChild(li);
        });

        document.getElementById("resultado").classList.remove("oculto");

    } catch (error) {
        mostrarError("Error al conectar con la API. Verifica que esté activa.");
        console.error(error);
    }
});
