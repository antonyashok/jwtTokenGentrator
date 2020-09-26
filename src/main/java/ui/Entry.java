package ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Entry extends Application{
	@Override
	public void start(Stage stage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("uiV2.fxml"));
		
		Scene scene = new Scene(root, 869, 637);
		
		stage.setTitle("JWT Token Gentrator");
		stage.setScene(scene);
		stage.show();
		
	}
	
	public static void main(String[] args) {   
		launch(args);
	}
}