/*
HashCompute

Calculates random string values, then send array to GPU for hashing.
 */
import com.jogamp.opencl.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    static JSObject jso;
    Controller c;
    static GPUDriver gpu;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxml_loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
        c = new Controller();
        gpu = new GPUDriver();
        fxml_loader.setController(c);
        Parent root = fxml_loader.load();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 960, 720));

        c.getStartButton().setOnMouseReleased(event -> {
            jso.call("setCells",new Object[]{gpu.calculateHashes().toArray()});
            jso.call("updateGrid",new Object());
            //gpu.releaseContext();
        }
            );
        primaryStage.setResizable(false);
        primaryStage.show();
        // Wait until the page is fully loaded before continuing.
        c.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if(newValue == Worker.State.SUCCEEDED){
                    postLoad();
                }
            }
        });
    }
    // Function runs after page has been loaded.
    public void postLoad(){
        // Get reference to the window.
        jso = (JSObject) c.getEngine().executeScript("window");


        /*
         * Use JSObject to call the "test" Javascript method.
         * The 'call' method expects an Object[] containing each argument.
         * To pass an array as a parameter, we create a new Object[] with the first element
         * being a Integer[].
         */
        jso.call("initialize",new Object());
    }






    public static void main(String[] args){
        launch(args);
        gpu.releaseContext();
    }
}
