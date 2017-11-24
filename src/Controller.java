import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    @FXML
    private WebView D3View;

    @FXML
    private WebView D3ViewLeft;

    @FXML
    private WebView D3ViewRight;

    @FXML
    private WebView D3ViewBottom;

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
    private WebEngine engineLeft;
    private WebEngine engineRight;
    private WebEngine engineBottom;

    public void setEngineURL(URL url){
        //engine.load("viz_page.html");
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
        engineLeft = D3ViewLeft.getEngine();
        engineRight = D3ViewRight.getEngine();
        engineBottom = D3ViewBottom.getEngine();
        URL url;
        try {
            url = getClass().getResource("viz_page.html");
            engine.load(url.toString());
            url = getClass().getResource("infohtmlleft.html");
            engineLeft.load(url.toString());
            url = getClass().getResource("infohtmlright.html");
            engineRight.load(url.toString());
            url = getClass().getResource("infohtmlbottom.html");
            engineBottom.load(url.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
