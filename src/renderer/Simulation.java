package renderer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import simulation.Network;
import simulation.Node;
import util.com.daniel.utils.Vector;

/**
 *
 * @author Daniel
 */
public class Simulation implements Runnable{
    
    private NetworkRenderer networkRenderer;
    private TableRenderer tableRenderer;
    
    private Network network;
    
    private Canvas networkCanvas;
    private VBox vbxTables;
    private GraphicsContext networkGC;
    
    private Color background;
    private int fps;
    
    private Node selectedNode;
    private Vector mousePos;
    
    public Simulation(Canvas networkCanvas, VBox vbxTables, Network network) {
        this.networkCanvas = networkCanvas;
        this.vbxTables = vbxTables;
        networkGC = networkCanvas.getGraphicsContext2D();
        
        this.network = network;
        networkRenderer = new NetworkRenderer(networkCanvas, network);
        tableRenderer = new TableRenderer(vbxTables, network);
        resetRenderer();
        
        fps = 30;
        background = Color.AZURE;
    }

    public void softResetRenderer() {
        networkRenderer.softReset();
        tableRenderer.softReset();
    }
    
    public void resetRenderer() {
        networkRenderer.reset();
        tableRenderer.reset();
    }

    public NetworkRenderer getNetworkRenderer() {
        return networkRenderer;
    }
    
    public TableRenderer getTableRenderer() {
        return tableRenderer;
    }
    
    @Override
    public void run() {
        while(true) {
            network.update();
            Platform.runLater(() -> {
                performRendering();            
            });
            sleep();
        }
    }
    private void performRendering() {
        networkGC.save();
        networkGC.setFill(background);
        networkGC.fillRect(0, 0, networkCanvas.getWidth(), networkCanvas.getHeight());
        networkGC.restore();
        
        if(selectedNode != null) {
            networkGC.save();
            networkGC.setStroke(Color.BLACK);
            double NODE_SIZE = NetworkRenderer.NODE_SIZE;
            networkGC.strokeLine(selectedNode.getPos().getX() + NODE_SIZE/2, selectedNode.getPos().getY() + NODE_SIZE/2, mousePos.getX(), mousePos.getY());
            networkGC.restore();
        }
        
        networkRenderer.render(); 
        
        if(network.isChanged()) {
            network.calculateTables();
            tableRenderer.render();
            network.resetChanged();
        }
    }  
    
    private void sleep() {
        try {
            Thread.sleep(1000/fps);
        } catch (InterruptedException ex) {
            Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void setMousePos(Vector mousePos) {
        this.mousePos = mousePos;
    }
    
}
