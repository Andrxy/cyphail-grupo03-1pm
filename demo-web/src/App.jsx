import { useCallback, useEffect, useRef, useState } from 'react'
import { executeQuery, loadGraphs } from './api.js'
import GraphCanvas from './GraphCanvas.jsx'

const examples = [
  {
    label: 'Relaciones',
    query: 'MATCH (a)-[r]->(b) RETURN a, b, r, type(r);',
  },
  {
    label: 'Nodos',
    query: 'MATCH (n) RETURN n;',
  },
  {
    label: 'Propiedades',
    query: 'MATCH (n) RETURN n.nombre;',
  },
]

const emptyGraph = { nodes: [], edges: [] }

function DetailPanel({ selection, onClose }) {
  if (!selection) {
    return (
      <div className="detail-empty">
        <div className="detail-empty-icon">◎</div>
        <p>Seleccioná un nodo o una relación para ver sus datos.</p>
      </div>
    )
  }

  const { kind, item } = selection
  return (
    <div className="detail-content">
      <div className="detail-heading">
        <span className={`detail-kind ${kind}`}>{kind === 'node' ? 'Nodo' : 'Relación'}</span>
        <button className="icon-button" onClick={onClose} aria-label="Cerrar detalles">×</button>
      </div>
      <h3>{kind === 'node' ? item.label : item.type}</h3>
      <dl className="detail-list">
        <div><dt>ID</dt><dd>{item.id}</dd></div>
        {kind === 'node' ? (
          <div><dt>Etiquetas</dt><dd>{item.labels.length ? item.labels.join(', ') : 'Sin etiquetas'}</dd></div>
        ) : (
          <>
            <div><dt>Origen</dt><dd>{item.source}</dd></div>
            <div><dt>Destino</dt><dd>{item.target}</dd></div>
          </>
        )}
      </dl>
      <h4>Propiedades</h4>
      {Object.keys(item.properties).length ? (
        <dl className="detail-list">
          {Object.entries(item.properties).map(([key, value]) => (
            <div key={key}><dt>{key}</dt><dd>{value}</dd></div>
          ))}
        </dl>
      ) : <p className="muted">Este elemento no tiene propiedades.</p>}
    </div>
  )
}

function ResultsTable({ headers, rows }) {
  if (!headers?.length) {
    return <div className="table-empty">Consulta ejecutada. Hacé un MATCH con RETURN para ver el grafo actualizado.</div>
  }
  return (
    <div className="table-wrap">
      <table>
        <thead><tr>{headers.map((header, index) => <th key={`${header}-${index}`}>{header}</th>)}</tr></thead>
        <tbody>
          {rows.length ? rows.map((row, index) => (
            <tr key={index}>{row.map((cell, cellIndex) => <td key={cellIndex}>{cell}</td>)}</tr>
          )) : <tr><td colSpan={headers.length} className="table-empty">Sin resultados para esta consulta.</td></tr>}
        </tbody>
      </table>
    </div>
  )
}

export default function App() {
  const [graphs, setGraphs] = useState([])
  const [graphId, setGraphId] = useState('')
  const [query, setQuery] = useState(examples[0].query)
  const [result, setResult] = useState(null)
  const [activeTab, setActiveTab] = useState('graph')
  const [selection, setSelection] = useState(null)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState('')
  const [serverReady, setServerReady] = useState(false)
  const fitRef = useRef(null)
  const requestRef = useRef(0)

  const run = useCallback(async (selectedGraph = graphId, source = query) => {
    if (!selectedGraph || !source.trim()) {
      return
    }
    const request = ++requestRef.current
    setPending(true)
    setError('')
    setSelection(null)
    setResult(null)
    try {
      const nextResult = await executeQuery(selectedGraph, source)
      if (request !== requestRef.current) {
        return
      }
      setResult(nextResult)
      setActiveTab(nextResult.graph.nodes.length ? 'graph' : 'table')
    } catch (cause) {
      if (request === requestRef.current) {
        setError(cause.message)
      }
    } finally {
      if (request === requestRef.current) {
        setPending(false)
      }
    }
  }, [graphId, query])

  useEffect(() => {
    let active = true
    loadGraphs().then((available) => {
      if (!active) {
        return
      }
      setGraphs(available)
      setServerReady(true)
      if (available.length) {
        const initialGraph = available.find((item) => item.id === 'amigos')?.id || available[0].id
        setGraphId(initialGraph)
        run(initialGraph, examples[0].query)
      }
    }).catch(() => {
      if (active) {
        setError('No se pudo conectar con Cyphail. Iniciá la API Java y recargá esta página.')
      }
    })
    return () => { active = false }
  }, [])

  const graph = result?.graph || emptyGraph

  function changeGraph(event) {
    const selectedGraph = event.target.value
    requestRef.current += 1
    setGraphId(selectedGraph)
    setResult(null)
    setSelection(null)
    setError('')
    setPending(false)
  }

  function handleEditorKeyDown(event) {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault()
      run()
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-mark" aria-hidden="true"><i /><i /><i /></div>
          <span>Cyphail</span>
          <span className="brand-divider" />
          <span className="brand-section">Explorador</span>
        </div>
        <div className={`connection ${serverReady ? 'connected' : ''}`}>
          <span className="connection-dot" />
          {serverReady ? 'Motor conectado' : 'Sin conexión'}
        </div>
      </header>

      <main className="workspace">
        <aside className="query-panel">
          <div className="panel-intro">
            <span className="section-index">01 / Consulta</span>
            <h1>Explorá tus grafos.</h1>
            <p>Escribí una consulta Cyphail y examiná sus resultados en la tabla o en el lienzo.</p>
          </div>

          <div className="field-group">
            <label htmlFor="graph-select">Grafo activo</label>
            <div className="select-wrap">
              <select id="graph-select" value={graphId} onChange={changeGraph} disabled={!graphs.length}>
                {graphs.length ? graphs.map((item) => <option key={item.id} value={item.id}>{item.id}</option>) : <option value="">Sin grafos</option>}
              </select>
            </div>
            <span className="field-hint">{graphs.find((item) => item.id === graphId)?.description || 'Seleccioná un grafo disponible'}</span>
          </div>

          <div className="field-group editor-group">
            <div className="field-heading">
              <label htmlFor="query-editor">Consulta</label>
              <span>Cyphail</span>
            </div>
            <textarea
              id="query-editor"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              onKeyDown={handleEditorKeyDown}
              spellCheck={false}
              aria-describedby="editor-hint"
            />
            <span id="editor-hint" className="field-hint">Ctrl + Enter para ejecutar</span>
          </div>

          <button className="run-button" onClick={() => run()} disabled={!serverReady || pending || !query.trim()}>
            <span>{pending ? 'Ejecutando…' : 'Ejecutar consulta'}</span>
            <span aria-hidden="true">↗</span>
          </button>

          <div className="examples">
            <h2>Probá una consulta</h2>
            <div className="example-list">
              {examples.map((example) => (
                <button key={example.label} onClick={() => { setQuery(example.query); run(graphId, example.query) }} disabled={!serverReady || pending}>
                  <span>{example.label}</span><span aria-hidden="true">↗</span>
                </button>
              ))}
            </div>
          </div>
        </aside>

        <section className="results-panel" aria-label="Resultados de la consulta">
          <div className="result-toolbar">
            <div>
              <span className="section-index">02 / Resultado</span>
              <h2>{graphId || 'Grafo'}</h2>
            </div>
            <div className="result-actions">
              {activeTab === 'graph' && graph.nodes.length > 0 && <button className="fit-button" onClick={() => fitRef.current?.()}>Centrar grafo</button>}
              <span className="result-count">{result?.rows?.length ?? 0} filas</span>
            </div>
          </div>

          <div className="result-tabs" role="tablist" aria-label="Vista del resultado">
            <button role="tab" aria-selected={activeTab === 'graph'} className={activeTab === 'graph' ? 'active' : ''} onClick={() => setActiveTab('graph')}>Grafo <span>{graph.nodes.length}</span></button>
            <button role="tab" aria-selected={activeTab === 'table'} className={activeTab === 'table' ? 'active' : ''} onClick={() => setActiveTab('table')}>Tabla <span>{result?.rows?.length ?? 0}</span></button>
          </div>

          {error && <div className="feedback error" role="alert">{error}</div>}
          {result && !result.ok && <div className="feedback error" role="alert">{result.message}</div>}

          <div className="result-content">
            {activeTab === 'graph' ? (
              graph.nodes.length ? (
                <div className="graph-layout">
                  <div className="graph-surface">
                    <GraphCanvas graph={graph} onSelect={setSelection} fitRef={fitRef} />
                    <div className="graph-legend"><span className="legend-node" /> Nodo <span className="legend-edge" /> Relación</div>
                  </div>
                  <aside className="detail-panel" aria-label="Detalles de selección">
                    <div className="detail-title">Detalles</div>
                    <DetailPanel selection={selection} onClose={() => setSelection(null)} />
                  </aside>
                </div>
              ) : (
                <div className="empty-state">
                  <div className="empty-symbol">◎</div>
                  <h3>{pending ? 'Ejecutando consulta…' : 'Todavía no hay nodos para mostrar'}</h3>
                  <p>{result ? 'Devolvé un nodo o una relación en RETURN para verla en el grafo. Los valores simples siguen disponibles en Tabla.' : 'Ejecutá una consulta para explorar el resultado.'}</p>
                </div>
              )
            ) : result?.ok ? <ResultsTable headers={result.headers} rows={result.rows || []} /> : (
              <div className="empty-state">
                <h3>{result ? 'No hay filas para mostrar' : 'Ejecutá una consulta'}</h3>
                <p>{result ? 'Corregí la consulta y volvé a ejecutarla.' : 'Las filas aparecerán en esta vista.'}</p>
              </div>
            )}
          </div>

          <footer className="result-footer">
            <span>{result?.ok ? result.message : 'Consultas Cyphail sobre SWI-Prolog'}</span>
            <span>{graph.nodes.length} nodos · {graph.edges.length} relaciones</span>
          </footer>
        </section>
      </main>
    </div>
  )
}
