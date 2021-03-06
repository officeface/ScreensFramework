package screensframework;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.json.JSONException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import objects.Customer;
import objects.Listing;

/**
 * Controller class for the Booking History page. Allows customers to see their
 * past bookings and delete any bookings that haven't taken place yet.
 * 
 * @author Mark Backhouse
 *
 */
public class CustBookingHistoryPageController implements Initializable, ControlledScreen, CustToolbar {

	@FXML
	private Button btnLogout, btnHome, btnMyProfile, btnMyBookings, btnUpdate;

	@FXML
	public TextField txtEmail, txtFirstName, txtLastName;

	@FXML
	private Label lblUpdateStatus, lblFetchDetails;

	@FXML
	private Accordion bookingAccordion = new Accordion();

	ScreensController myController;

	/**
	 * A Map of key:value pairs that will contain the Customer's Booking History.
	 * The keys are the Listing IDs and the values are Lists of Strings of the names
	 * of the seats the Customer has booked.
	 */
	Map<String, List<String>> bookingHistory;

	/**
	 * Initializes the controller class. Populates the Customer's booking history
	 * accordion with a list of their past bookings. These bookings can be deleted
	 * if they have not yet taken place.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		try {
			bookingHistory = Customer.importBookingHistory(LoginController.USER);
			// Iterate through HashMap:
			Set<String> keys = bookingHistory.keySet();
			int i = 0;
			TitledPane[] bookings = new TitledPane[bookingHistory.size()];

			for (String key : keys) {
				TitledPane pane = new TitledPane(); // Set title of pane
				pane.setText(Listing.findMovieTitle(key) + "\n" + Listing.findMovieDateAndTime(key)); // Set date/time
																										// information

				// Add delete button if the listing has not yet completed:
				String timeNowString = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
				String timeListingString = Listing.findMovieDateAndTime(key);
				Date timeNow = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(timeNowString);
				Date timeListing = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(timeListingString);

				if (timeNow.before(timeListing)) { // Checks if the listing is able to be deleted:

					Button btnDeleteListing = new Button("Delete");
					btnDeleteListing.setStyle("-fx-background-color: rgb(85,209,255); -fx-text-fill: white; "); // Sets
																												// the
																												// style
																												// of
																												// the
																												// buttons

					// Two events handlers if the user hovers over the buttons
					btnDeleteListing.setOnMouseExited(new EventHandler<MouseEvent>() {

						@Override
						public void handle(MouseEvent t) {
							btnDeleteListing.setStyle("-fx-background-color: rgb(85,209,255); -fx-text-fill: white;");
						}
					});

					btnDeleteListing.setOnMouseEntered(new EventHandler<MouseEvent>() {

						@Override
						public void handle(MouseEvent t) {
							btnDeleteListing.setStyle("-fx-background-color: rgb(0,144,254); -fx-text-fill: white;");
						}
					});

					// Add ActionEvent for delete button:
					btnDeleteListing.setOnAction((ActionEvent e) -> {
						try {
							TextFileManager.removeBooking(LoginController.USER, key);
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						this.bookingAccordion.getPanes().remove(pane);
					});

					// Alignment of button:
					btnDeleteListing.setPrefWidth(120);
					pane.setGraphic(btnDeleteListing);
					pane.setGraphicTextGap(10);

				} else {

					Button btnDeleteListing = new Button("Expired");
					btnDeleteListing.setDisable(true);
					btnDeleteListing.setStyle("-fx-background-color: rgb(85,209,255); -fx-text-fill: white; "); // Sets
																												// the
																												// style
																												// of
																												// the
																												// buttons

					// Alignment of button:
					btnDeleteListing.setPrefWidth(120);
					// Tooltip:
					btnDeleteListing.setTooltip(
							new Tooltip("Booking expired!\nYou can only delete bookings that have not yet occurred."));
					pane.setGraphic(btnDeleteListing);
					pane.setGraphicTextGap(10);

				}

				// Set pane contents:
				List<String> seatsArrayList = bookingHistory.get(key);
				String totalCost = "Total cost: £" + 5 * seatsArrayList.size() + ".00"; // Set booking total cost
				seatsArrayList.add(totalCost);
				ObservableList<String> seatsObservableList = FXCollections.observableArrayList(seatsArrayList);
				ListView<String> seatsListView = new ListView<String>(seatsObservableList);
				pane.setContent(seatsListView);

				bookings[i] = pane; // Add pane to list of bookings
				i++; // Go to next index of bookings array
			}

			this.bookingAccordion.getPanes().addAll(bookings);

		} catch (ParseException e) {
			ScreensFramework.LOGGER.warning(e.getMessage());
		} catch (JSONException e) {
			ScreensFramework.LOGGER.warning(e.getMessage());
		} catch (IOException e) {
			ScreensFramework.LOGGER.warning(e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setScreenParent(ScreensController screenParent) {
		myController = screenParent;
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

	/**
	 * {@inheritDoc}
	 */
	@FXML
	public void goToCustBookingHistoryPage(ActionEvent event) {

	}

}
