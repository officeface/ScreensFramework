package screensframework;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 */
public class StaffChoicePageController implements Initializable, ControlledScreen {

    ScreensController myController;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }

    @FXML
    private void goToCustHome(ActionEvent event){
       myController.setScreen(ScreensFramework.custHomeID);
    }
    
    @FXML
    private void goToStaffHome(ActionEvent event){
       myController.setScreen(ScreensFramework.staffHomeID);
    }
}