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

        document.getElementById("categoria").textContent = data.categoria;

        document.getElementById("probabilidad").textContent =
            (data.probabilidad * 100).toFixed(2) + "%";

        const lista = document.getElementById("infoAdicional");
        lista.innerHTML = "";

        data.informacion_adicional.forEach(item => {
            const li = document.createElement("li");
            li.textContent = item;
            lista.appendChild(li);
        });

        document.getElementById("resultado").classList.remove("oculto");

    } catch (error) {
        alert("Error al conectar con la API. Verifica que esté activa.");
        console.error(error);
    }
});
