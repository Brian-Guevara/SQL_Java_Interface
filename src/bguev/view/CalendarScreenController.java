/**
 *
 * Brian Guevara
 * WGU ID: 001003681
 */
package bguev.view;

import bguev.classes.Appointment;
import bguev.classes.User;
import bguev.util.DBConnect;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CalendarScreenController implements Initializable {

    @FXML
    private TableView<Appointment> calendarTable;

    @FXML
    private TableColumn<Appointment, LocalDate> dateColumn;

    @FXML
    private TableColumn<Appointment, String> startColumn;

    @FXML
    private TableColumn<Appointment, String> endColumn;

    @FXML
    private TableColumn<Appointment, String> customerColumn;

    @FXML
    private TableColumn<Appointment, String> typeColumn;

    @FXML
    private RadioButton all_button;

    @FXML
    private DatePicker picker;
    @FXML
    private RadioButton week;

    @FXML
    private RadioButton month;

    @FXML
    private Label msg;

    private Connection conn;

    private User user;

    private final ZoneId z_id = ZoneId.systemDefault();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conn = DBConnect.getConn();

        msg.setText("");

        // Setup our columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // By default, the 'All' radio button will be selected
        all_button.setSelected(true);

        // Populate the calendar
        try {
            popTable();
        } catch (SQLException ex) {
            Logger.getLogger(CalendarScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Load the user who is on this page
    protected void loadUser(User x) {
        this.user = x;

    }

    // Set the text in the label/msg box
    protected void setTheMsg(String s) {
        msg.setText(s);
    }

    // Delete the currently selected appointment from the calendar.
    @FXML
    public void delete_press(ActionEvent e) throws SQLException {
        try {
            int app_id = calendarTable.getSelectionModel().getSelectedItem().getAppointmentId();
            PreparedStatement del_app = conn.prepareStatement("DELETE FROM appointment "
                    + "WHERE appointment.appointmentId = " + app_id);
            del_app.executeUpdate();
            popTable();
            msg.setText("Appointment deleted.");
        } catch (NullPointerException f) {
            // this is to prevent an error from appearing when you press the delete button without
            // any current selection
            msg.setText("Select an appointment.");
        }

    }

    // This method is assigned to the date picker which just goes back to the
    // populate table method.
    public void getDateFromPicker() throws SQLException {
        popTable();

    }

    // Load the add appointment page if clicked. 
    public void addAppointment_press(ActionEvent e) throws IOException {
        try {
            msg.setText("");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AppointmentAddScreen.fxml"));
            Parent AppointmentScreenFXML = loader.load();

            Scene nextScene = new Scene(AppointmentScreenFXML);

            AppointmentAddScreenController controller = loader.getController();
            Stage stage = new Stage();

            stage.initOwner(calendarTable.getScene().getWindow());
            stage.setTitle("Appointment Page");
            stage.setScene(nextScene);
            controller.loadUser(user);

            // Hide the current window            
            stage.show();

            // This is done so we can edit the MSG box from the appointment page.
            controller.setController(this);

        } catch (RuntimeException f) {
        }
    }

    // Open the edit appointment page
    public void editAppointment_press(ActionEvent e) throws IOException, SQLException {
        try {
            // Get the appointment ID of the selected appointment
            int appt_id = calendarTable.getSelectionModel().getSelectedItem().getAppointmentId();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AppointmentEditScreen.fxml"));
            Parent AppointmentScreenFXML = loader.load();

            Scene nextScene = new Scene(AppointmentScreenFXML);

            AppointmentEditScreenController controller = loader.getController();
            Stage stage = new Stage();

            stage.initOwner(calendarTable.getScene().getWindow());
            stage.setTitle("Appointment Page");
            stage.setScene(nextScene);

            // Load the current user and appointment ID into the edit page.
            controller.loadUser(user);
            controller.loadAppointment(appt_id);
            // Hide the current window            
            stage.show();

            // This is done so we can edit the MSG box from the appointment page.
            controller.setController(this);

        } catch (NullPointerException f) {
            msg.setText("Select an appointment.");
        } catch (RuntimeException f) {
        }
    }

    // The following methods work like this:
    // If the All, Week, or Month button is selected then fill that one in and
    // turn off the other 2. Then repopulate the table.
    public void all_selected(ActionEvent e) throws SQLException {
        month.setSelected(false);
        all_button.setSelected(true);
        week.setSelected(false);
        popTable();

    }

    public void weekly_selected(ActionEvent e) throws SQLException {
        month.setSelected(false);
        all_button.setSelected(false);
        week.setSelected(true);
        popTable();

    }

    public void monthly_selected(ActionEvent e) throws SQLException {
        month.setSelected(true);
        all_button.setSelected(false);
        week.setSelected(false);
        popTable();
    }

    // This method opens the CustomerEdit page with a customer selected from the calendar.
    public void goToCustomer() throws SQLException, IOException {
        // Get the appointment id of the selected item
        int apt_id = 0;
        String cust_name = null;
        try {
            apt_id = calendarTable.getSelectionModel().getSelectedItem().getAppointmentId();
            cust_name = calendarTable.getSelectionModel().getSelectedItem().getCustomer();
        } catch (NullPointerException e) {
            msg.setText("Select a customer");
            return;
        }
        // Run a search to get the customerID
        PreparedStatement id_finder = conn.prepareStatement("SELECT customer.customerId FROM customer, appointment "
                + "WHERE appointment.customerId = customer.customerId AND customer.customerName = '" + cust_name
                + "' AND appointment.appointmentId = " + apt_id);
        ResultSet rs = id_finder.executeQuery();
        int cust_id = 0;
        while (rs.next()) {
            cust_id = rs.getInt("customer.customerId");

        }
        // Load the page with the customerID
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerEdit.fxml"));
            Parent CustomerEditFXML = loader.load();

            Scene nextScene = new Scene(CustomerEditFXML);

            CustomerEditController CustomerEditController = loader.getController();
            Stage stage = new Stage();

            CustomerEditController.setUser(user);

            // This is where we use the 2nd load_customer Method from the CustomerEdit page
            CustomerEditController.load_customer(cust_id);
            stage.initOwner(msg.getScene().getWindow());

            stage.setScene(nextScene);

            // Hide the current window
            stage.setTitle("Customer Records");
            stage.show();

        } catch (RuntimeException f) {
        }

    }

    public void popTable() throws SQLException {
        String where_clause = "";
        LocalDate date = picker.getValue();

        // We will make a string to add to our where clause. The outcomes of this
        // string depends on which radio button is selected AND if there is a date
        // in the date picker.
        // If there is no date then we will just do ALL appointments (ALL),
        // upcoming appointments this week/month (depending on which button selected)
        if (date == null) {
            if (all_button.isSelected()) {
            } else if (week.isSelected()) {
                where_clause = "AND curdate() <= a.start "
                        + "AND a.start <= date_add(curdate(), INTERVAL 7 DAY) ";
            } else if (month.isSelected()) {
                where_clause = "AND curdate() <= a.start "
                        + "AND a.start <= date_add(curdate(), INTERVAL 30 DAY) ";
            }

        } else {
            ZonedDateTime userDay = date.atStartOfDay(z_id);
            ZonedDateTime utcDay = userDay.withZoneSameInstant(ZoneId.of("UTC"));

            if (all_button.isSelected()) {
                where_clause = "AND '" + utcDay + "' <= a.start "
                        + "AND a.start <= date_add('" + utcDay + "', INTERVAL 1 DAY) ";
            } else if (week.isSelected()) {
                where_clause = "AND '" + utcDay + "' <= a.start "
                        + "AND a.start <= date_add('" + utcDay + "', INTERVAL 7 DAY) ";
            } else if (month.isSelected()) {
                where_clause = "AND '" + utcDay + "' <= a.start "
                        + "AND a.start <= date_add('" + utcDay + "', INTERVAL 30 DAY) ";
            }

        }
        // If there is a date, then we will get all the appointments on that date (ALL),
        // or we will get the appointments coming up from that week/month

        // Create our search statement
        PreparedStatement stmt = conn.prepareStatement("SELECT a.appointmentId, a.start, a.end, c.customerName,a.type "
                + "FROM appointment a, customer c "
                + "WHERE a.customerId = c.customerId "
                + where_clause
                + "ORDER BY a.start");
        ResultSet rs = stmt.executeQuery();

        // Create a list for our appointments, get the data, and add it to the list.
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        ZonedDateTime zStart;
        ZonedDateTime lStart;

        ZonedDateTime zEnd;
        ZonedDateTime lEnd;
        String customer;
        String type;
        int id;

        while (rs.next()) {

            zStart = rs.getTimestamp("a.start").toLocalDateTime().atZone(ZoneId.of("UTC"));
            lStart = zStart.withZoneSameInstant(z_id);

            zEnd = rs.getTimestamp("a.end").toLocalDateTime().atZone(ZoneId.of("UTC"));
            lEnd = zEnd.withZoneSameInstant(z_id);
            customer = rs.getString("c.customerName");
            type = rs.getString("a.type");
            id = rs.getInt("a.appointmentId");
            Appointment apt = new Appointment(id, customer, type, lStart, lEnd);
            appointmentList.add(apt);

        }

        calendarTable.getItems()
                .setAll(appointmentList);

    }
}
