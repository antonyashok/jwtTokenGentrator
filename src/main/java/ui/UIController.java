package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;


public class UIController {

	@FXML private TextField lanId;
	@FXML private TextField password;
	@FXML private TextField clientId;
	@FXML private TextField secretId;
	@FXML private Text lanIdError;
	@FXML private Text passwordError;
	@FXML private Text clientIdError;
	@FXML private Text secretIdError;
	@FXML private TextArea txtArea;

	
	
	@FXML protected void selectCacheFile(ActionEvent event){
		System.out.println(lanId.getText());
		System.out.println(password.getText());
		System.out.println(clientId.getText());
		System.out.println(secretId.getText());
		
		    RestTemplate restTemplate = new RestTemplate();
		     
		    final String baseUrl = "http://localhost:"+randomServerPort+"/employees/";
		    URI uri = new URI(baseUrl);
		     
		    Employee employee = new Employee("test","123","23");
		 
		    ResponseEntity<String> result = restTemplate.postForEntity(uri, employee, String.class);
		     
	}

	/**This gets called automatically once the matching .fxml file has been loaded.*/
	public void initialize(){
		System.out.println("UIController.initialize() called");
		txtArea.setDisable(true);
	}
}

