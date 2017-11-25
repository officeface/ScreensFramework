package screensframework;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CustConfirmPageController implements Initializable, ControlledScreen {

    ScreensController myController; 
    

    //Labels to change
    
    @FXML
    private Label lblTitle;
    
    @FXML
    private Label lblDate;
    
    @FXML
    private Label lblTime;
    
    @FXML
    private Label lblNoOfSeats;
    
    @FXML
    private Label lblOverallPrice;
    
    //Buttons
    
    @FXML
    private Button btnBack;
    
    @FXML
    private Button btnBook;
    
    
    
    
    
    
    
    
    
    
    /**
	 * Initialises the controller class.
	 */
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		//Labels have been updated in the confirm page from the booking page
		this.lblTitle.setText(CustHomeController.BOOKING.getMovie().getTitle());
		this.lblDate.setText(CustHomeController.BOOKING.getMovie().getDate());
		this.lblTime.setText(CustHomeController.BOOKING.getMovie().getTime());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void setScreenParent(ScreensController screenParent) {
		myController = screenParent;
	}
	
	@FXML
	public void goToCustBookFilmPage(ActionEvent event) {
		myController.setScreen(ScreensFramework.custBookFilmPageID);
	}
	
}