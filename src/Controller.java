import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
    private Button RunHashButton;

    @FXML
    private ChoiceBox DeviceDropdown;

    @FXML
    private ChoiceBox PlatformDropdown;

    @FXML
    private TextArea TextLog;

    @FXML
    private TextArea HashResultField;

    @FXML
    private TextField String1Field;

    @FXML
    private TextField String2Field;

    @FXML
    private TextField HashesPerformed;

    @FXML
    private TextField CollisionsFound;

    @FXML
    private TextField CollisionPct;

    @FXML
    private TableColumn String1Column;

    @FXML
    private TableColumn String2Column;

    @FXML
    private TableColumn HashValueColumn;

    @FXML
    private TableView CollisionTable;

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

    public ChoiceBox getDeviceDropdown(){ return DeviceDropdown; }

    public TableColumn getHashValueColumn() {
        return HashValueColumn;
    }

    public TableColumn getString1Column() {
        return String1Column;
    }

    public TableColumn getString2Column() {
        return String2Column;
    }

    public TableView getCollisionTable() {
        return CollisionTable;
    }

    public TextArea getHashResultField() {
        return HashResultField;
    }

    public TextField getCollisionPct() {
        return CollisionPct;
    }

    public TextField getCollisionsFound() {
        return CollisionsFound;
    }

    public TextField getHashesPerformed() {
        return HashesPerformed;
    }

    public TextField getString1Field() {
        return String1Field;
    }

    public TextField getString2Field() {
        return String2Field;
    }

    public TextArea getTextLog() {
        return TextLog;
    }

    public Button getRunHashButton() {
        return RunHashButton;
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
