import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
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

    @FXML
    private ChoiceBox PlatformDropdown;

    @FXML
    private TextArea TextLog;


    private WebEngine engine;

    public void setEngineURL(URL url){
        //engine.load("myhtml.html");
    }

    public Button getStartButton(){
        return StartButton;
    }

    public ChoiceBox getPlatformDropdown() {
        return PlatformDropdown;
    }

    public ChoiceBox getDeviceDropdown(){
        return DeviceDropdown;
    }

    public TextArea getTextLog() {
        return TextLog;
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
