import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    @FXML
    private WebView D3View;

    @FXML
    private Button StartButton;

    @FXML
    private Button StopButton;

    @FXML
    private ChoiceBox DeviceDropdown;

    private WebEngine engine;

    public void setEngineURL(URL url){
        //engine.load("myhtml.html");
    }

    public Button getStartButton(){
        return StartButton;
    }

    public Button getStopButton(){
        return StopButton;
    }

    public ChoiceBox getDeviceDropdown(){
        return DeviceDropdown;
    }

    public WebEngine getEngine(){
        return engine;
    }
    public void initialize(URL location, ResourceBundle resources) {
        engine = D3View.getEngine();
        URL url;
        try {
            url = getClass().getResource("myhtml.html");
            engine.load(url.toString());
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
