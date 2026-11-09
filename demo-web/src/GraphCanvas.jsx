import { useEffect, useRef } from 'react'
import cytoscape from 'cytoscape'

const nodeColors = {
  Persona: '#177c89',
  Person: '#177c89',
  Empresa: '#9367b2',
  Avanzada: '#c47a49',
  Intermedia: '#bd9858',
  Principiante: '#7e9d74',
}

function elementsFor(graph) {
  const nodes = graph.nodes.map((node) => ({
    group: 'nodes',
    data: {
      id: node.id,
      label: node.label,
      color: node.labels.map((label) => nodeColors[label]).find(Boolean) || '#54768d',
    },
  }))
  const edges = graph.edges.map((edge) => ({
    group: 'edges',
    data: {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      label: edge.type,
    },
  }))
  return [...nodes, ...edges]
}

export default function GraphCanvas({ graph, onSelect, fitRef }) {
  const containerRef = useRef(null)

  useEffect(() => {
    if (!containerRef.current || graph.nodes.length === 0) {
      return undefined
    }

    const compact = containerRef.current.clientWidth < 600
    const network = cytoscape({
      container: containerRef.current,
      elements: elementsFor(graph),
      layout: graph.nodes.length <= 2
        ? { name: 'grid', rows: 1, fit: false }
        : compact ? { name: 'circle', radius: 140, fit: false, startAngle: -Math.PI / 2 }
        : {
            name: 'breadthfirst',
            directed: true,
            fit: false,
            spacingFactor: 1.5,
            padding: 80,
          },
      minZoom: 0.35,
      maxZoom: 2.5,
      wheelSensitivity: 0.17,
      style: [
        {
          selector: 'node',
          style: {
            'background-color': 'data(color)',
            'background-opacity': 1,
            'border-width': 5,
            'border-color': '#ffffff',
            width: 66,
            height: 66,
            label: 'data(label)',
            color: '#263749',
            'font-family': 'IBM Plex Sans',
            'font-size': 15,
            'font-weight': 600,
            'text-valign': 'bottom',
            'text-margin-y': 13,
            'text-wrap': 'ellipsis',
            'text-max-width': 110,
            'overlay-opacity': 0,
          },
        },
        {
          selector: 'edge',
          style: {
            width: 2.5,
            'line-color': '#a0adb9',
            'target-arrow-color': '#a0adb9',
            'target-arrow-shape': 'triangle',
            'arrow-scale': 1.15,
            'curve-style': 'bezier',
            label: 'data(label)',
            color: '#617184',
            'font-family': 'IBM Plex Mono',
            'font-size': 10,
            'text-rotation': 'autorotate',
            'text-background-color': '#f7f9fa',
            'text-background-opacity': 1,
            'text-background-padding': '4px',
            'overlay-opacity': 0,
          },
        },
        {
          selector: ':selected',
          style: {
            'border-color': '#d6a348',
            'border-width': 6,
            'line-color': '#d6a348',
            'target-arrow-color': '#d6a348',
          },
        },
      ],
    })

    network.on('tap', 'node, edge', (event) => {
      const selected = event.target
      const kind = selected.isNode() ? 'node' : 'edge'
      const item = kind === 'node'
        ? graph.nodes.find((node) => node.id === selected.id())
        : graph.edges.find((edge) => edge.id === selected.id())
      onSelect(item ? { kind, item } : null)
    })
    network.on('tap', (event) => {
      if (event.target === network) {
        onSelect(null)
      }
    })
    const fitGraph = () => {
      network.resize()
      network.fit(network.elements(), compact ? 24 : 72)
      if (network.zoom() > 1.25) {
        network.zoom(1.25)
      }
      network.center()
    }
    fitRef.current = fitGraph
    const resizeObserver = new ResizeObserver(fitGraph)
    resizeObserver.observe(containerRef.current)
    requestAnimationFrame(fitGraph)

    return () => {
      fitRef.current = null
      resizeObserver.disconnect()
      network.destroy()
    }
  }, [graph, onSelect, fitRef])

  return <div ref={containerRef} className="graph-canvas" aria-label="Grafo interactivo" />
}
