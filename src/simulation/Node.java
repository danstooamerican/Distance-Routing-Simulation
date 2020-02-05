package simulation;

import java.util.Stack;
import renderer.NetworkRenderer;
import util.com.daniel.utils.Vector;

/**
 *
 * @author Daniel
 */
public class Node implements Cloneable{
    
    private String name;
    private Vector pos;
    private Vector displacement;
    private boolean selected;
    
    private Stack<Table> tables;

    public Node(String name, Vector pos) {
        this.name = name;
        this.pos = pos;
        displacement = new Vector(0, 0);
        
        tables = new Stack<>();
    }

    public String getName() {
        return name;
    }
    
    public boolean containsPoint(Vector point) {
        Vector distanceFromCenterToPoint = new Vector(pos.getX() + NetworkRenderer.NODE_SIZE/2, pos.getY() + NetworkRenderer.NODE_SIZE/2);
        
        distanceFromCenterToPoint.sub(point);
        
        return distanceFromCenterToPoint.getLength() <= NetworkRenderer.NODE_SIZE/2;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    public Vector getPos() {
        return pos;
    }

    public void setPos(Vector pos) {
        this.pos = pos;
    }

    public Vector getDisplacement() {
        return displacement;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Stack<Table> getTables() {
        return tables;
    }
    
    public Table getLastTable() {
        if(tables.size() > 0) {
            return tables.peek();
        } else {
            return new Table();
        }        
    }

    public void setTables(Stack<Table> tables) {
        this.tables = tables;
    }
    
}
