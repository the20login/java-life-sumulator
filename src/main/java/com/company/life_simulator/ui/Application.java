package com.company.life_simulator.ui;

import com.company.life_simulator.ui.window.Controller;
import com.company.life_simulator.world.WorldPlayer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Application extends javafx.application.Application {
    private final WorldPlayer player;

    public Application() {
        player = new WorldPlayer(100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        /*BorderPane root = new BorderPane();
        ResizableCanvas canvas = new ResizableCanvas(canvas1 -> WorldDrawer.drawWorld(world, canvas1));
        HBox hbox = new HBox(canvas);
        root.setCenter(hbox);

        ToggleButton toggle = new ToggleButton("Toggle color");
        VBox controls = new VBox(5, toggle);
        controls.setAlignment(Pos.CENTER);
        root.setRight(controls);

//        vbox.styleProperty().bind(Bindings.when(toggle.selectedProperty())
//                .then("-fx-background-color: cornflowerblue;")
//                .otherwise("-fx-background-color: white;"));

        hbox.backgroundProperty().set(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println(hbox.getWidth());*/
        /*;

        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(canvas);

        // Bind canvas size to stack pane size.
        canvas.widthProperty().bind(
                stackPane.widthProperty());
        canvas.heightProperty().bind(
                stackPane.heightProperty());

        primaryStage.setScene(new Scene(stackPane));
        primaryStage.setTitle("World");
        WorldDrawer.drawWorld(world, canvas);

        primaryStage.show();*/

        URL path = getClass().getResource("/layout.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(path);
        Parent root = loader.load();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 800, 600));

        Controller controller = loader.getController();
        controller.setWorldPlayer(player);
        primaryStage.setOnCloseRequest(event -> player.destroy());

        primaryStage.show();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Properties properties = new Properties();
        properties.load(Application.class.getResourceAsStream("/application.properties"));
        properties.stringPropertyNames().stream()
                .filter(name -> System.getProperty(name) == null)
                .forEach(name -> System.setProperty(name, properties.getProperty(name)));

        Application.launch(args);
    }
}