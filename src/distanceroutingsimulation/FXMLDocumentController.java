package distanceroutingsimulation;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import renderer.NetworkRenderer;
import renderer.Simulation;
import simulation.Connection;
import simulation.Network;
import simulation.Node;
import util.com.daniel.utils.Dialogs;
import util.com.daniel.utils.Mathe;
import util.com.daniel.utils.Vector;

/**
 *
 * @author Daniel
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML    
    private Canvas cnvNetwork, cnvTables;
    @FXML
    private VBox vbxOptions, vbxTables;
    @FXML
    private Button btnEnableLayout, btnEnableSimulation, btnClear, btnLoad, btnSave;
    @FXML
    private ScrollPane scpTables;
    @FXML
    private TextField txbTableSize;
    
    private Network network;
    private Simulation simulation;
    
    private Node selectedNode;    
    private boolean dragged;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {            
        network = new Network();
        initGraphics(); 
        initControls();  
        
        network.calculateTables();
        simulation.softResetRenderer();
    }    
    private void initGraphics() {        
        Platform.runLater(() -> {
            Scene scene = cnvNetwork.getScene();
            scene.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
                updateCanvasLayout(adjustNetworkCanvasX(newSceneWidth.doubleValue()), cnvNetwork.getHeight());
            });
            scene.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) -> {
                updateCanvasLayout(adjustNetworkCanvasX(scene.getWidth()), newSceneHeight.doubleValue());
            });
        });   
        
        simulation = new Simulation(cnvNetwork, vbxTables, network);
        Thread renderer = new Thread(simulation);
        renderer.setDaemon(true);
        renderer.start();        
    }
    private void initControls() {
        cnvNetwork.setOnMouseClicked((event) -> {
            if(dragged) {
                dragged = false;
                unselectNode();
            } else {
                Node clickedNode = network.getNodeFromPosition(adjustNetworkCanvasX(event.getSceneX()), event.getSceneY());
            
                if(event.isShiftDown()) {
                    Connection c = network.getConnectionFromPosition(adjustNetworkCanvasX(event.getSceneX()), event.getSceneY());
                    
                    if(c != null) {
                        if(event.getButton().equals(MouseButton.PRIMARY)) {
                            TextInputDialog d = new TextInputDialog(String.valueOf(Mathe.randomInt(1, 10)));
                            d.setTitle("Enter new weight");
                            d.setHeaderText("Gewichte müssen Integer sein");

                            d.showAndWait().ifPresent(weight -> {
                                if(weight.matches("[0-9]+")) {
                                    c.setWeight(Integer.parseInt(weight));
                                    network.setChanged(true);
                                }
                            });
                        } else if(event.getButton().equals(MouseButton.SECONDARY)) {
                            network.getConnections().remove(c);
                            network.setChanged(true);
                        }                        
                    }
                } else if(event.getButton() == MouseButton.PRIMARY) {
                    if(clickedNode == null) {
                        network.addNode(adjustNetworkCanvasX(event.getSceneX()) - NetworkRenderer.NODE_SIZE/2, event.getSceneY() - NetworkRenderer.NODE_SIZE/2);
                        unselectNode();
                    } else {
                        if(selectedNode == null) {
                            selectNode(clickedNode);
                        } else {
                            network.addConnection(selectedNode, clickedNode, Mathe.randomInt(1, 6));
                            unselectNode();
                        }
                    }
                } else if(event.getButton() == MouseButton.SECONDARY) {
                    if(clickedNode != null) {
                        network.removeNode(clickedNode);
                        unselectNode();
                    }
                }
            }
            simulation.softResetRenderer();            
        });
        
        cnvNetwork.setOnMouseDragged(event -> {
            if(!dragged && selectedNode == null) {
                selectedNode = network.getNodeFromPosition(adjustNetworkCanvasX(event.getSceneX()), event.getSceneY());
            }
            
            if(selectedNode != null) {
                dragged = true;
                selectedNode.setPos(new Vector(adjustNetworkCanvasX(event.getSceneX()) - NetworkRenderer.NODE_SIZE/2, event.getSceneY() - NetworkRenderer.NODE_SIZE/2));
            }
        });
        
        cnvNetwork.setOnMouseMoved(event -> {
            simulation.setMousePos(new Vector(adjustNetworkCanvasX(event.getSceneX()), event.getSceneY()));
        });
        
        btnEnableLayout.setOnAction((event) -> {
            simulation.getNetworkRenderer().toggleEnableLayout();
            if(simulation.getNetworkRenderer().isEnableLayout()) {
                btnEnableLayout.setText("Disable Layout");
            } else {
                btnEnableLayout.setText("Enable Layout");
            }
        });
        
        btnEnableSimulation.setOnAction(event -> {
            network.toggleUpdate();
            if(network.isUpdate()) {
                btnEnableSimulation.setText("Disable Simulation");
            } else {
                btnEnableSimulation.setText("Enable Simulation");
            }
        });
        
        btnClear.setOnAction((event) -> {
            network.getNodes().clear();
            network.getConnections().clear();
            network.resetNodeCnt();
        });
        
        btnLoad.setOnAction(event -> {
            String path = Dialogs.chooseFileDialog("Load");
            
            if(path != null && !path.isEmpty()) {
                network.deserialize(path, new Vector(cnvNetwork.getWidth(), cnvNetwork.getHeight()));
                network.calculateTables();
                simulation.softResetRenderer();  
            }
        });
        
        btnSave.setOnAction(event -> {
            File file = Dialogs.saveFileDialog("Save");
            
            if(file != null) {
                network.serialize(file.getPath());
            }
        });
        
        txbTableSize.textProperty().addListener((observable, oldValue, newValue) -> {
            final int minSize = 150;
            final int maxSize = 500;
            if (newValue.matches("[0-9]+")) {
                int tableSize = Integer.parseInt(newValue);
                
                if (tableSize >= minSize) {
                    if (tableSize <= maxSize) {
                        simulation.getTableRenderer().setTableSize(tableSize);
                    }            
                }
            } else {
                txbTableSize.setText(oldValue);
            }
        });
        
        txbTableSize.setTooltip(new Tooltip("Table Size ranges from 150px-500px"));
    }
    private void selectNode(Node node) {
        selectedNode = node;
        simulation.setSelectedNode(selectedNode);
        node.setSelected(true);
    }
    private void unselectNode() {
        if(selectedNode != null) {
            selectedNode.setSelected(false);
            simulation.setSelectedNode(null);
            selectedNode = null;
        }
    }
    
    private double adjustNetworkCanvasX(double sceneX) {
        return sceneX -vbxOptions.getWidth();
    }
    
    private void updateCanvasLayout(double width, double height) {
        cnvNetwork.setHeight(height);
        cnvNetwork.setWidth(width/3);
        scpTables.setPrefWidth(width/3*2);
        scpTables.setPrefHeight(height);
        simulation.softResetRenderer();
    }
    
}
