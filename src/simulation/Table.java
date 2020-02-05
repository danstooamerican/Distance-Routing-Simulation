package simulation;

import java.util.HashMap;

/**
 *
 * @author Daniel
 */
public class Table implements Cloneable {
    
    //first key = neighbor nodes, second is all goals
    private HashMap<Node, HashMap<Node, Integer>> costs;
    
    public Table() {
        costs = new HashMap<>();
    }
    
    public void addNeighbor(Node n, int cost) {
        HashMap<Node, Integer> goalCosts = new HashMap<>();
        goalCosts.put(n, cost);
        
        costs.put(n, goalCosts);
    }
    
    public void addGoalCost(Node neighbor, Node goal, int costFromNeighborToGoal) {
        if(costs.get(neighbor).containsKey(goal)) {
            if(costs.get(neighbor).get(goal) > costFromNeighborToGoal+getCostToNeighbor(neighbor)) {
                costs.get(neighbor).put(goal, costFromNeighborToGoal+getCostToNeighbor(neighbor));
            }
        } else {
            costs.get(neighbor).put(goal, costFromNeighborToGoal+getCostToNeighbor(neighbor));
        }        
    }
    private int getCostToNeighbor(Node n) {
        return costs.get(n).get(n);
    }

    public Integer getLowestCostFor(Node node) {
        Integer smallestWeight = null;
        
        for(Node n : costs.keySet()) {
            if(costs.get(n).containsKey(node)) {
                if(smallestWeight == null || smallestWeight > costs.get(n).get(node)) {
                    smallestWeight = costs.get(n).get(node);
                }  
            }
        }
        
        return smallestWeight;
    } 

    public HashMap<Node, HashMap<Node, Integer>> getCosts() {
        return costs;
    }
    
    @Override
    public String toString() {
        String out = "";
        
        for(Node n : costs.keySet()) {
            System.out.println("- " + n.getName());
            for(Node g : costs.get(n).keySet()) {
                System.out.println(g.getName());
            }
            System.out.println("");
        }
        
        return out;
    }

    public void setCosts(HashMap<Node, HashMap<Node, Integer>> costs) {
        this.costs = costs;
    }
    
}
