package ma.farm.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import ma.farm.dao.EggProductionDAO;
import ma.farm.dao.HouseDAO;
import ma.farm.util.DateUtil;

import java.time.LocalDate;

public class DashboardController {

    @FXML
    private Label totalChickensLabel;

    @FXML
    private Label eggsTodayLabel;

    @FXML
    private Label alertsCountLabel;

    @FXML
    private LineChart<String, Number> eggsChart;

    private HouseDAO houseDAO;
    private EggProductionDAO eggProductionDAO;

    @FXML
    public void initialize() {
        try {
            houseDAO = new HouseDAO();
            eggProductionDAO = new EggProductionDAO();
        } catch (Exception e) {
            System.err.println("Error initializing DAOs: " + e.getMessage());
        }

        refreshDashboard();
    }




    private void loadAlertsCount() {
        alertsCountLabel.setText("0");
    }

    private void load7DayChart() {
        try {
            eggsChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Production des 7 jours");

            LocalDate[] last7Days = DateUtil.getLast7Days();



            eggsChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error loading 7-day chart: " + e.getMessage());
        }
    }

    public void refreshDashboard() {


        loadAlertsCount();
        load7DayChart();
    }
}
