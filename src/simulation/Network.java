package simulation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import renderer.NetworkRenderer;
import util.com.daniel.utils.Mathe;
import util.com.daniel.utils.Vector;

/**
 *
 * @author Daniel
 */
public class Network {
    
    private int nodeCnt;
    
    private final ArrayList<Node> nodes;
    private final ArrayList<Connection> connections;
    
    private boolean changed;
    private boolean update;
    
    
    private final int maxNodes = 9;
    
    public Network() {
        nodes = new ArrayList<>();
        connections = new ArrayList<>();
        
        changed = true;
        
        addNode(50, 50);
        addNode(70, 70);
        addNode(10, 10);
        addNode(100, 100);
        
        addConnection(nodes.get(0), nodes.get(1), 3);
        addConnection(nodes.get(0), nodes.get(2), 23);
        addConnection(nodes.get(1), nodes.get(2), 2);
        addConnection(nodes.get(2), nodes.get(3), 5);
    }
    
    public final void update() {
        if (update && (connections.size() > 0) && (Mathe.randomDouble(0, 1) < 0.15)) {
            int index = Mathe.randomInt(0, connections.size() - 1);
            connections.get(index).setWeight(Mathe.randomInt(1, 50));
            changed = true;
        }
    }
    
    public final Node addNode(double x, double y) {
        return addNode(generateNodeName(), x, y);
    }
    public final Node addNode(String name, double x, double y) {
        Node n = nodes.stream()
                .filter(n1 -> n1.getName().equals(name))
                .findFirst()
                .orElse(null);
        
        if ((n == null) && (nodes.size() < maxNodes)) {
            n = new Node(name, new Vector(x, y));
            nodes.add(n);
            nodeCnt++;
            changed = true;        
        }        
        return n;
    }
    private final String generateNodeName() {
        int maxLength = 2;
        StringBuilder sb = new StringBuilder(maxLength);
        
        int cnt = nodeCnt;
        for (int i = 0; (i < maxLength) && (cnt >= 0); i++) {
            int character = -1;
            
            if (cnt > 25) {
                while ((cnt > 25) && (character < 25)) {
                    character++;
                    cnt -= 26;
                }
                sb.append((char) (character + 65));
            } else {
                sb.append((char) (cnt + 65));
                cnt = -1;
            }
        }        
        
        return sb.toString();
    }    
    public final void removeNode(Node n) {
        nodes.remove(n);
        List<Connection> toDelete = connections.stream()
                .filter(c -> c.getFrom().equals(n) || c.getTo().equals(n))
                .collect(toList());
        
        toDelete.forEach(c -> connections.remove(c));
        
        changed = true;
    }    
    public final void addConnection(Node a, Node b, int weight) {
        if (a.equals(b)) {
            return;
        }
        
        if (connections.stream().filter(c -> c.getFrom().equals(a) && c.getTo().equals(b)).count() == 0) {
            connections.add(new Connection(a, b, weight));  
            changed = true;
        }
    }
    
    public final void calculateTables() {    
        nodes.forEach(n -> {
            n.getTables().clear();
            n.getTables().push(createNewTableWithNeighborConnections(n));
        });

        for (int i = 0; i < nodes.size() - 1; i++) {
            ArrayList<Table> finishedTables = new ArrayList<>();            

            for (Node start : nodes) {
                Table table = createNewTableWithNeighborConnections(start);

                List<Node> neighbors = getNeighbors(start).stream().map(c -> c.getTo()).collect(toList());
                for (Node neigh : neighbors) {
                    for (Node goal : nodes) {
                        if (goal != start) {
                            Integer cost = neigh.getLastTable().getLowestCostFor(goal);
                            if (cost != null) {
                                table.addGoalCost(neigh, goal, cost);
                            }
                        }
                    }
                }

                finishedTables.add(table);
            }
            
            for (int j = 0; j < finishedTables.size(); j++) {
                nodes.get(j).getTables().push(finishedTables.get(j));
            }
        }
    }
    private final Table createNewTableWithNeighborConnections(Node n) {
        Table table = new Table();
        getNeighbors(n).stream().forEach((c) -> {
            table.addNeighbor(c.getTo(), c.getWeight());
        });   
        
        return table;
    }
    private final List<Connection> getNeighbors(Node n) {
        return connections.stream().filter(c -> c.getFrom().equals(n)).collect(toList());
    }
    
    public final Node getNodeFromPosition(double x, double y) {
        return nodes.stream().filter(n -> n.containsPoint(new Vector(x, y))).findFirst().orElse(null);
    }    
    public final Connection getConnectionFromPosition(double x, double y) {
        return connections.stream().filter(c -> c.containsPoint(new Vector(x, y))).findFirst().orElse(null);
    }
    
    public final ArrayList<Node> getNodes() {
        return nodes;
    }
    public final ArrayList<Connection> getConnections() {
        return connections;
    }
    
    public final void resetNodeCnt() {
        nodeCnt = 0;
    }

    public final boolean isUpdate() {
        return update;
    }

    public final void toggleUpdate() {
        update = !update;
    }

    public final boolean isChanged() {
        return changed;
    }

    public final void setChanged(boolean changed) {
        this.changed = changed;
    }

    public final void resetChanged() {
        changed = false;
    }
    
    public final void serialize(String path) {
        try (BufferedWriter bw = new BufferedWriter(new PrintWriter(path))) {
            for (Connection c : connections) {
                bw.write(c.getFrom() + "," + c.getTo() + "," + c.getWeight());
                bw.newLine();
            }            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public final void deserialize(String path, Vector screenDimensions) {
        nodes.clear();
        connections.clear();

        List<String> lines = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            lines = stream.collect(toList());                                     
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        } 

        lines.forEach(l -> {
            String[] parts = l.split(",");
            Node n1 = addNode(parts[0], Mathe.randomDouble(0, screenDimensions.getX() - NetworkRenderer.NODE_SIZE), 
                    Mathe.randomDouble(0, screenDimensions.getY() - NetworkRenderer.NODE_SIZE));
            Node n2 = addNode(parts[1], Mathe.randomDouble(0, screenDimensions.getX() - NetworkRenderer.NODE_SIZE), 
                    Mathe.randomDouble(0, screenDimensions.getY() - NetworkRenderer.NODE_SIZE));
            addConnection(n1, n2, Integer.valueOf(parts[2]));
        });
        
        changed = true;
    }
    
}
