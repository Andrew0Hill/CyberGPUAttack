/*
HashCompute

Calculates random string values, then send array to GPU for hashing.
 */

import com.jogamp.opencl.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HashGUI extends Application {
    // Hashing constants
    static ArrayList<Integer> collisions_per_cell;
    JSObject jso;
    Controller c;
    static GPUDriver gpu;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxml_loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
        c = new Controller();
        gpu = new GPUDriver();
        fxml_loader.setController(c);
        Parent root = fxml_loader.load();
        primaryStage.setTitle("Birthday Attack Demo");
        primaryStage.setScene(new Scene(root, 960, 720));

        CLPlatform[] platform_list = gpu.getPlatformList();
        for(CLPlatform plat : platform_list){
            c.getPlatformDropdown().getItems().add(plat.getName() + " (" + plat.version + ")");
        }
        c.getPlatformDropdown().getSelectionModel().selectFirst();

        c.getPlatformDropdown().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(!newValue.equals(oldValue)){
                    CLPlatform[] list = gpu.getPlatformList();
                    gpu.setCLPlatform(list[(Integer) newValue]);
                    c.getTextLog().appendText("Platform changed to: " + list[(Integer) newValue].getName()+ ".\n");
                    setUpDeviceBox();
                    c.getDeviceDropdown().getSelectionModel().selectFirst();
                }
            }
        });

        setUpDeviceBox();

        c.getDeviceDropdown().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(!newValue.equals(oldValue) && !newValue.equals(-1)){
                    CLDevice[] list = gpu.getDeviceList();
                    gpu.setCLDevice(list[(Integer) newValue]);
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
                            // Update the grid on screen and re-enable the hash button.
                            Platform.runLater(()-> {
                                c.getTextLog().appendText("Compute Complete.\nThere were " + gpu.getCollisions() + " collisions.\nTable has " + gpu.getHashMapSize() + " entries.\n");
                                jso.call("setCells", new Object[]{arr.toArray()});
                                jso.call("updateGrid", new Object());
                                c.getStartButton().setDisable(false);
                            });
                        }
                    }).start();
                }
        );
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


        /*
         * Use JSObject to call the "test" Javascript method.
         * The 'call' method expects an Object[] containing each argument.
         * To pass an array as a parameter, we create a new Object[] with the first element
         * being a Integer[].
         */
        jso.call("initialize", new Object());
    }


    public static void main(String[] args) {
        launch(args);
        gpu.releaseContext();
    }
}
