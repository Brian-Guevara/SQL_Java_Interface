/**
 *
 * Brian Guevara
 * WGU ID: 001003681
 */
package bguev.view;

import bguev.classes.Customer;
import bguev.classes.User;
import bguev.util.DBConnect;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class CustomerEditController implements Initializable {

    /**
     * Initializes the controller class.
     */
    Connection conn;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Integer> idColumn;

    @FXML
    private TableColumn<Customer, String> nameColumn;

    @FXML
    private TableColumn<Customer, String> addressColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;

    private User user;

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField address2Field;

    @FXML
    private TextField cityField;

    @FXML
    private TextField postalField;

    @FXML
    private ComboBox countryBox;

    @FXML
    private Label msg;

    @FXML
    private TextField phoneField;

    @Override

    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnect.getConn();

        // Populate the Country combo box
        try {
            populateComboBox();
        } catch (SQLException ex) {
            Logger.getLogger(CustomerEditController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Setup our columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Make the msg text blank and add our customers to the table
        msg.setText("");
        customerTable.getItems().setAll(populateCustomerList());

    }

    // Method searches for the countries in the database and adds them to the list for selection
    private void populateComboBox() throws SQLException {
        PreparedStatement select = conn.prepareStatement("SELECT country FROM country");
        ResultSet rs = select.executeQuery();
        while (rs.next()) {
            countryBox.getItems().add(rs.getString("country"));

        }
    }

    // Method sets the user who is on the page making changes.
    public void setUser(User x) {
        user = x;
    }

    // Creates our customerList
    public List<Customer> populateCustomerList() {
        int tCustomerId;
        String tCustomerName;
        String tAddress;
        String tPhone;

        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try (
                PreparedStatement statement = conn.prepareStatement(
                        "SELECT c.customerId, c.customerName, a.address, a.address2, a.postalCode, cit.cityId, cit.city, con.country, a.phone "
                        + "FROM customer c, address a, city cit, country con "
                        + "WHERE c.addressId = a.addressId AND a.cityId = cit.cityId AND cit.countryId = con.countryId "
                        + "ORDER BY c.customerId");
                ResultSet rs = statement.executeQuery();) {

            while (rs.next()) {
                tCustomerId = rs.getInt("c.customerId");
                tCustomerName = rs.getString("c.customerName");
                tAddress = rs.getString("a.address");

                tPhone = rs.getString("a.phone");
                Customer newCustomer = new Customer(tCustomerId, tCustomerName, tAddress, tPhone);
                customerList.add(newCustomer);

            }

        } catch (SQLException sqe) {
        } catch (Exception e) {
        }
        // Return our observable list of customers
        return customerList;

    }

    public void addButton(ActionEvent e) throws SQLException {
        int country_id;
        try {
            country_id = getCountryID();
            if (nameField.getText().trim().isEmpty() || addressField.getText().isEmpty()
                    || cityField.getText().trim().isEmpty() || postalField.getText().trim().isEmpty()
                    || countryBox.getValue() == null || phoneField.getText().trim().isEmpty()) {
                Alert a = new Alert(AlertType.ERROR);
                a.setContentText("Please fill in all needed fields");
                a.show();
                return;
            }
        } catch (NullPointerException f) {
            Alert a = new Alert(AlertType.ERROR);
            a.setContentText("Please fill in all needed fields");
            a.show();
            return;
        }

//        Check the city to see if it already exists
        int city_id = -1;
        String cityname = cityField.getText().toLowerCase().trim();
        PreparedStatement city_search = conn.prepareStatement("SELECT * FROM city "
                + "WHERE city = '" + cityname + "' "
                + "AND countryId = " + country_id);
        ResultSet cities = city_search.executeQuery();

        while (cities.next()) {
            city_id = cities.getInt("cityId");
        }

        // If the city id is -1, it means it was not found in the database and we have to add the city before
        // other information.
        if (city_id == -1) {
            PreparedStatement insert_city = conn.prepareStatement("INSERT INTO city (city, countryId, createDate, createdBy, "
                    + "lastUpdate, lastUpdateBy) Values ('"
                    + cityname + "', " + country_id + ", "
                    + "NOW(), '"
                    + user.getUsername() + "', "
                    + "NOW(), '"
                    + user.getUsername() + "')"
            );
            insert_city.executeUpdate();
            cities = city_search.executeQuery();

            // Get the ID of the new city made
            while (cities.next()) {
                city_id = cities.getInt("cityId");
            }
        }

        // Here, we will check the address information to see if we get a similar/same address
        String add = addressField.getText().toLowerCase().trim();
        String add2 = address2Field.getText().toLowerCase().trim();
        String postal = postalField.getText().toLowerCase().trim();
        String phone = phoneField.getText().toLowerCase().trim();

        PreparedStatement add_Search = conn.prepareStatement("SELECT * FROM address "
                + "WHERE address = '" + add + "' AND "
                + "address2 = '" + add2 + "' AND "
                + "cityId = " + city_id + " AND "
                + "postalCode = '" + postal + "' AND "
                + "phone = '" + phone + "'");
        ResultSet add_res = add_Search.executeQuery();

        // Same as the city, if it remains as -1 after this execution then we will
        // add a new address to our database.
        int add_id = -1;
        while (add_res.next()) {
            add_id = add_res.getInt("addressId");
        }

        if (add_id == -1) {
            PreparedStatement insert_address = conn.prepareStatement("INSERT INTO address (address, address2, cityId, postalCode, "
                    + "phone, createDate, createdBy, lastUpdate, lastUpdateBy) Values ("
                    + "'" + add + "', "
                    + "'" + add2 + "', "
                    + city_id + ", "
                    + "'" + postal + "', "
                    + "'" + phone + "', "
                    + "NOW(), '"
                    + user.getUsername() + "', "
                    + "NOW(), '"
                    + user.getUsername() + "')"
            );
            insert_address.executeUpdate();
            add_res = add_Search.executeQuery();
            while (add_res.next()) {
                add_id = add_res.getInt("addressId");
            }
        }

        String cust_name = nameField.getText().trim();

        // Now we search for the customer and check the data
        PreparedStatement customer_search = conn.prepareStatement("SELECT * FROM customer "
                + "WHERE customerName = '" + cust_name + "' "
                + "AND addressId = " + add_id);
        ResultSet existing_customer = customer_search.executeQuery();

        int cust = -1;
        while (existing_customer.next()) {
            cust = existing_customer.getInt("customerId");
        }

        // If our customer ID is not negative on that means there was an exact match (this person is
        // already in the database with this address information)
        if (cust != -1) {
            msg.setText("This customer is already in the DB.");
        } else {
            PreparedStatement add_customer = conn.prepareStatement("INSERT INTO customer (customerName, addressId, active, createDate, createdBy, "
                    + "lastUpdate, lastUpdateBy) VALUES ('"
                    + cust_name + "', "
                    + add_id + ", "
                    + "1, "
                    + "NOW(), '"
                    + user.getUsername() + "', "
                    + "NOW(), '"
                    + user.getUsername() + "')"
            );

            // Adds new customer to DB and repopulates teh customerList.
            add_customer.executeUpdate();
            msg.setText("Customer Added!");
            customerTable.getItems().setAll(populateCustomerList());

            // Clear the fields once the customer is added.
            clearFields();

        }

    }

    // Method will load the information of the customer that was clicked.
    @FXML
    public void load_customer(MouseEvent e) {
        try {
            // Get the customer ID of the selected object
            int cust_id = customerTable.getSelectionModel().getSelectedItem().getId();

            // Find the customer's information and load it into the fields within the page
            PreparedStatement stmt = conn.prepareStatement("SELECT c.customerName,  a.address, a.address2, "
                    + "cit.city, a.postalCode, con.country, a.phone "
                    + "FROM customer c, address a,city cit, country con "
                    + "WHERE c.customerId = " + cust_id + " AND c.addressId = a.addressId AND a.cityId = cit.cityId AND cit.countryId = con.countryId");
            ResultSet result = stmt.executeQuery();
            result.next();
            nameField.setText(result.getString("c.customerName"));
            addressField.setText(result.getString("a.address"));
            address2Field.setText(result.getString("a.address2"));
            cityField.setText(result.getString("cit.city"));
            postalField.setText(result.getString("a.postalCode"));
            countryBox.setValue(result.getString("con.country"));
            phoneField.setText(result.getString("a.phone"));
        } catch (SQLException ex) {
            // Do nothing. This means the window was clicked but not a user.
        }
        catch (NullPointerException ex){}

    }

    // Method is the same as the last but, this is used by the calendar page where
    // the customerID is passed in
    public void load_customer(int x) {
        try {
            int cust_id = x;

            // This is a lambda expression to quickly look through our customer list and select the customer we are looking at
            // when loading from the calendar page.
            customerTable.getItems().stream().filter(Customer -> Customer.getId() == x).findAny().ifPresent(item -> {
                customerTable.getSelectionModel().select(item);
                customerTable.scrollTo(item);
            });;
            PreparedStatement stmt = conn.prepareStatement("SELECT c.customerName,  a.address, a.address2, "
                    + "cit.city, a.postalCode, con.country, a.phone "
                    + "FROM customer c, address a,city cit, country con "
                    + "WHERE c.customerId = " + cust_id + " AND c.addressId = a.addressId AND a.cityId = cit.cityId AND cit.countryId = con.countryId");
            ResultSet result = stmt.executeQuery();
            result.next();
            nameField.setText(result.getString("c.customerName"));
            addressField.setText(result.getString("a.address"));
            address2Field.setText(result.getString("a.address2"));
            cityField.setText(result.getString("cit.city"));
            postalField.setText(result.getString("a.postalCode"));
            countryBox.setValue(result.getString("con.country"));
            phoneField.setText(result.getString("a.phone"));

        } catch (SQLException ex) {
            // Do nothing. This means the window was clicked but not a user.
        }

    }

    // Updates a customer's information
    @FXML
    public void update_button(ActionEvent e) throws SQLException {

        // Get the customerId and selected countryID
        int cust_id;

        try {
            cust_id = customerTable.getSelectionModel().getSelectedItem().getId();
        } catch (NullPointerException f) {
            Alert a = new Alert(AlertType.INFORMATION);
            a.setContentText("Customer must be selected before updating.");
            a.show();
            return;
        }

        int country_id;
        try {
            country_id = getCountryID();
            if (nameField.getText().trim().isEmpty() || addressField.getText().isEmpty()
                    || cityField.getText().trim().isEmpty() || postalField.getText().trim().isEmpty()
                    || countryBox.getValue() == null || phoneField.getText().trim().isEmpty()) {
                Alert a = new Alert(AlertType.ERROR);
                a.setContentText("Please fill in all needed fields");
                a.show();
                return;
            }
        } catch (NullPointerException f) {
            Alert a = new Alert(AlertType.ERROR);
            a.setContentText("Please fill in all needed fields");
            a.show();
            return;
        }

        // We will repeat the steps we did when adding... We will check for any existing cities/addresses.
        int city_id = -1;
        String cityname = cityField.getText().toLowerCase().trim();
        PreparedStatement city_search = conn.prepareStatement("SELECT * FROM city "
                + "WHERE city = '" + cityname + "' "
                + "AND countryId = " + country_id);
        ResultSet cities = city_search.executeQuery();

        while (cities.next()) {
            city_id = cities.getInt("cityId");
        }

        if (city_id == -1) {
            PreparedStatement insert_city = conn.prepareStatement("INSERT INTO city (city, countryId, createDate, createdBy, "
                    + "lastUpdate, lastUpdateBy) Values ('"
                    + cityname + "', " + country_id + ", "
                    + "NOW(), '"
                    + user.getUsername() + "', "
                    + "NOW(), '"
                    + user.getUsername() + "')"
            );
            insert_city.executeUpdate();
            cities = city_search.executeQuery();
            while (cities.next()) {
                city_id = cities.getInt("cityId");
            }
        }

//        Checking the address ID
        String cust_name = nameField.getText().trim();
        String add = addressField.getText().toLowerCase().trim();
        String add2 = address2Field.getText().toLowerCase().trim();
        String postal = postalField.getText().toLowerCase().trim();
        String phone = phoneField.getText().toLowerCase().trim();

        PreparedStatement add_Search = conn.prepareStatement("SELECT * FROM address "
                + "WHERE address = '" + add + "' AND "
                + "address2 = '" + add2 + "' AND "
                + "cityId = " + city_id + " AND "
                + "postalCode = '" + postal + "' AND "
                + "phone = '" + phone + "'");
        ResultSet add_res = add_Search.executeQuery();

        int add_id = -1;
        while (add_res.next()) {
            add_id = add_res.getInt("addressId");
        }

        // If the address is the same or another that is already on record, we will just update the name and other information
        // the addressID can remain the same since it's a match
        if (add_id != -1) {

        } else {

            PreparedStatement insert_address = conn.prepareStatement("INSERT INTO address (address, address2, cityId, postalCode, "
                    + "phone, createDate, createdBy, lastUpdate, lastUpdateBy) Values ("
                    + "'" + add + "', "
                    + "'" + add2 + "', "
                    + city_id + ", "
                    + "'" + postal + "', "
                    + "'" + phone + "', "
                    + "NOW(), '"
                    + user.getUsername() + "', "
                    + "NOW(), '"
                    + user.getUsername() + "')"
            );
            insert_address.executeUpdate();
            add_res = add_Search.executeQuery();

            // Reload the address ID
            while (add_res.next()) {
                add_id = add_res.getInt("addressId");
            }
        }

        // Update the customer's information using their customer ID as the searching
        // method.
        PreparedStatement update_cust = conn.prepareStatement("UPDATE customer "
                + "SET customerName = '" + cust_name
                + "', addressId = " + add_id
                + ", lastUpdate = Now()"
                + ", lastUpdateBy = '" + user.getUsername() + "' "
                + "WHERE customerId = " + cust_id);
        update_cust.executeUpdate();
        msg.setText("Customer Updated!");
        customerTable.getItems().setAll(populateCustomerList());
        clearFields();
    }

    // Method deletes a customer from the database
    @FXML
    public void deleteButton(ActionEvent e) {
        try {
            int current_id = customerTable.getSelectionModel().getSelectedItem().getId();

            PreparedStatement customerQuery = conn.prepareStatement("SELECT * FROM customer "
                    + "WHERE customerId = " + current_id);
            ResultSet custom = customerQuery.executeQuery();
            custom.next();

            // We will get the addressID from the customer before they are deleted.
            int old_address = custom.getInt("addressId");

            // Delete the customer.
            PreparedStatement del_customer = conn.prepareStatement("DELETE FROM customer "
                    + "WHERE customer.customerId = " + current_id);
            del_customer.executeUpdate();
            msg.setText("Customer deleted!");

            // Check to see if more people/customers live at this address.
            PreparedStatement customer_at_add = conn.prepareStatement("SELECT * FROM customer "
                    + "WHERE customer.addressId = " + old_address);
            ResultSet customers_at_address = customer_at_add.executeQuery();

            int add_count = 0;
            while (customers_at_address.next()) {
                add_count = +1;
            }
            // If the amount of customers at this address is not 0, that means more people are there and we can 
            // leave the address alone
            if (add_count != 0) {

            } // Otherwise, delete the address that has no customers anymore
            else {
                PreparedStatement del_address = conn.prepareStatement("DELETE FROM address "
                        + "WHERE address.addressId= " + old_address);
                del_address.executeUpdate();
            }

            clearFields();
            customerTable.getSelectionModel().clearSelection();
            customerTable.getItems().setAll(populateCustomerList());
        } // This is thrown in case someone tries to delete a customer with an existing appointment.
        catch (SQLException ex) {
            msg.setText("Customer Has an Appointment.");
        }
        catch (NullPointerException f) {
            Alert a = new Alert(AlertType.INFORMATION);
            a.setContentText("Customer must be selected before deleting.");
            a.show();
            return;
        }

    }

    public int getCountryID() {
        String c = countryBox.getSelectionModel().getSelectedItem().toString();
        if (c.equals("United States")) {
            return 1;
        }
        if (c.equals("England")) {
            return 2;
        }
        if (c.equals("France")) {
            return 3;
        }
        if (c.equals("Italy")) {
            return 4;
        }
        if (c.equals("Canada")) {
            return 5;
        }
        if (c.equals("Mexico")) {
            return 6;
        }
        return 0;
    }

    // Clears the field of all the inputs.
    public void clearFields() {
        nameField.setText("");
        addressField.setText("");
        address2Field.setText("");
        cityField.setText("");
        postalField.setText("");
        countryBox.setValue(null);
        phoneField.setText("");

    }

}
