package simulation;

import renderer.NetworkRenderer;
import static renderer.NetworkRenderer.NODE_SIZE;
import util.com.daniel.utils.Vector;

/**
 *
 * @author Daniel
 */
public class Connection {
    
    private Node from, to;
    private int weight;
    
    private boolean changed;
    
    public Connection(Node from, Node to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Node getFrom() {
        return from;
    }
    public Node getTo() {
        return to;
    }
    public int getWeight() {
        return weight;
    }

    public boolean containsPoint(Vector point) {
        Vector distanceFromCenterToPoint = getWeightPos().addN(new Vector(1, 1).scaleN(NetworkRenderer.WEIGHT_SIZE/2));
        
        distanceFromCenterToPoint.sub(point);
        
        return distanceFromCenterToPoint.getLength() <= NetworkRenderer.WEIGHT_SIZE/2;
    }
    public Vector getWeightPos() {
        Vector weightPosDelta = new Vector(from.getPos().getX()+NODE_SIZE/2, from.getPos().getY()+NODE_SIZE/2);
        weightPosDelta.sub(new Vector(to.getPos().getX()+NODE_SIZE/2, to.getPos().getY()+NODE_SIZE/2));
        weightPosDelta.scale(2.0/3);
        weightPosDelta.sub(new Vector(NetworkRenderer.WEIGHT_SIZE/2, NetworkRenderer.WEIGHT_SIZE/2));
        
        Vector weightPos = new Vector(to.getPos().getX()+NODE_SIZE/2, to.getPos().getY()+NODE_SIZE/2);
        weightPos.add(weightPosDelta);
        
        return weightPos;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
        changed = true;
    }
    
    public boolean isChanged() {
        return changed;
    }

    public void resetChanged() {
        changed = false;
    }
}
