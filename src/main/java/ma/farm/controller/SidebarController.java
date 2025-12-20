package ma.farm.controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.*;
import javafx.util.Duration;

public class SidebarController {

    @FXML private VBox sidebar;
    @FXML private HBox sidebarHeader;

    @FXML private Text titleText;

    @FXML private ScrollPane sidebarScroll;

    @FXML private AnchorPane dashboardBtn;
    @FXML private AnchorPane chickenBtn;
    @FXML private AnchorPane eggsBtn;
    @FXML private AnchorPane storageBtn;
    @FXML private AnchorPane taskBtn;
    @FXML private AnchorPane personnelBtn;
    @FXML private AnchorPane logoutBtn;

    @FXML private Button sidebarMenu;

    private final List<AnchorPane> modules = new ArrayList<>();
    private boolean isCollapsed = false;
    public final double expandedWidth = 180.0;
    public final double collapsedWidth = 50.0;

    // MAIN width controller
    private final DoubleProperty sidebarWidth = new SimpleDoubleProperty(expandedWidth);

    private MainWindowController mainController;

    @FXML
    private void initialize() {

        // Bind sidebar width to the animated property
        sidebar.minWidthProperty().bind(sidebarWidth);
        sidebar.maxWidthProperty().bind(sidebarWidth);
        sidebar.prefWidthProperty().bind(sidebarWidth);

        sidebarHeader.minWidthProperty().bind(sidebarWidth);
        sidebarHeader.maxWidthProperty().bind(sidebarWidth);
        sidebarHeader.prefWidthProperty().bind(sidebarWidth);

        sidebarMenu.setText("<");
        dashboardBtn.getStyleClass().add("sidebar-item-active");

        modules.add(dashboardBtn);
        modules.add(chickenBtn);
        modules.add(eggsBtn);
        modules.add(storageBtn);
        modules.add(taskBtn);
        modules.add(personnelBtn);
        modules.add(logoutBtn);

        for (AnchorPane module : modules) {
            if (module != null) {
                module.setOnMouseClicked(event -> activate(module));
            }
        }

        sidebarMenu.setOnMouseClicked(event -> toggleSidebar());
        sidebarScroll.setFitToWidth(true); // makes scroll content resize nicely
    }

    public void setMainController(MainWindowController mainController) {
        this.mainController = mainController;
    }


    private void animateSidebar(double targetWidth) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(sidebarWidth, targetWidth, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }

    private void collapseSidebar() {
        titleText.setVisible(false);
        sidebarMenu.setText(">");

        animateSidebar(collapsedWidth);
    }

    private void expandSidebar() {
        titleText.setVisible(true);
        sidebarMenu.setText("<");

        animateSidebar(expandedWidth);
    }

    @FXML
    private void toggleSidebar() {
        if (isCollapsed) {
            expandSidebar();
            sidebar.getStyleClass().remove("sidebar-collapsed");
            logoutBtn.getStyleClass().remove("sidebar-collapsed");
            sidebarHeader.getStyleClass().remove("sidebar-collapsed");
        } else {
            collapseSidebar();
            sidebar.getStyleClass().add("sidebar-collapsed");
            logoutBtn.getStyleClass().add("sidebar-collapsed");
            sidebarHeader.getStyleClass().add("sidebar-collapsed");
        }
        isCollapsed = !isCollapsed;
    }


    private void activate(AnchorPane module) {
        if (module == null) return;

        for (AnchorPane ap : modules) {
            if (ap != null) {
                ap.getStyleClass().remove("sidebar-item-active");
            }
        }

        module.getStyleClass().add("sidebar-item-active");

        if (mainController != null) {
            switch (module.getId()) {
                case "dashboardBtn" -> mainController.showDashboard();
                case "chickenBtn" -> mainController.showChickenBay();
                case "eggsBtn" -> mainController.showEggsBay();
                case "storageBtn" -> mainController.showStorage();
                case "taskBtn" -> mainController.showTasks();
                case "personnelBtn" -> mainController.showPersonnel();
                case "logoutBtn" -> mainController.handleLogout();
            }
        }
    }
}
