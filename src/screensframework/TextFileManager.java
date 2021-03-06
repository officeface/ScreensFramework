package screensframework;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import objects.Booking;
import objects.Listing;
import objects.Movie;
import objects.User;

/**
 * This class manages the various text files of the application by reading their
 * contents and storing them inside ArrayLists for usage from other classes.
 * 
 * @author mark
 *
 */
public class TextFileManager {

	private List<String[]> loginDetails;
	private List<String[]> filmList;
	private List<String[]> filmTimes;

	/**
	 * The database file, stored in JSON format.
	 */
	public static File database = new File(System.getProperty("user.home") + "/assets/", "database.json");

	/**
	 * Initialises the Login Details, Film List and Film Times arrays.
	 * 
	 * @throws IOException
	 *             if the database file canot be found.
	 */
	public TextFileManager() throws IOException {
		this.loginDetails = loginDetailsToArrayList();
		this.filmList = filmListToArrayList();
		this.filmTimes = filmTimesToArrayList();
	}

	/**
	 * Gets an input stream from a specified filepath.
	 * 
	 * @param path
	 *            address of the file to be read
	 * @return Input from a file as an Input Stream
	 */
	public static InputStream inputStreamFromFile(String path) {

		try {
			InputStream inputStream = TextFileManager.class.getResourceAsStream(path);
			return inputStream;
		} catch (Exception e) {
			ScreensFramework.LOGGER.warning(e.getMessage());

		}
		return null;
	}

	/**
	 * Gets the seating information for a specified listing.
	 * 
	 * @param id
	 *            Listing ID for a particular listing
	 * @return A 2-dimensional array of information about the availability of seats.
	 *         If the seat is free, its value will be "Free". If the seat is booked,
	 *         its value will be the User ID of the user who has booked it.
	 * @throws JSONException
	 *             the JSON object cannot be found.
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static String[][] getSeatInformation(String id) throws JSONException, IOException {

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("FilmTimes");
		String title;

		// CHANGE SIZE OF ARRAY AT LATER DATE IF NECESSARY!!!
		String[][] seatInformation = new String[6][10];

		for (int i = 0; i < jsonArray.length(); i++) {
			if (jsonArray.getJSONObject(i).getString("showingID").equals(id)) {
				JSONObject seats = jsonArray.getJSONObject(i).getJSONObject("seats");

				title = jsonArray.getJSONObject(i).getString("title");

				for (int j = 0; j < seatInformation.length; j++) {
					for (int k = 0; k < seatInformation[0].length; k++) {
						Integer J = (Integer) j;
						Integer K = (Integer) k;
						seatInformation[j][k] = seats.getString(J.toString() + K.toString());

					}
				}

				ScreensFramework.LOGGER.info("Loaded booking data for " + title);
				// System.out.println("Loaded information for " + title);
				break;
			}
		}

		return seatInformation;
	}

	/**
	 * Rewrites the database with new information for a user whose details will be
	 * updated
	 * 
	 * @param user
	 *            Customer whose details are to be updated
	 * @throws JSONException
	 *             the JSON object cannot be found.
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static void updateUserDetails(User user) throws JSONException, IOException {
		String userID = user.getUserID();
		String newEmail = user.getEmail();
		String newFirstName = user.getFirstName();
		String newLastName = user.getLastName();

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("LoginDetails");

		for (int i = 0; i < jsonArray.length(); i++) {
			if (jsonArray.getJSONObject(i).getString("userID").equals(userID)) {
				jsonArray.getJSONObject(i).put("email", newEmail);
				jsonArray.getJSONObject(i).put("firstname", newFirstName);
				jsonArray.getJSONObject(i).put("surname", newLastName);

			}
		}

		// Write JSON Object to file:
		try (FileWriter file = new FileWriter(database)) {
			file.write(obj.toString());
			ScreensFramework.LOGGER.info("Successfully updated database.json with new information.");
			// System.out.println("Successfully updated JSON Object in File...");
		}

	}

	/**
	 * Adds a new Customer to the database.
	 * 
	 * @param user
	 *            the Customer to be added.
	 * @throws JSONException
	 *             if the JSON object cannot be found.
	 * @throws IOException
	 *             if the database file cannot be found.
	 */
	public static void registerNewUser(User user) throws JSONException, IOException {
		String userID = user.getUserID();
		String email = user.getEmail();
		String password = user.getPassword();
		String firstName = user.getFirstName();
		String lastName = user.getLastName();

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("LoginDetails");

		JSONObject userObject = new JSONObject();
		JSONObject bookings = new JSONObject();
		userObject.put("userID", userID);
		userObject.put("email", email);
		userObject.put("password", password);
		userObject.put("firstname", firstName);
		userObject.put("surname", lastName);
		userObject.put("type", "C");
		userObject.put("bookings", bookings);

		jsonArray.put(userObject); // Add new user to array

		// Write JSON Object to file:
		try (FileWriter file = new FileWriter(database)) {
			file.write(obj.toString());
			ScreensFramework.LOGGER.info("Successfully updated database.json with new information.");
			// System.out.println("Successfully updated JSON Object in File...");
		}
	}

	/**
	 * Updates an existing listing with new seat information (normally after a
	 * customer has made a new booking and seating changes must reflect this)
	 * 
	 * @param listing
	 *            the Listing to be updated
	 * @throws IOException
	 *             the database file cannot be found.
	 * @throws JSONException
	 *             the JSON object cannot be found.
	 */
	public static void updateListing(Listing listing) throws JSONException, IOException {
		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("FilmTimes");
		String[][] seats = listing.getSeats();

		JSONObject seatList = new JSONObject();

		// Fill out new seat object
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 10; j++) {
				seatList.put(((Integer) i).toString() + ((Integer) j).toString(), seats[i][j]);
			}
		}

		// Find Showing, then add in seat object
		for (int k = 0; k < jsonArray.length(); k++) {
			if (jsonArray.getJSONObject(k).getString("showingID").equals(listing.getShowingID())) {
				jsonArray.getJSONObject(k).put("seats", seatList);
			}
		}

		// Write JSON Object to file:
		try (FileWriter file = new FileWriter(database)) {
			file.write(obj.toString());
			// System.out.println("Successfully updated JSON Object in File...");
			ScreensFramework.LOGGER.info("Successfully updated database.json with new information.");

		}

	}

	/**
	 * Updates a User's booking history with a new booking.
	 * 
	 * @param booking
	 *            The Booking to be added
	 * @throws IOException
	 *             the database file cannot be found.
	 * @throws JSONException
	 *             the JSON object cannot be found.
	 */
	public static void updateBookingHistory(Booking booking) throws JSONException, IOException {
		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("LoginDetails");
		JSONArray seatInfo;
		try {
			seatInfo = new JSONArray();

			for (int i = 0; i < jsonArray.length(); i++) {
				if (jsonArray.getJSONObject(i).getString("userID").equals(booking.getCustomer().getUserID())) {
					seatInfo = jsonArray.getJSONObject(i).getJSONObject("bookings")
							.getJSONArray(booking.getMovie().getShowingID());
				}
			}

		} catch (Exception e) {
			ScreensFramework.LOGGER.warning(e.getMessage());
			seatInfo = new JSONArray();
		}
		for (int i = 0; i < booking.getSeats().size(); i++) {
			seatInfo.put(booking.getSeats().get(i));
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			if (jsonArray.getJSONObject(i).getString("userID").equals(booking.getCustomer().getUserID())) {
				jsonArray.getJSONObject(i).getJSONObject("bookings").put(booking.getMovie().getShowingID(), seatInfo);

			}
		}
		// Write JSON Object to file:
		try (FileWriter file = new FileWriter(database)) {
			file.write(obj.toString());
			// System.out.println("Successfully updated JSON Object in File...");
			ScreensFramework.LOGGER.info("Successfully updated database.json with new information.");

		}

	}

	/**
	 * Removes a specified booking from the User's history
	 * 
	 * @param user
	 *            The User whose booking should be removed
	 * @param listingID
	 *            The ID for the listing to be removes
	 * @throws IOException
	 *             the database file cannot be found.
	 * @throws JSONException
	 *             the JSON object cannot be found.
	 */
	public static void removeBooking(User user, String listingID) throws JSONException, IOException {
		// Steps: remove listing from user's part of database
		// Find steps inside listing part of database and reset them to "Free"

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray loginArray = obj.getJSONArray("LoginDetails");
		JSONArray listingsArray = obj.getJSONArray("FilmTimes");
		JSONObject listingToBeChanged = null;
		JSONObject listingSeats = null; // Initialise
		int indexOfListing = -1;

		// Set listing seats:
		for (int i = 0; i < listingsArray.length(); i++) {
			if (listingsArray.getJSONObject(i).getString("showingID").equals(listingID)) {
				listingToBeChanged = listingsArray.getJSONObject(i);
				listingSeats = listingToBeChanged.getJSONObject("seats");
				indexOfListing = i;
			}
		}

		// Find user's seats for the booking to be deleted:
		for (int i = 0; i < loginArray.length(); i++) {

			JSONObject JSONUser = loginArray.getJSONObject(i);
			if (JSONUser.getString("userID").equals(user.getUserID())) { // Find the user in the database

				// Convert the User's seats for this listing into array format:
				JSONArray seats = JSONUser.getJSONObject("bookings").getJSONArray(listingID); // The seats in question

				// Modify listing's seats by setting each of this user's seats to "Free":
				for (int j = 0; j < seats.length(); j++) {
					String seatToChange = seatToCoordinate(seats.getString(j).toString());
					listingSeats.put(seatToChange, "Free");

				}

				// Put this seat information back into the listings array:
				listingToBeChanged.put("seats", listingSeats);
				listingsArray.remove(indexOfListing);
				listingsArray.put(listingToBeChanged);

				// Delete user's seats for this listing:
				JSONUser.getJSONObject("bookings").remove(listingID);

			}
		}

		// Write JSON Object to file:
		try (FileWriter file = new FileWriter(database)) {
			file.write(obj.toString());
			ScreensFramework.LOGGER.info("Successfully updated database.json with new information.");
			// System.out.println("Successfully updated JSON Object in File...");
		}

	}

	/**
	 * Converts a 'nice' String representation of a seat to numbered coordinates for
	 * ease-of-use in the database.
	 * 
	 * @param seat
	 *            A String in the form "Seat A7"
	 * @return The coordinates of a booking of the form "Seat A7". In this case the
	 *         returned value would be "06", since Seat A7 is in row 0 and column 6.
	 */
	public static String seatToCoordinate(String seat) {
		char row;
		String col;
		Integer colAsNumber;

		String letterNumberCombo = seat.split(" ")[1];
		row = (char) (letterNumberCombo.charAt(0) - 17);

		colAsNumber = Integer.parseInt(letterNumberCombo.substring(1, letterNumberCombo.length())) - 1;
		col = colAsNumber.toString();

		return ((Character) (row)).toString() + col;
	}

	/**
	 * 
	 * Adds a new film as a JSONObject to the database file.
	 * 
	 * @param newMovie
	 *            the new film to be added.
	 * @throws IOException
	 *             the database file cannot be found.
	 * @throws JSONException
	 *             the JSON Object cannot be found.
	 */
	public static void addFilmDetails(Movie newMovie) throws JSONException, IOException {

		String title = newMovie.getTitle();
		String image = newMovie.getImage();
		String description = newMovie.getDescription();
		Boolean filmAlreadyExists = false;

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);

		JSONArray jsonArray = obj.getJSONArray("FilmList");

		// Check if the movie already exists in the database, in which case we will
		// merely update it:
		for (int i = 0; i < jsonArray.length(); i++) {

			if (jsonArray.getJSONObject(i).getString("title").equals(title)) {
				filmAlreadyExists = true;

				jsonArray.getJSONObject(i).put("title", title);
				jsonArray.getJSONObject(i).put("image", image);
				jsonArray.getJSONObject(i).put("description", description);

			}
		}

		if (!filmAlreadyExists) {
			// APPENDS TO THE END OF THE FILELIST
			JSONObject templist = new JSONObject();
			templist.put("title", title);
			templist.put("image", image);
			templist.put("description", description);
			jsonArray.put(templist);
		}

		// Write JSON Object to file:
		try (FileWriter file = new FileWriter(database)) {
			file.write(obj.toString());
			ScreensFramework.LOGGER.info("Successfully updated database.json with new information.");
			// System.out.println("Successfully updated JSON Object in File...");
		}

	}

	/**
	 * 
	 * Adds a new film listing as a JSONObject to the database. The seats are stored
	 * as 60 "Free" key:value pairs.
	 * 
	 * @param newListing
	 *            the new listing to be added.
	 * @throws IOException
	 *             the database file cannot be found.
	 * @throws JSONException
	 *             the JSON Object cannot be found.
	 */
	public static void addFilmListings(Listing newListing) throws JSONException, IOException {

		String showingID = newListing.getShowingID();
		String title = newListing.getTitle();
		String date = newListing.getDate();
		String time = newListing.getTime();

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database); // testing
																	// in
																	// database
																	// 2
		JSONArray jsonArray = obj.getJSONArray("FilmTimes");

		JSONObject seatList = new JSONObject();
		for (int i = 0; i < 60; i++) {
			Integer I = (Integer) i;
			if (i < 10) {
				seatList.put("0" + I.toString(), "Free");
			} else {
				seatList.put(I.toString(), "Free");
			}
		}

		// APPENDS TO THE END OF THE FILETIMES! - HOWEVER DB GOES CRAZY
		JSONObject templist = new JSONObject();
		templist.put("date", date);
		templist.put("showingID", showingID);
		templist.put("time", time);
		templist.put("title", title);
		templist.put("seats", seatList);

		jsonArray.put(templist);

		// Write JSON Object to file:
		try (FileWriter file = new FileWriter(database)) {
			file.write(obj.toString());
			ScreensFramework.LOGGER.info("Successfully updated database.json with new information.");
			// System.out.println("Successfully updated JSON Object in File...");
		}

	}

	/**
	 * Takes the Login Details from the database and inserts them into an Array List.
	 * @return An array of login details for users of the cinema booking system. The
	 *         array columns signify email-address/username, password and a
	 *         staff/customer choice that tells the system to point the user in the
	 *         correct direction.
	 * @throws IOException
	 *             the database file cannot be found.
	 * 
	 */
	public static List<String[]> loginDetailsToArrayList() throws IOException {

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("LoginDetails");
		List<String[]> loginDetails = new ArrayList<String[]>();

		for (int i = 0; i < jsonArray.length(); i++) {
			String[] tempArray = new String[6];

			tempArray[0] = jsonArray.getJSONObject(i).getString("userID");
			tempArray[1] = jsonArray.getJSONObject(i).getString("email");
			tempArray[2] = jsonArray.getJSONObject(i).getString("password");
			tempArray[3] = jsonArray.getJSONObject(i).getString("type");
			tempArray[4] = jsonArray.getJSONObject(i).getString("firstname");
			tempArray[5] = jsonArray.getJSONObject(i).getString("surname");

			loginDetails.add(tempArray);

		}

		return loginDetails;
	}

	/**
	 * Takes the Film List from the database and returns it in Array List format.
	 * @return The list of films, their img URLs and descriptions in an array list.
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static List<String[]> filmListToArrayList() throws IOException {
		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("FilmList");
		List<String[]> filmList = new ArrayList<String[]>();

		for (int i = 0; i < jsonArray.length(); i++) {
			String[] tempArray = new String[3];

			tempArray[0] = jsonArray.getJSONObject(i).getString("title");
			tempArray[1] = jsonArray.getJSONObject(i).getString("image");
			tempArray[2] = jsonArray.getJSONObject(i).getString("description");

			filmList.add(tempArray);

		}

		return filmList;
	}

	/**
	 * Takes the Film Times from the database and returns them in an Array List format.
	 * @return The list of film showings in an array list. Each film has a
	 *         collection of dates and times attributed to it.
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static List<String[]> filmTimesToArrayList() throws IOException {
		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("FilmTimes");
		List<String[]> filmTimes = new ArrayList<String[]>();

		for (int i = 0; i < jsonArray.length(); i++) {
			String[] tempArray = new String[4];

			tempArray[0] = jsonArray.getJSONObject(i).getString("showingID");
			tempArray[1] = jsonArray.getJSONObject(i).getString("title");
			tempArray[2] = jsonArray.getJSONObject(i).getString("date");
			tempArray[3] = jsonArray.getJSONObject(i).getString("time");

			filmTimes.add(tempArray);

		}

		return filmTimes;
	}

	/**
	 * Filters the list of films by a selected date.
	 * @param date
	 *            in the format dd/MM/yyyy
	 * @return A list of film titles that are playing on a specified date
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static List<String> filmsFilteredByDate(String date) throws IOException {

		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray filmTimesArray = obj.getJSONArray("FilmTimes");
		List<String> films = new ArrayList<String>();

		for (int j = 0; j < filmTimesArray.length(); j++) {
			if (obj.getJSONArray("FilmTimes").getJSONObject(j).getString("date").equals(date)) {
				if (!films.contains(obj.getJSONArray("FilmTimes").getJSONObject(j).getString("title"))) {
					films.add(obj.getJSONArray("FilmTimes").getJSONObject(j).getString("title"));
				}
			}
		}

		return films;
	}

	/**
	 * Filters the available times for a given film and date.
	 * @param date
	 *            Date of the film listing
	 * @param film
	 *            Title of the film listing
	 * @return All times that this film is showing on this date
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static List<String> timesFilteredByDateAndFilm(String date, String film) throws IOException {
		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray filmTimesArray = obj.getJSONArray("FilmTimes");
		List<String> times = new ArrayList<String>();

		for (int i = 0; i < filmTimesArray.length(); i++) {
			if (obj.getJSONArray("FilmTimes").getJSONObject(i).getString("date").equals(date)
					&& obj.getJSONArray("FilmTimes").getJSONObject(i).getString("title").equals(film)) {

				times.add(obj.getJSONArray("FilmTimes").getJSONObject(i).getString("time"));

			}
		}

		return times;
	}

	/**
	 * Returns the Login Details
	 * @return the Login Details
	 */
	public List<String[]> getLoginDetails() {
		return loginDetails;
	}

	/**
	 * Sets the Login Details
	 * @param loginDetails the Login Details to be set.
	 */
	public void setLoginDetails(List<String[]> loginDetails) {
		this.loginDetails = loginDetails;
	}

	/**
	 * Returns the Film List
	 * @return the Film List
	 */
	public List<String[]> getFilmList() {
		return filmList;
	}

	/**
	 * Sets the Film List
	 * @param filmList the Film List to be set.
	 */
	public void setFilmList(List<String[]> filmList) {
		this.filmList = filmList;
	}

	/**
	 * Returns the Film Times
	 * @return the Film Times
	 */
	public List<String[]> getFilmTimes() {
		return filmTimes;
	}

	/** 
	 * Sets the Film Times
	 * @param filmTimes the Film Time to be set
	 */
	public void setFilmTimes(List<String[]> filmTimes) {
		this.filmTimes = filmTimes;
	}

	/**
	 * Returns a List of all film titles in the database.
	 * @return Lists of all film titles in the database
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static List<String[]> getFilmTitles() throws IOException {
		JSONObject obj = JSONUtils.getJSONObjectFromFile(database);
		JSONArray jsonArray = obj.getJSONArray("FilmList");
		List<String[]> filmListofTitles = new ArrayList<String[]>();
		for (int i = 0; i < jsonArray.length(); i++) {
			String[] tempArray = new String[1];
			tempArray[0] = jsonArray.getJSONObject(i).getString("title");
			filmListofTitles.add(tempArray);
		}
		return filmListofTitles;
	}

	/**
	 * Returns a list of all Film Times in the database.
	 * @return Lists of all timings available by the cinema
	 * @throws IOException
	 *             the database file cannot be found.
	 */
	public static String[] getFilmTimings() throws IOException {
		//
		String[] filmListofTimings = new String[24];

		for (int i = 0; i < 24; i++) {
			if (i < 10) {
				filmListofTimings[i] = "0" + i + ":00";
			} else {
				filmListofTimings[i] = i + ":00";
			}
		}
		return filmListofTimings;
	}

}
