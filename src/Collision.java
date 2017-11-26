import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Collision {
    public StringProperty value_one;
    public StringProperty value_two;
    public StringProperty hash_value;

    Collision(String v1, String v2, String hv){
        value_one = new SimpleStringProperty();
        value_two = new SimpleStringProperty();
        hash_value = new SimpleStringProperty();

        value_one.setValue(v1);
        value_two.setValue(v2);
        hash_value.setValue(hv.toUpperCase());
    }

    public StringProperty value_oneProperty(){
        return value_one;
    }

    public StringProperty value_twoProperty(){
        return value_two;
    }

    public StringProperty hash_valueProperty(){
        return hash_value;
    }
}
