# Proyecto-Final-Arquisoft

## Integrantes:

* Santiago Espinosa
* Juan Pablo Niño
* Brian Matasca
* Juan Esteban Brawn

## Instrucciones

Para empezar cabe aclarar que el proyecto se encuentra desplegado en diferentes maquinas de la sala.

Paso 1.
Se correrá el comando `icebox --Ice.Config=config.icebox` dentro de la carpeta IceStormConfig.

Paso 2.
Se correrá el comando  `java -jar client/build/libs/client.jar` para ejecutar el jar de servidor en el dispositivo que va a servir de host.

Paso 3.
Se correrá el comando `java -jar server/build/libs/server.jar` para ejecutar el jar del cliente en el mismo dispositivo que el host, en el estado del proyecto se tiene configurado tanto el server, el cliente y el IceStorm deben ejecutados en la misma maquina.

Paso 4.
Se correrá el comando `java -jar worker/build/libs/worker.jar` para ejecutar el jar del worker, este paso se repetirá en cada maquina que se quiera usar como worker.

Paso 5.
En el cliente se escribe la integral con sus respectivos limites y se selecciona el método de integración, luego de esto se arrojara el resultado.
