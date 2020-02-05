package renderer;

import java.util.HashMap;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import simulation.Network;
import simulation.Node;

/**
 *
 * @author Daniel
 */
public class TableRenderer implements Renderer {
    
    private Network network;
    private VBox vbxTables;

    private double tableSize = 170;
    
    private double cellSize;
    private boolean fullRender;

    public TableRenderer(VBox vbxTables, Network network) {
        this.vbxTables = vbxTables;
        setData(network);     
        fullRender = true;
    }
    
    public final void setData(Network network) {
        this.network = network;
        softReset();
    }
    
    @Override
    public void render() {    
        if (fullRender) {
            fullRender();
            fullRender = false;
        } else {
            minimalRender();
        }        
    }
    private void fullRender() {
        vbxTables.getChildren().clear();        
        for (int row = 0; row < network.getNodes().size(); row++) {
            HBox hbx = new HBox();
            hbx.setPrefWidth(vbxTables.getPrefWidth());
            hbx.setPrefHeight(vbxTables.getPrefHeight());
            for (int col = 0; col < network.getNodes().get(row).getTables().size(); col++) {
                Canvas canvas = new Canvas(tableSize, tableSize);
                drawHeader(canvas.getGraphicsContext2D(), network.getNodes().get(row), col);
                drawDistances(canvas.getGraphicsContext2D(), network.getNodes().get(row), col);             
                hbx.getChildren().add(canvas);
            }
            vbxTables.getChildren().add(hbx);
        } 
    }
    private void minimalRender() {
        for (int row = 0; row < network.getNodes().size(); row++) {
            HBox hbx = (HBox) vbxTables.getChildren().get(row);
            
            for (int col = 0; col < network.getNodes().get(row).getTables().size(); col++) {
                Canvas canvas = (Canvas) hbx.getChildren().get(col);
                drawDistances(canvas.getGraphicsContext2D(), network.getNodes().get(row), col);            
            }
        } 
    }

    private void drawHeader(GraphicsContext gc, Node start, int t) {          
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        
        gc.setFill(Color.LIGHTSLATEGRAY);
        gc.fillRect(0, 0, cellSize, cellSize);
        gc.strokeRect(0, 0, cellSize, cellSize);
        
        gc.setFill(Color.BLACK);
        gc.fillText(start.getName() + " t=" + t, cellSize / 2, cellSize / 2);       
        
        //header
        for (int i = 1; i <= network.getNodes().size(); i++) {
            gc.setFill(Color.LIGHTGREY);            
            gc.fillRect(cellSize * i, 0, cellSize, cellSize);
            gc.setFill(Color.BLACK);
            gc.fillText(network.getNodes().get(i - 1).getName(), cellSize * i + cellSize / 2, cellSize / 2);
            gc.strokeRect(cellSize * i, 0, cellSize, cellSize);
        }
        
        //side header
        for (int i = 1; i <= network.getNodes().size(); i++) {
            gc.setFill(Color.LIGHTGREY);   
            gc.fillRect(0, cellSize * i, cellSize, cellSize);
            gc.setFill(Color.BLACK);   
            gc.fillText(network.getNodes().get(i - 1).getName(), cellSize / 2, cellSize * i + cellSize / 2);
            gc.strokeRect(0, cellSize * i, cellSize, cellSize);
        }
    }
    
    private void drawDistances(GraphicsContext gc, Node start, int t) {
        int[][] matrix = createMatrix(start, t);
        
        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[y].length; x++) {
                gc.clearRect((x + 1) * cellSize, (y + 1) * cellSize, cellSize, cellSize);
                if (matrix[x][y] <= 0) {                    
                    gc.fillText("X", (x + 1) * cellSize + cellSize / 2, 
                            (y + 1) * cellSize + cellSize / 2);                    
                } else {
                    if (isBestOption(start, network.getNodes().get(x), network.getNodes().get(y), t)) {
                        gc.save();
                        gc.setFill(Color.LIGHTGREEN);
                        gc.fillRect((x + 1) * cellSize, (y + 1) * cellSize, cellSize, cellSize);
                        gc.setLineWidth(2);
                        gc.strokeRect((x + 1) * cellSize, (y + 1) * cellSize, cellSize, cellSize);
                    } else {
                        gc.save();
                        gc.setFill(Color.BURLYWOOD);
                        gc.fillRect((x + 1) * cellSize, (y + 1) * cellSize, cellSize, cellSize);
                        gc.restore();
                    }
                    gc.restore();                    
                    gc.fillText(String.valueOf(matrix[x][y]), 
                            cellSize * (x + 1) + cellSize / 2, cellSize * (y + 1) + cellSize / 2);
                }  
                gc.strokeRect((x + 1) * cellSize, (y + 1) * cellSize, cellSize, cellSize);
            }
        }
        
    }
    private boolean isBestOption(Node start, Node via, Node target, int t) {
        int cost = start.getTables().get(t).getCosts().get(via).get(target);
        
        for (Node n : start.getTables().get(t).getCosts().keySet()) {
            if (!n.equals(via)) {
                if (start.getTables().get(t).getCosts().get(n).containsKey(target)) {
                    if (cost > start.getTables().get(t).getCosts().get(n).get(target)) {
                        return false;
                    }
                }                
            }
        }
        
        return true;
    }
    
    private int[][] createMatrix(Node start, int t) {
        int[][] matrix = new int[network.getNodes().size()][network.getNodes().size()];

        HashMap<Node, HashMap<Node, Integer>> costs = start.getTables().get(t).getCosts();
        for (Node n : costs.keySet()) {
            int x = network.getNodes().indexOf(n);
            
            for (Node n1 : costs.get(n).keySet()) {
                int y = network.getNodes().indexOf(n1);
                
                matrix[x][y] = costs.get(n).get(n1);
            }
        }
        
        return matrix;
    }

    @Override
    public void softReset() {
        vbxTables.setPrefWidth(network.getNodes().size() * tableSize);
        vbxTables.setPrefHeight(tableSize);
        
        cellSize = tableSize / (network.getNodes().size() + 1);
        
        fullRender = true;
    }

    @Override
    public void reset() {
        
    }

    public void setTableSize(double tableSize) {
        this.tableSize = tableSize;
        softReset();
        render();
    }
    
}
