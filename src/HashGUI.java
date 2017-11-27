/*
HashCompute

Calculates random string values, then send array to GPU for hashing.
 */

import com.jogamp.opencl.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import java.util.ArrayList;
import java.util.function.Consumer;

public class HashGUI extends Application {
    // Hashing constants
    static ArrayList<Integer> collisions_per_cell;
    JSObject jso;
    Controller c;
    static GPUDriver gpu;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the GUI FXML.
        FXMLLoader fxml_loader = new FXMLLoader(getClass().getResource("GUI.fxml"));

        // Create a new Controller object.
        c = new Controller();
        // Create a new GPUDriver object to interface with the GPU.
        gpu = new GPUDriver();
        // Set the controller for our GUI to the one we just made.
        fxml_loader.setController(c);
        Parent root = fxml_loader.load();
        primaryStage.setTitle("Birthday Attack Demo");
        primaryStage.setScene(new Scene(root, 960, 720));

        // Get the available list of OpenCL platforms.
        CLPlatform[] platform_list = gpu.getPlatformList();

        // Add each platform to the dropdown menu
        for(CLPlatform plat : platform_list){
            c.getPlatformDropdown().getItems().add(plat.getName() + " (" + plat.version + ")");
        }
        // Select the first OpenCL platform by default.
        c.getPlatformDropdown().getSelectionModel().selectFirst();

        // Handler for the user selecting a new option in the dropdown box.
        c.getPlatformDropdown().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(!newValue.equals(oldValue)){
                    // Get list of platforms
                    CLPlatform[] list = gpu.getPlatformList();
                    // Set platform to the one selected by the user.
                    gpu.setCLPlatform(list[(Integer) newValue]);
                    // Log this in the text log.
                    c.getTextLog().appendText("Platform changed to: " + list[(Integer) newValue].getName()+ ".\n");
                    // Reset the device box to show devices associated with this platform.
                    setUpDeviceBox();
                }
            }
        });

        setUpDeviceBox();

        c.getDeviceDropdown().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(!newValue.equals(oldValue) && !newValue.equals(-1)){
                    // List all devices on this platform.
                    CLDevice[] list = gpu.getDeviceList();
                    // Set the device to the new one selected by the user.
                    gpu.setCLDevice(list[(Integer) newValue]);
                    // Log the change.
                    c.getTextLog().appendText("Device changed to: " + list[(Integer) newValue].getName() + ".\n");
                }
            }
        });
        // Mouse event listener
        c.getStartButton().setOnMouseReleased(event -> {
                    // Create new Task to to run GPU computation in a background thread.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // Use Platform.runLater() to ensure that UI updates happen on the UI thread,
                            // and that the GPU computation doesn't lock up the UI thread.
                            Platform.runLater(()->{
                                c.getStartButton().setDisable(true);
                                c.getTextLog().appendText("Using Platform: " + gpu.platform.getName() + "\nUsing Device: " + gpu.device.getName() + "\n");
                            });
                            // Calculate GPU hashes.
                            ArrayList<Integer> arr = gpu.calculateHashes();
                            Platform.runLater(()-> {
                                // Print out log text.
                                c.getTextLog().appendText("Compute Complete.\nThere were " + gpu.getCollisions() + " collisions.\nTable has " + gpu.getHashMapSize() + " entries.\n");
                                // Update the grid on screen.
                                jso.call("setCells", new Object[]{arr.toArray()});
                                jso.call("updateGrid", new Object());
                                // Add each collision to the table for users to see.
                                c.getCollisionTable().getItems().addAll(gpu.getBatch_collisions());

                                // Fill in the number of hashes performed.
                                c.getHashesPerformed().clear();
                                c.getHashesPerformed().appendText(Integer.toString(gpu.getHashesPerformed()));

                                // Fill in the number of collisions found.
                                c.getCollisionsFound().clear();
                                c.getCollisionsFound().appendText(Integer.toString(gpu.getCollisions()));

                                c.getCollisionPct().clear();
                                c.getCollisionPct().appendText(String.format("%,.5f%%\n",((double) gpu.getCollisions() / gpu.getHashesPerformed()) * 100));

                                // Re-enable the button.
                                c.getStartButton().setDisable(false);
                            });
                        }
                    }).start();
                }
        );
        // Set up the columns of the table so that text values are displayed correctly.
        c.getString1Column().setCellValueFactory(new PropertyValueFactory<Collision,String>("value_one"));
        c.getString2Column().setCellValueFactory(new PropertyValueFactory<Collision,String>("value_two"));
        c.getHashValueColumn().setCellValueFactory(new PropertyValueFactory<Collision,String>("hash_value"));

        c.getCollisionTable().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // On mouse click, get the selected row, and copy it over to the hash collision checker.
                Collision temp_col = (Collision) c.getCollisionTable().getSelectionModel().getSelectedItem();
                if(temp_col != null) {
                    // Clear both fields first.
                    c.getString1Field().clear();
                    c.getString2Field().clear();

                    // Add each string to the appropriate field.
                    c.getString1Field().appendText(temp_col.value_one.get());
                    c.getString2Field().appendText(temp_col.value_two.get());
                }
            }
        });

        c.getRunHashButton().setOnMouseReleased((event) -> {
            c.getHashResultField().clear();
            String h1 = new String();
            String h2 = new String();
            if (!c.getString1Field().getText().isEmpty()){
                h1 = FNVHash.hash(c.getString1Field().getText());
                c.getHashResultField().appendText("String 1 Hash: " + h1 + "\n");
            }
            if (!c.getString2Field().getText().isEmpty()){
                h2 = FNVHash.hash(c.getString2Field().getText());
                c.getHashResultField().appendText("String 2 Hash: " + h2 + "\n");
            }
            if(h1.equals(h2)){
                c.getHashResultField().appendText("Collision Occurred!");
            }



        });
        primaryStage.setResizable(false);
        primaryStage.show();
        // Wait until the page is fully loaded before continuing.
        c.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    postLoad();
                }
            }
        });
    }

    public void setUpDeviceBox(){
        CLDevice[] devices_list = gpu.getDeviceList();
        c.getDeviceDropdown().getItems().clear();
        for(CLDevice dev : devices_list){
            c.getDeviceDropdown().getItems().add(dev.getName());
        }
        c.getDeviceDropdown().getSelectionModel().selectFirst();
    }
    // Function runs after page has been loaded.
    public void postLoad() {
        // Get reference to the window.
        jso = (JSObject) c.getEngine().executeScript("window");
        // Use the JSObject to call the initialize function on our html page.
        jso.call("initialize", new Object());
    }


    public static void main(String[] args) {
        launch(args);
        gpu.releaseContext();
    }
}
