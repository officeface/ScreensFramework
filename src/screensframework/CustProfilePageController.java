package screensframework;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller class for the Customer's profile page. Allows the Customer to see
 * their username, firstname and lastname, as well as updating these details.
 * 
 * @author Mark Backhouse
 *
 */
public class CustProfilePageController implements Initializable, ControlledScreen, CustToolbar {

	@FXML
	private Button btnLogout, btnHome, btnMyProfile, btnMyBookings, btnUpdate;

	@FXML
	public TextField txtEmail, txtFirstName, txtLastName;

	@FXML
	private Label lblUpdateStatus, lblFetchDetails;

	ScreensController myController;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.txtEmail.setText(LoginController.USER.getEmail());
		this.txtFirstName.setText(LoginController.USER.getFirstName());
		this.txtLastName.setText(LoginController.USER.getLastName());
	}

	/**
	 * Sets the screen parent.
	 */
	@Override
	public void setScreenParent(ScreensController screenParent) {
		myController = screenParent;
	}

	/**
	 * Refreshes the textfields with the User's information.
	 * 
	 * @param event
	 *            the "Refresh" button is selected.
	 */
	@FXML
	private void getDetails(ActionEvent event) {
		try {
			this.txtEmail.setText(LoginController.USER.getEmail());
			this.txtFirstName.setText(LoginController.USER.getFirstName());
			this.txtLastName.setText(LoginController.USER.getLastName());
		} catch (Exception e) {
			ScreensFramework.LOGGER.warning(e.getMessage());
		}
	}

	/**
	 * Tells the TextFileManager to update the database with the new Customer
	 * information.
	 * 
	 * @param event
	 *            the "Update" button is clicked.
	 * @throws JSONException
	 *             if the JSON object cannot be found.
	 * @throws IOException
	 *             if the database file cannot be found.
	 */
	@FXML
	private void update(ActionEvent event) throws JSONException, IOException {
		if (!this.txtEmail.getText().isEmpty() && !this.txtFirstName.getText().isEmpty()
				&& !this.txtLastName.getText().isEmpty()) {
			LoginController.USER.setEmail(this.txtEmail.getText());
			LoginController.USER.setFirstName(this.txtFirstName.getText());
			LoginController.USER.setLastName(this.txtLastName.getText());

			TextFileManager.updateUserDetails(LoginController.USER); // Update information in database

			this.lblUpdateStatus.setText("Updated details!");
		} else {
			this.lblUpdateStatus.setText("Field missing! Try again.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@FXML
	public void goToCustHome(ActionEvent event) {
		myController.setScreen(ScreensFramework.custHomeID);
	}

	/**
	 * {@inheritDoc}
	 */
	@FXML
	public void goToCustProfilePage(ActionEvent event) {
		myController.setScreen(ScreensFramework.custProfilePageID);
	}

	/**
	 * {@inheritDoc}
	 */
	@FXML
	public void goToCustBookingHistoryPage(ActionEvent event) {
		myController.setScreen(ScreensFramework.custBookingHistoryPageID);

	}

	/**
	 * {@inheritDoc}
	 */
	@FXML
	public void goToLogin(ActionEvent event) {
		// Unload the User:
		LoginController.USER.clearDetails();
		ScreensFramework.LOGGER.info("User logged out.");
		// Unload screens:
		myController.unloadScreen(ScreensFramework.loginID);
		myController.unloadScreen(ScreensFramework.registrationID);
		myController.unloadScreen(ScreensFramework.staffHomeID);
		myController.unloadScreen(ScreensFramework.custHomeID);
		myController.unloadScreen(ScreensFramework.custProfilePageID);
		myController.unloadScreen(ScreensFramework.custBookFilmPageID);
		myController.unloadScreen(ScreensFramework.custConfirmPageID);

		myController.unloadScreen(ScreensFramework.staffExportID);
		myController.unloadScreen(ScreensFramework.bookingSummaryID);
		myController.unloadScreen(ScreensFramework.addFilmPageID);
		myController.unloadScreen(ScreensFramework.addFilmListingsID);

		myController.loadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
		myController.setScreen(ScreensFramework.loginID);
	}

}
