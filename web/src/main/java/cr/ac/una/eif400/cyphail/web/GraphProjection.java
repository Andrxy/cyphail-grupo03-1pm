package cr.ac.una.eif400.cyphail.web;

import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.common.QueryResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class GraphProjection {
    record Node(String id, String label, List<String> labels, Map<String, String> properties) {
    }

    record Edge(String id, String source, String target, String type, Map<String, String> properties) {
    }

    record Graph(List<Node> nodes, List<Edge> edges) {
    }

    private record EdgeData(String source, String target, String type) {
    }

    private GraphProjection() {
    }

    static Graph from(Broker broker, QueryResponse result) {
        if (!result.isOk() || result.getRows() == null || result.getRows().length == 0) {
            return new Graph(List.of(), List.of());
        }

        Map<String, Set<String>> labels = new LinkedHashMap<>();
        for (String[] row : rows(broker, "cy_exec_query(cy_node(Id), ['id'], [Id], Rows)")) {
            labels.put(row[0], new LinkedHashSet<>());
        }

        for (String[] row : rows(broker, "cy_exec_query(cy_node_label(Id, Label), ['id', 'label'], [Id, Label], Rows)")) {
            labels.computeIfAbsent(row[0], ignored -> new LinkedHashSet<>()).add(row[1]);
        }

        Map<String, EdgeData> edges = new LinkedHashMap<>();
        for (String[] row : rows(broker, "cy_exec_query((cy_edge(Id, Source, Target), cy_edge_type(Id, Type)), ['id', 'source', 'target', 'type'], [Id, Source, Target, Type], Rows)")) {
            edges.put(row[0], new EdgeData(row[1], row[2], row[3]));
        }

        Set<String> nodeIds = new LinkedHashSet<>();
        Set<String> edgeIds = new LinkedHashSet<>();
        for (String[] row : result.getRows()) {
            for (String value : row) {
                if (labels.containsKey(value)) {
                    nodeIds.add(value);
                }
                if (edges.containsKey(value)) {
                    edgeIds.add(value);
                }
            }
        }

        for (String edgeId : edgeIds) {
            EdgeData edge = edges.get(edgeId);
            nodeIds.add(edge.source());
            nodeIds.add(edge.target());
        }

        Map<String, Map<String, String>> nodeProperties = new LinkedHashMap<>();
        for (String[] row : rows(broker, "cy_exec_query((get_current_graph(G), node_prop(G, Id, Key, Value)), ['id', 'key', 'value'], [Id, Key, Value], Rows)")) {
            if (nodeIds.contains(row[0])) {
                nodeProperties.computeIfAbsent(row[0], ignored -> new LinkedHashMap<>()).put(row[1], row[2]);
            }
        }

        Map<String, Map<String, String>> edgeProperties = new LinkedHashMap<>();
        for (String[] row : rows(broker, "cy_exec_query((get_current_graph(G), edge_prop(G, Id, Key, Value)), ['id', 'key', 'value'], [Id, Key, Value], Rows)")) {
            if (edgeIds.contains(row[0])) {
                edgeProperties.computeIfAbsent(row[0], ignored -> new LinkedHashMap<>()).put(row[1], row[2]);
            }
        }

        List<Node> selectedNodes = new ArrayList<>();
        for (String id : nodeIds) {
            Map<String, String> properties = nodeProperties.getOrDefault(id, Map.of());
            String label = properties.getOrDefault("nombre", properties.getOrDefault("name", id));
            selectedNodes.add(new Node(id, label, List.copyOf(labels.getOrDefault(id, Set.of())), properties));
        }

        List<Edge> selectedEdges = new ArrayList<>();
        for (String id : edgeIds) {
            EdgeData edge = edges.get(id);
            selectedEdges.add(new Edge(id, edge.source(), edge.target(), edge.type(),
                    edgeProperties.getOrDefault(id, Map.of())));
        }

        return new Graph(selectedNodes, selectedEdges);
    }

    private static List<String[]> rows(Broker broker, String goal) {
        QueryResponse response = broker.resolveQuery(goal);
        if (!response.isOk()) {
            throw new IllegalStateException(response.getMessage());
        }
        return Arrays.asList(response.getRows());
    }
}
