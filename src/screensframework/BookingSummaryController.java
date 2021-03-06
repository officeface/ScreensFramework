package screensframework;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import objects.Listing;

/**
 * FXML Controller class for the Booking Summary page.
 * 
 * This page shows a summary of bookings for a given film listing, which is
 * based on the date, film and time selected. The number of free and booked
 * seats for a given listing is also displayed.
 * 
 * Hovering over booked seats provides individual customer information for that
 * seat.
 * 
 * @author Fraz Ahmad
 *
 */
public class BookingSummaryController implements Initializable, ControlledScreen, StaffToolbar {

	ScreensController myController;

	// 1) Variables to change the summary info
	@FXML
	private ComboBox<String> comboListOfFilms; // Lists of films to find summary booking of

	@FXML
	private ComboBox<String> comboTimeSelector; // Selected time of booking

	@FXML
	private DatePicker datePickerSelector; // Selected date of booking

	@FXML
	private Button btnScreenInfo; // Once form is filled, pressing button will show the summary data

	// To store data from TextFileManager methods
	private ObservableList<String> filmNames = FXCollections.observableArrayList(); // Container for film titles
	private ObservableList<String> timeList = FXCollections.observableArrayList(); // Container for film times
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Formatting dates

	// 2) Displaying seat numbers
	@FXML
	private Label lblBookedSeats; // Label describing the number of booked seats

	@FXML
	private Label lblFreeSeats; // Label describing the number of free seats

	// 3) VARIABLES INVOLVED IN THE GRAPHICAL REPRESENTATION OF THE SCREEN

	// Seating
	String[][] seats; // Initialisation of seats (as a String array)

	@FXML
	private GridPane seatLayout;

	// Toolbar variables
	@FXML
	private Button btnSLogout;

	@FXML
	private Button btnSHome;

	@FXML
	private Button btnGoToExport;

	/**
	 * Initialises the controller class.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setScreenParent(ScreensController screenParent) {
		myController = screenParent;
	}

	// Find current listings based on date -> METHODS @author: Mark
	/**
	 * @return A list of films that are playing on a specified date.
	 * @throws IOException
	 *             if the database file could not be found.
	 */
	public List<String> getFilmList() throws IOException {
		try {
			String date = dateTimeFormatter.format(datePickerSelector.getValue());
			return TextFileManager.filmsFilteredByDate(date); // Returns a list of films based on the date value
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @return A list of times for a given date and film.
	 * @throws IOException
	 *             if the database file could not be found.
	 */
	public List<String> getTimesList() throws IOException {
		try {
			String date = dateTimeFormatter.format(datePickerSelector.getValue());
			String film = comboListOfFilms.getValue();
			return TextFileManager.timesFilteredByDateAndFilm(date, film); // Showings based on date and time selected
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Sets the films list after a date has been selected. Films are those that will
	 * be showing on the specified date.
	 * 
	 * @param event
	 *            date selection from DatePicker box
	 * @throws IOException
	 *             if the database file cannot be found.
	 */
	@FXML
	private void setFilmList(ActionEvent event) throws IOException {
		comboListOfFilms.getItems().clear();
		// Populate film list
		try {
			List<String> filmList = getFilmList();
			for (int i = 0; i < filmList.size(); i++) {
				filmNames.add(filmList.get(i)); // Intermediate
			}
			comboListOfFilms.setItems(filmNames); // Sets the film list comboBox
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the times list.
	 * 
	 * @param event
	 *            time selection from time comboBox
	 * @throws IOException
	 *             if the database file cannot be found.
	 */
	@FXML
	private void setTimesList(ActionEvent event) throws IOException {
		comboTimeSelector.getItems().clear();
		// Populate times list
		try {
			List<String> times = getTimesList();
			for (int i = 0; i < times.size(); i++) {
				timeList.add(times.get(i)); // Intermediate
			}
			comboTimeSelector.setItems(timeList); // Sets the time list comboBox
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Once the user sets the film showing based on data, if the user presses the
	 * screen info button, information will be updated such as number of booked and
	 * free seats as well as a graphical representation of the cinema's screen
	 * seating.
	 * 
	 * @author Fraz Ahmad and Mark Backhouse
	 * @param event
	 *            the user clicked "Get info" button.
	 * @throws IOException
	 *             if the database file could not be found.
	 */
	@FXML
	public void getScreenInfo(ActionEvent event) throws IOException {

		if (datePickerSelector.getValue() != null && !comboListOfFilms.getSelectionModel().isEmpty()
				&& !comboTimeSelector.getSelectionModel().isEmpty()) { // Check that a selection has been made

			String title = this.comboListOfFilms.getValue();
			String date = dateTimeFormatter.format(datePickerSelector.getValue());
			String time = this.comboTimeSelector.getValue();
			String listingID = Listing.findShowingID(title, date, time); // Based on user input the listing ID is found
			String[][] seats = TextFileManager.getSeatInformation(listingID); // String array of seats is found based on
																				// listing

			// Initialisation of the booked and free seats counter
			Integer bookedSeatsCounter = 0;
			Integer freeSeatsCounter = 0;

			// Generate the seats according to the listing information:
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 10; j++) {
					Button btn = new Button(); // New buttons generated
					Integer I = (Integer) i; // Conversion to 'Integer' formats
					Integer J = (Integer) j;
					seatLayout.add(btn, j, i); // Adding buttons to gridlayout
					btn.setPrefSize(58, 28); // Setting preferred size
					btn.setId(getSeatName(I, J)); // Setting btn ID

					// Seats labels
					String seatLabel = getSeatName(I, J);
					btn.setText(seatLabel); // Setting seat name to the button

					// Check if seat is available:
					if (seats[i][j].equals("Free")) {
						// Tooltip:
						btn.setTooltip(new Tooltip("Seat is free"));

						btn.setStyle("-fx-base: lightgreen;"); // Sets button to lightgreen if free
						freeSeatsCounter = freeSeatsCounter + 1; // Updates free seats counter

					} else {
						// Get customer's name for extra information:
						TextFileManager fileManager = new TextFileManager();
						List<String[]> loginDetails = fileManager.getLoginDetails();
						for (int k = 0; k < loginDetails.size(); k++) {
							if (loginDetails.get(k)[0].equals(seats[i][j])) {
								// Tooltip over button for extra information:
								btn.setTooltip(new Tooltip("Customer " + seats[i][j] + "\n" + loginDetails.get(k)[4]
										+ " " + loginDetails.get(k)[5]));
							}
						}

						btn.setStyle("-fx-base: lightpink;"); // Sets button to lightpink if booked
						bookedSeatsCounter = bookedSeatsCounter + 1; // Updates free seats counter

					}

				}

			}

			// Setting the labels to their respective numbers + conversion from integer to
			// string
			lblBookedSeats.setText(bookedSeatsCounter.toString());
			lblFreeSeats.setText(freeSeatsCounter.toString());
		}
	}

	/**
	 * 
	 * @author Mark Backhouse
	 * 
	 * @param row
	 *            Seat's row number
	 * @param col
	 *            Seat's col number
	 * @return The seat's name as a nice string. e.g. if the seat is in row 0, col
	 *         5, the seat name will be "Seat A6" (rows/cols are zero-indexed)
	 */
	public String getSeatName(Integer row, Integer col) {

		char rowAnswer = (char) ('A' + row);

		Integer colShift = col + 1;
		String colAnswer = colShift.toString();

		return rowAnswer + colAnswer;
	}

	// Toolbar methods
	/**
	 * {@inheritDoc}
	 */
	@FXML
	public void goToStaffHome(ActionEvent event) {
		myController.setScreen(ScreensFramework.staffHomeID);
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
	public void goToStaffExport(ActionEvent event) {
		myController.setScreen(ScreensFramework.staffExportID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void goToAddFilmPage(ActionEvent event) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void goToAddListings(ActionEvent event) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void goToBookingSummary(ActionEvent event) {
	}

}
