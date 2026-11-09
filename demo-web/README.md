# Demo web de Cyphail

La demo ejecuta consultas reales con el compilador Java y SWI-Prolog. Muestra las filas en una tabla y dibuja los nodos y relaciones devueltos con Cytoscape.js.

## Iniciar

Desde la raíz del repositorio, en PowerShell:

```powershell
.\mvnw.cmd -pl web -am package
java -jar web\target\cyphail-web.jar
```

Dejá esa terminal abierta. En otra terminal:

```powershell
cd demo-web
npm install
npm run dev
```

Abrí `http://127.0.0.1:5173/`. La API escucha únicamente en `127.0.0.1:8090` y Vite reenvía las llamadas a `/api`.

## Probar

Elegí `amigos` o `ingenieria`, escribí una consulta y ejecutala con el botón o con `Ctrl+Enter`. Por ejemplo:

```cypher
MATCH (a)-[r]->(b) RETURN a, b, r, type(r);
```

La pestaña **Grafo** permite acercar, mover los nodos y seleccionar un elemento para leer sus propiedades. **Tabla** muestra todas las columnas devueltas. Cada consulta sustituye el resultado anterior.

También podés ejecutar consultas que cambian el grafo. Una consulta con `RETURN` muestra inmediatamente los elementos creados o actualizados. Por ejemplo:

```cypher
CREATE (n:Demo {nombre: 'Prueba web'}) RETURN n;
```

Las consultas `CREATE`, `SET`, `REMOVE` o `DELETE` sin `RETURN` muestran el estado de ejecución y dejan el lienzo vacío. Ejecutá después un `MATCH ... RETURN ...` para ver el estado actualizado. Los cambios se guardan en `engine/state/` mediante la persistencia del motor.

El grafo visual se construye a partir de los identificadores de nodos y relaciones presentes en las filas. Una consulta que devuelve solo propiedades, como `RETURN n.nombre`, tendrá resultado tabular pero no elementos que dibujar. Esta extracción es suficiente para la demo; una representación estructurada en la respuesta del motor permitiría distinguir los tipos de cada columna sin depender de sus valores.
