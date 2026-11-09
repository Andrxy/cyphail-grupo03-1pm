export async function loadGraphs() {
  const response = await fetch('/api/graphs')
  if (!response.ok) {
    throw new Error('No se pudo obtener la lista de grafos.')
  }
  return response.json()
}

export async function executeQuery(graph, query) {
  const response = await fetch('/api/query', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ graph, query }),
  })
  const result = await response.json()
  if (!response.ok) {
    throw new Error(result.error || 'No se pudo ejecutar la consulta.')
  }
  return result
}
