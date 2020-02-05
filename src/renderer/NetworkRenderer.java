package renderer;

import java.util.ArrayList;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import simulation.Connection;
import simulation.Network;
import simulation.Node;
import util.com.daniel.utils.Point;
import util.com.daniel.utils.Vector;

/**
 *
 * @author Daniel
 */
public class NetworkRenderer implements Renderer {
    
    public static final double NODE_SIZE = 30;
    public static final double WEIGHT_SIZE = 25;
    
    private boolean needCalculation;
    private double t, k;
    
    private Color nodeColor;
    
    private Network network;
    private GraphicsContext gc;
    private Canvas canvas;
    
    private boolean enableLayout;
    
    public NetworkRenderer(Canvas canvas, Network network) {
        this.network = network;
        this.gc = canvas.getGraphicsContext2D();
        this.canvas = canvas;
        
        needCalculation = true;
        
        nodeColor = Color.BLUE;
    }
    
    @Override
    public void reset() {
        softReset();
        
        network.getNodes().stream().forEach((n) -> {
            Point rnd = Point.getRandomPoint(canvas.getWidth() - NODE_SIZE, canvas.getHeight() - NODE_SIZE);
            n.setPos(new Vector(rnd.getX(), rnd.getY()));
        });
    }
    @Override
    public void softReset() {
        double area = Math.min(canvas.getWidth()*canvas.getWidth(), canvas.getHeight()*canvas.getHeight());
        k = 1.5 * Math.sqrt(area/network.getNodes().size());
        t = canvas.getWidth()*canvas.getHeight()/4;
        needCalculation = true;
    }
    
    public void drawNode(Node n) {
        gc.save();
        gc.setFill(nodeColor);
        gc.fillOval(n.getPos().getX(), n.getPos().getY(), NODE_SIZE, NODE_SIZE);
        
        if(n.isSelected()) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
        } else {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
        }
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(n.getName(), n.getPos().getX() + NODE_SIZE/2, n.getPos().getY() + NODE_SIZE/2);
        
        gc.setStroke(Color.BLACK);
        gc.strokeOval(n.getPos().getX(), n.getPos().getY(), NODE_SIZE, NODE_SIZE);
        gc.restore();
    }
    public void drawConnection(Connection c) {
        gc.save();
        
        gc.setStroke(Color.BLACK);
        gc.strokeLine(c.getFrom().getPos().getX() + NODE_SIZE/2, c.getFrom().getPos().getY() + NODE_SIZE/2, c.getTo().getPos().getX() + NODE_SIZE/2, c.getTo().getPos().getY() + NODE_SIZE/2);
        
        gc.restore();
    }
    private void drawWeight(Connection c) {
        gc.save();
        
        
        
        if(c.isChanged()) {
            gc.setFill(Color.RED);
            c.resetChanged();
        } else {
            gc.setFill(Color.BURLYWOOD);
        }
        
        gc.fillOval(c.getWeightPos().getX(), c.getWeightPos().getY(), WEIGHT_SIZE, WEIGHT_SIZE);
        
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(c.getWeight()), c.getWeightPos().getX()+WEIGHT_SIZE/2, c.getWeightPos().getY()+WEIGHT_SIZE/2);
        gc.restore();
    }
    
    private void findNewPositions() {        
        for(int i=0;i<1&&needCalculation;i++) {
            calculateStep();
        }
    }
    private void calculateStep() {
        //repulsive forces
        network.getNodes().forEach(n1 -> {
            n1.getDisplacement().scale(0);
            
            network.getNodes().forEach(n2 -> {
                if(!n1.equals(n2)) {
                    Vector delta = new Vector(n1.getPos().getX(), n1.getPos().getY());
                    delta.sub(n2.getPos());
                    
                    double length = delta.getLength();
                    delta.normalize();
                    
                    double force = k*k/length;
                    
                    delta.scale(force);
                    
                    n1.getDisplacement().add(delta);
                }
            });            
        });
        
        //attractive forces
        ArrayList<Node> fromNodes = new ArrayList<>();
        network.getConnections().forEach(c -> {
            if(!fromNodes.contains(c.getTo())) {
                fromNodes.add(c.getFrom());          
                
                Vector delta = new Vector(c.getFrom().getPos().getX(), c.getFrom().getPos().getY());
                delta.sub(c.getTo().getPos());

                double length = delta.getLength();
                delta.normalize();
                
                if(length != 0) {
                    double force = length*length/k;
                    delta.scale(force);
                }                
                
                c.getFrom().getDisplacement().sub(delta);
                c.getTo().getDisplacement().add(delta);                
            }            
        });
        
        double slowness = 0.05;
        network.getNodes().forEach(n -> {
            if(n.getDisplacement().getLength() > t) {
                n.getDisplacement().normalize();
                n.getDisplacement().scale(t);
            }      
            n.getDisplacement().scale(slowness);
            
            n.getPos().add(n.getDisplacement());
            
            n.getPos().setX(Math.min(canvas.getWidth()-NODE_SIZE, Math.max(0, n.getPos().getX())));
            n.getPos().setY(Math.min(canvas.getHeight()-NODE_SIZE, Math.max(0, n.getPos().getY())));
        });
        
        t = Math.max(t * (1 - 0.065*slowness), 1);
        
        needCalculation = network.getNodes().stream().filter(n -> n.getDisplacement().getLength() > 2).count() > 0;
    }

    @Override
    public void render() {
        if(enableLayout) findNewPositions();
        network.getConnections().forEach(c -> drawConnection(c));
        network.getConnections().forEach(c -> drawWeight(c));
        network.getNodes().forEach(n -> drawNode(n));
    }

    public void toggleEnableLayout() {
        enableLayout = !enableLayout;
    }

    public boolean isEnableLayout() {
        return enableLayout;
    }
    
}
