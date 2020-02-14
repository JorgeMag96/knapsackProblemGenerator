package com.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.models.*;
import com.tools.*;
import com.tools.files.*;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextFormatter.Change;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller{
	
	@FXML
    public void initialize() {
		System.out.println("Initializing controller...");
		
		number_of_instances.setValueFactory(
	            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20)
		        );		
		
		UnaryOperator<Change> number = change -> (change.getControlNewText().matches("([1-9][0-9]*)?")) ? change : null;
		ChangeListener<String> sliderCalcListener = (observable, oldValue, newValue) -> knapsackWeightCalc(knp_weight_percent.getValue());
		
		number_of_items.setTextFormatter(new TextFormatter<>(number));
		number_of_items.textProperty().addListener(sliderCalcListener);
		
		min_item_value.setTextFormatter(new TextFormatter<>(number));
		min_item_value.focusedProperty().addListener((observable, unfocused, focused) -> {
			if(unfocused) {
				int maxVal = Integer.parseInt(max_item_value.getText().isEmpty()?"0":max_item_value.getText());
				int minVal = Integer.parseInt(min_item_value.getText().isEmpty()? "0":min_item_value.getText());
				if( maxVal < minVal){
					min_item_value.setText(String.valueOf(maxVal));
					max_item_value.setText(String.valueOf(minVal));
				}
			}
		});
		
		max_item_value.setTextFormatter(new TextFormatter<>(number));
		max_item_value.focusedProperty().addListener((observable, unfocused, focused) -> {
			if(unfocused) {
				int maxVal = Integer.parseInt(max_item_value.getText().isEmpty()?"0":max_item_value.getText());
				int minVal = Integer.parseInt(min_item_value.getText().isEmpty()? "0":min_item_value.getText());
				if( maxVal < minVal){
					max_item_value.setText(String.valueOf(minVal));
					min_item_value.setText(String.valueOf(maxVal));
				}
			}
		});
		
		min_item_weight.setTextFormatter(new TextFormatter<>(number));
		min_item_weight.textProperty().addListener(sliderCalcListener);
		min_item_weight.focusedProperty().addListener((observable, unfocused, focused) -> {
			if(unfocused) {
				int maxVal = Integer.parseInt(max_item_weight.getText().isEmpty()?"0":max_item_weight.getText());
				int minVal = Integer.parseInt(min_item_weight.getText().isEmpty()? "0":min_item_weight.getText());
				if( maxVal < minVal){
					min_item_weight.setText(String.valueOf(maxVal));
					max_item_weight.setText(String.valueOf(minVal));
				}
			}
		});
		max_item_weight.setTextFormatter(new TextFormatter<>(number));		
		max_item_weight.textProperty().addListener(sliderCalcListener);
		max_item_weight.focusedProperty().addListener((observable, unfocused, focused) -> {
			if(unfocused) {
				int maxVal = Integer.parseInt(max_item_weight.getText().isEmpty()?"0":max_item_weight.getText());
				int minVal = Integer.parseInt(min_item_weight.getText().isEmpty()? "0":min_item_weight.getText());
				if( maxVal < minVal){
					max_item_weight.setText(String.valueOf(minVal));
					min_item_weight.setText(String.valueOf(maxVal));
				}
			}
		});
		
		knp_weight.setText(String.valueOf(knp_weight_percent.valueProperty().intValue()));
		knp_weight_percent.setSnapToTicks(true);
		knp_weight_percent.setMin(1);
		knp_weight_percent.setMax(100);		
		knp_weight_percent.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
            		knapsackWeightCalc(new_val.intValue());            		
                }			
            });
		
		heuristicType.getItems().addAll(HeuristicType.values());
		heuristicType.setValue(HeuristicType.RATIO);
				
		System.out.println("Controller initialization successful.");
    }
	
	public void knapsackWeightCalc(Number new_val) {
		int numberOfItems 	= Integer.parseInt(number_of_items.getText().isEmpty()? "0":number_of_items.getText());
		int minWeight		= Integer.parseInt(min_item_weight.getText().isEmpty()? "0":min_item_weight.getText());
		int maxWeight		= Integer.parseInt(max_item_weight.getText().isEmpty()? "0":max_item_weight.getText());
		
		int val = ((numberOfItems*((minWeight+maxWeight)/2))*new_val.intValue())/100;
		
        knp_weight.setText(String.valueOf(val));
	}
	
	public void selectInstanceImp() {

		FileChooser instanceFileChooser = new FileChooser();
		instanceFileChooser.getExtensionFilters().addAll(
			     new FileChooser.ExtensionFilter("Text Files", "*.txt")			    
			);
		instanceFileChooser.setInitialDirectory(new File(System.getProperty("user.dir")+"\\instances"));
		
		File selectedInstanceFile = instanceFileChooser.showOpenDialog(stage);		
		
		instance_file_field.setText((selectedInstanceFile != null)? selectedInstanceFile.toString():"");		

	}
	
	public void runHeuristicImp() {

		try {
			DeserializedInstance deserializedInstance = new DeserializedInstance(instance_file_field.getText());			
			Knapsack knapsack = deserializedInstance.getKnapsack();	        
			Item[] items = deserializedInstance.getItems();
			Heuristic heuristic = heuristicChoice(heuristicType.getValue(), knapsack, items);
		    heuristic.runHeuristic();
		    String resultFileName = "result_"+deserializedInstance.getInstanceFileName()+"_"+heuristicType.getValue().toString()+".txt";
		    saveResults(heuristic.getKnapsack(), resultFileName);
		}
		catch(IOException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(e.getMessage());
			alert.setHeaderText("No results were produced");
			alert.setContentText("Please select a valid instance file.");
			alert.showAndWait();
		}		
	}
	
	private Heuristic heuristicChoice(HeuristicType type, Knapsack knapsack, Item[] items) {

    	Heuristic heuristic = null;

    	switch(type) {
    		case RATIO:{
    			heuristic = new RatioHeuristic(knapsack, items);
    			break;
    		}
    		case VALUE:{
    			heuristic = new ValueHeuristic(knapsack, items);
    			break;
    		}
    		case WEIGHT:{
    			heuristic = new WeightHeuristic(knapsack, items);
    			break;
    		}
    		default:{
    			System.out.println("Heuristic type selection error");
    		}
    	}

    	return heuristic;
    }
	
	private boolean missingInformationDialog() {
		Alert sucess = new Alert(AlertType.INFORMATION);
		sucess.setTitle("Incomplete information to generate instances");
		sucess.setHeaderText("No instances were generated.");
		sucess.setContentText("Please make sure to provide all the required information.");
		sucess.showAndWait();
		return false;
	}
	
	private boolean isOkToGenerateInstances() {

		List<String> errors = new ArrayList<>();
		
		if(number_of_items.getText().isEmpty()) {
			return missingInformationDialog();
		}
		
		if(!min_item_value.getText().isEmpty() || !max_item_value.getText().isEmpty()) {
			if(Integer.parseInt(min_item_value.getText()) > Integer.parseInt(max_item_value.getText())) {
				errors.add("Item max value cannot be less than min value");			
				max_item_value.setText("");
			}
		}
		else {
			return missingInformationDialog();
		}
		
		if(!min_item_weight.getText().isEmpty() || !max_item_weight.getText().isEmpty()) {
			if(Integer.parseInt(min_item_weight.getText()) > Integer.parseInt(max_item_weight.getText())) {
				errors.add("Item max weight cannot be less than min weight");			
				max_item_weight.setText("");
			}
		}
		else {
			return missingInformationDialog();	
		}
		
		if(knp_weight.getText().isEmpty()) {
			return missingInformationDialog();
		}
		
		if(!errors.isEmpty()){
			Alert sucess = new Alert(AlertType.INFORMATION);
			sucess.setTitle("Invalid values");
			sucess.setHeaderText("No instances were generated.");
			StringBuilder content = new StringBuilder();
			errors.forEach(error -> content.append(error+"\n"));
			sucess.setContentText(content.toString());
			sucess.showAndWait();
			return false;
		}

		return true;
	}
	
	public void generateInstances() {
		if(isOkToGenerateInstances()) {
			ArrayList<String> newInstances = new ArrayList<>();
			int numberOfItems 	= Integer.parseInt(number_of_items.getText());
			int minValue 		= Integer.parseInt(min_item_value.getText());
			int maxValue 		= Integer.parseInt(max_item_value.getText());
			int minWeight		= Integer.parseInt(min_item_weight.getText());
			int maxWeight		= Integer.parseInt(max_item_weight.getText());		
			
			for(int i = 1; i <= number_of_instances.getValue(); i++) {
				
				Item[] items 		= ItemsGenerator.generate(numberOfItems, minValue, maxValue, minWeight, maxWeight);			
				Knapsack knapsack 	= new Knapsack(Integer.parseInt(knp_weight.getText()));
								
				try {					
					newInstances.add(SerializedInstance.saveInstance(knapsack,items));
				} catch (IOException e) {					
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Dialog");
					alert.setHeaderText(e.getMessage());
					alert.setContentText("Error while serializing instance ");
					alert.showAndWait();
				}				
			}
			StringBuilder content = new StringBuilder();
			newInstances.forEach(instance -> content.append("New instance generated: "+instance+"\n"));
			Alert sucess = new Alert(AlertType.INFORMATION);
			sucess.setTitle("Information dialog");
			sucess.setHeaderText("Instance(s) generated successfully !");
			sucess.setContentText(content.toString());
			sucess.showAndWait();
		}		
		resetFields();		
	}
	
	private void resetFields() {
		instance_file_field.clear();
		number_of_items.clear();
		min_item_value.clear();
		max_item_value.clear();
		min_item_weight.clear();
		max_item_weight.clear();
		knp_weight_percent.setValue(0);
	}
	
	private void saveResults(Knapsack knapsack, String resultFileName) {
		
		File resultFile = new File(System.getProperty("user.dir")+"\\results\\"+resultFileName);
		try {
			FileWriter writer = new FileWriter(resultFile);
			writer.write("Knapsack result = "+knapsack.getTotalValue());
			writer.flush();
			writer.close();
			Alert sucess = new Alert(AlertType.INFORMATION);
			sucess.setTitle("Information dialog");
			sucess.setHeaderText("Heuristic results successfully saved !");
			sucess.setContentText("Result file: "+resultFileName);
			sucess.showAndWait();
			instance_file_field.clear();
		} catch (IOException e) {					
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Dialog");
			alert.setHeaderText(e.getMessage());
			alert.setContentText("Error while saving results");
			alert.showAndWait();
		}
		
		System.out.println("Knapsack result = "+knapsack.getTotalValue());
		System.out.println("Heuristic results saved successfully.");
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	private Stage		stage;
	public Button		slctHeuristicBtn;
	public Button 		runHeuristicBtn;
	public Button 		generateBtn;
	public TextField	instance_file_field;
	public TextField 	number_of_items;
	public TextField 	min_item_value;
	public TextField 	max_item_value;
	public TextField 	min_item_weight;
	public TextField 	max_item_weight;
	public Label 		knp_weight;
	public Slider 		knp_weight_percent;
	public Spinner<Integer> 		number_of_instances;
	public ComboBox<HeuristicType> 	heuristicType;	
}
