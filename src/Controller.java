import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    @FXML
    private WebView D3View;


    private WebEngine engine;

    public void setEngineURL(URL url){
        //engine.load("myhtml.html");
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
