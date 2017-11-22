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
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 960, 720));

        // Mouse event listener
        c.getStartButton().setOnMouseReleased(event -> {
                    // Create new Task to to run GPU computation in a background thread.
                    new Thread(new Task<Void>() {
                        @Override
                        protected Void call(){
                            // Use Platform.runLater() to ensure that UI updates happen on the UI thread,
                            // and that the GPU computation doesn't lock up the UI thread.
                            Platform.runLater(()->{c.getStartButton().setDisable(true);});
                            // Calculate GPU hashes.
                            ArrayList<Integer> arr = gpu.calculateHashes();
                            // Update the grid on screen and re-enable the hash button.
                            Platform.runLater(()-> {
                                jso.call("setCells", new Object[]{arr.toArray()});
                                jso.call("updateGrid", new Object());
                                c.getStartButton().setDisable(false);
                            });
                            return null;
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
