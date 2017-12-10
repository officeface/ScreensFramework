package screensframework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class StaffExportController implements Initializable, ControlledScreen {

	ScreensController myController;

	@FXML
	private ComboBox<String> filmDropDown; // Lists of films to export
	private ObservableList<String> filmNames = FXCollections.observableArrayList(); // Container for film titles
	private String selectedFilmForExport; // Holds selected Film for export

	@FXML
	private Button btnExportSelectedFilm; // Export to txt the selected film in the combobox

	@FXML
	private Button btnExportAll; // Export to txt all the available films

	// Toolbar variables
	@FXML
	private Button btnLogout;

	@FXML
	private Button btnHome;

	@FXML
	private Button btnGoToBookingSummary;

	/**
	 * Initialises the controller class.
	 */

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// As page loads film titles are added to the filmdropdown menu 
		List<String[]> filmList = null;
		try {
			filmList = TextFileManager.getFilmTitles(); // Getting film titles from the database
		} catch (IOException e) {

			e.printStackTrace();
		}

		for (int i = 0; i < filmList.size(); i++) {
			filmNames.addAll(filmList.get(i)); // Intermediate 
		}
		filmDropDown.setItems(filmNames); // Setting film titles to the dropdown 
	}

	@Override
	public void setScreenParent(ScreensController screenParent) {
		myController = screenParent;
	}

	/**
	 * Exports film details of a specified film from a json arrays to csv file.
	 * Number of booked and free seats of a specified film are also calculated.
	 * @author frazahmad
	 * @param event
	 * @throws IOException
	 */
	@FXML
	public void getSelectedFilmDetailsToCSV(ActionEvent event) throws IOException {

		// Getting specific film name to export to csv
		this.selectedFilmForExport = filmDropDown.getValue();
		JSONObject objSF = JSONUtils.getJSONObjectFromFile(TextFileManager.database); // New json object is the database for scanning

		// New json object and array to hold title,free and bookseats info
		JSONObject objSF2 = new JSONObject();
		JSONArray seatsArraySF = new JSONArray();
		JSONObject itemSF = new JSONObject();
		// Adding fields to array + temporary array created
		itemSF.put("title", "");
		itemSF.put("FreeSeats", "");
		itemSF.put("BookedSeats", "");
		itemSF.put("date", "");
		itemSF.put("time", "");
		seatsArraySF.put(itemSF); 
		objSF2.put("SelectedFilmInfo", seatsArraySF); // Naming the array and adding to a new json object 

		JSONArray jsonArraySF = objSF.getJSONArray("FilmTimes"); // Getting listings data from main database
		for (int z = 0; z < jsonArraySF.length(); z++) { // To transverse listings data 
			if (jsonArraySF.getJSONObject(z).getString("title").equals(this.selectedFilmForExport)) { // Finding matching title

				// Get selected title from database
				String[] tempTitleArraySF = new String[1];
				tempTitleArraySF[0] = jsonArraySF.getJSONObject(z).getString("title"); 

				// Get date from database
				String[] tempDateArraySF = new String[1];
				tempDateArraySF[0] = jsonArraySF.getJSONObject(z).getString("date");

				// Get time from database
				String[] tempTimeArraySF = new String[1];
				tempTimeArraySF[0] = jsonArraySF.getJSONObject(z).getString("time");

				Integer getSeatInfoSF = z + 1; // Position of seat info
				String[][] seats = TextFileManager.getSeatInformation(getSeatInfoSF.toString()); // getting seats info
				// Initialising book/free seat variable
				Integer bookedSeatsCounterSF = 0; 
				Integer freeSeatsCounterSF = 0;
				// Generate the seats according to the listing information:
				for (int i = 0; i < 6; i++) {
					for (int j = 0; j < 10; j++) {
						// Check if seat is available:
						if (seats[i][j].equals("Free")) {
							freeSeatsCounterSF = freeSeatsCounterSF + 1; // Updates free seats counter 
						} else {
							bookedSeatsCounterSF = bookedSeatsCounterSF + 1; // Updates booked seats counter 
						}
					}
				}
				;
				// Setting the labels to their respective numbers
				String bscSF = bookedSeatsCounterSF.toString();
				String fscSF = freeSeatsCounterSF.toString();

				// Adding temp selected film data into the existing jsonarray 
				JSONObject tempListSF = new JSONObject();
				tempListSF.put("title", tempTitleArraySF[0]);
				tempListSF.put("FreeSeats", fscSF);
				tempListSF.put("BookedSeats", bscSF);
				tempListSF.put("date", tempDateArraySF[0]);
				tempListSF.put("time", tempTimeArraySF[0]);
				seatsArraySF.put(tempListSF);

			}

		}

		String o = (CDL.toString(new JSONArray(objSF2.get("SelectedFilmInfo").toString()))); // Conversion from json array to csv format and then into a new string
		FileUtils.writeStringToFile(new File(this.selectedFilmForExport.replaceAll(" ", "") + "DataExport.text"), o, "UTF-8"); // New csv file created - previous string (which was json) written to new text file
		System.out.println(this.selectedFilmForExport + " information has been exported!");

	}

	
    /**
     * Exports entire database into a csv file. Also the number of booked and free seats
     * for each of the films are also added. 
     * @author frazahmad
     * @param event
     * @throws IOException
     */
	@FXML
	public void getDatabaseToCSV(ActionEvent event) throws IOException {
		
		// Creating new json object from the database
		JSONObject obj = JSONUtils.getJSONObjectFromFile(TextFileManager.database);
		// Creating a new csv file with FilmList from database at the start
		String x = (CDL.toString(new JSONArray(obj.get("FilmList").toString())));
		FileUtils.writeStringToFile(new File("FilmDataExported.text"), x, "UTF-8");
		// Appending FilmTimes to the file
		String y = (CDL.toString(new JSONArray(obj.get("FilmTimes").toString())));
		Files.write(Paths.get("FilmDataExported.text"), y.getBytes(), StandardOpenOption.APPEND);

		// New json array to hold title,free and bookseats info
		JSONObject obj2 = new JSONObject();
		JSONArray seatsArray = new JSONArray();
		JSONObject item = new JSONObject();
		item.put("title", "");
		item.put("FreeSeats", "");
		item.put("BookedSeats", "");
		item.put("date", "");
		item.put("time", "");
		seatsArray.put(item);
		obj2.put("SeatsInfo", seatsArray);

		// For Title values
		JSONArray jsonArray = obj.getJSONArray("FilmTimes");

		for (int z = 0; z < jsonArray.length(); z++) {
			// Get title
			String[] tempTitleArray = new String[1];
			tempTitleArray[0] = jsonArray.getJSONObject(z).getString("title");

			// Get date
			String[] tempDateArray = new String[1];
			tempDateArray[0] = jsonArray.getJSONObject(z).getString("date");

			// Get time
			String[] tempTimeArray = new String[1];
			tempTimeArray[0] = jsonArray.getJSONObject(z).getString("time");

			// Getting seats values for each title
			Integer getSeatInfo = z + 1;
			String[][] seats = TextFileManager.getSeatInformation(getSeatInfo.toString());
			Integer bookedSeatsCounter = 0;
			Integer freeSeatsCounter = 0;
			// Generate the seats according to the listing information:
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 10; j++) {
					// Check if seat is available:
					if (seats[i][j].equals("Free")) {
						freeSeatsCounter = freeSeatsCounter + 1;
					} else {
						bookedSeatsCounter = bookedSeatsCounter + 1;
					}
				}
			}
			;
			// Setting the labels to their respective numbers
			String bsc = bookedSeatsCounter.toString();
			String fsc = freeSeatsCounter.toString();

			JSONObject tempList = new JSONObject();
			tempList.put("title", tempTitleArray[0]);
			tempList.put("FreeSeats", fsc);
			tempList.put("BookedSeats", bsc);
			tempList.put("date", tempDateArray[0]);
			tempList.put("time", tempTimeArray[0]);
			seatsArray.put(tempList);

		} // End of loop

		String z = (CDL.toString(new JSONArray(obj2.get("SeatsInfo").toString())));
		Files.write(Paths.get("FilmDataExported.text"), z.getBytes(), StandardOpenOption.APPEND);

		System.out.println("Database has been exported!");

	}// End of overall method

	// Toolbar methods

	@FXML
	public void goToStaffHome(ActionEvent event) {
		myController.setScreen(ScreensFramework.staffHomeID);
	}

	@FXML
	public void goToLogin(ActionEvent event) {
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
		myController.unloadScreen(ScreensFramework.staffChoiceID);

		myController.loadScreen(ScreensFramework.loginID, ScreensFramework.loginFile);
		myController.setScreen(ScreensFramework.loginID);
	}

	@FXML
	private void goToBookingSummary(ActionEvent event) {
		myController.setScreen(ScreensFramework.bookingSummaryID);
	}

}
