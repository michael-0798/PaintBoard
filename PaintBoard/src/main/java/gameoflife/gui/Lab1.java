package gameoflife.gui;

import java.awt.Panel;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class Lab1 extends Application{

	private static final int CELL_WIDTH = 10;
	private static final int CELL_HEIGHT = 10;
	private static final int CELL_COUNT_ROW = 60;
	private static final int CELL_COUNT_COL = 60;

	private static final String CELL_DEAD_STYLE_ID = "cell";
	private static final String CELL_ALIVE_STYLE_ID = "cell_selected";
	private static final String BUTTON_EDDIT_ICON_STYLE_ID = "button_edit";
	private static final String BUTTON_ERASE_ICON_STYLE_ID = "button_erase";
	private static final String BUTTON_RESET_ICON_STYLE_ID = "button_reset";
	private static final String BUTTON_INFO_ICON_STYLE_ID = "button_info";
	private static final String CREDIT_TEXT_PATH = "credit.txt";

	private enum Tool {
		PEN, ERASER
	}

	private static final String TITLE = "Conway's Game Of Life - Skeleton";

	private GridPane grid;
	private BorderPane root;
	private ToolBar menuBar;
	private ToolBar statusBar;
	private ToggleGroup selectedTool;

	private Alert infoDialog;
	private Label generationCount;

	private int generation;

	private Label[][] cells;

	public void init() throws Exception {
		cells = new Label[CELL_COUNT_ROW][CELL_COUNT_COL];
		
		grid = new GridPane();
		menuBar = new ToolBar();
		statusBar = new ToolBar();
		selectedTool=new ToggleGroup();
				
		createToolBar();
		createStatusBar();
		createGridContent(CELL_COUNT_ROW, CELL_COUNT_COL);

		root = new BorderPane();
		
		root.setCenter(grid);
		root.setTop(menuBar);
		root.setBottom(statusBar);
		
		root.setOnKeyPressed(keyEvent ->{
			if( keyEvent.getCode() == KeyCode.SPACE ) {
				generation++;
				
				generationCount.setText(String.valueOf(generation));
			}
		});
	}

	public void start( Stage primaryStage) throws Exception {
		infoDialog = new Alert( AlertType.INFORMATION);
		infoDialog.setContentText( Files.readString( Paths.get( CREDIT_TEXT_PATH)));
		
		Scene scene = new Scene( root);
		scene.getStylesheets().add( "root.css");
		primaryStage.setScene( scene);
		primaryStage.setTitle( TITLE);
		primaryStage.setResizable( true);
		primaryStage.addEventHandler( KeyEvent.KEY_RELEASED, ( KeyEvent event) -> {

			if ( !event.isConsumed() && KeyCode.ESCAPE == event.getCode()) {
				primaryStage.hide();
			}
		});
		
		primaryStage.show();
		grid.requestFocus();
	}

	/**
	 * Helper method to create the ToolBar at the top. This will hold the options.
	 */
	private void createToolBar() {
		ToggleButton b1 = createButton(ToggleButton.class, BUTTON_EDDIT_ICON_STYLE_ID, false, Tool.PEN,null);
		ToggleButton b2 = createButton(ToggleButton.class, BUTTON_ERASE_ICON_STYLE_ID, false, Tool.ERASER,null);

		b1.setToggleGroup(selectedTool);
		b2.setToggleGroup(selectedTool);
		Button b3 = createButton(Button.class, BUTTON_RESET_ICON_STYLE_ID, false, null, event ->{
			ObservableList<Node> list = grid.getChildren();
			list.forEach((ll)->{
				ll.setId(CELL_DEAD_STYLE_ID);
			});
		});
		
		Pane p1 = new Pane();
		
		HBox.setHgrow(p1, Priority.ALWAYS);

		Button b4 = createButton(Button.class, BUTTON_INFO_ICON_STYLE_ID, false, null, event -> {
				infoDialog.showAndWait();
		});
		
		ObservableList<Node> list = menuBar.getItems();
		Separator sep = new Separator();
		
		list.addAll(b1,b2,sep,b3,p1,b4);

	}

	/**
	 * This is a helper method to create different types of buttons.<br>
	 * In JavaFX buttons inherit from the class ButtonBase. Meaning if a generic
	 * method return the ButtonBase class we can create any button in the same method.<br>
	 * <br>
	 * Then why use generic? Why not just return the return type ButtonBase?<br>
	 * Using generic we can have the return type of the desired type, meaning we wont have
	 * to cast it to get access to special methods. It is more convenient.
	 * 
	 * @param <T> - generic type of method which inherits from ButtonBase.
	 * @param clazz - The class type of the object we are creating. This is used to find the constructor with reflection.
	 * @param id - CSS id used for the button.
	 * @param focusable - is this button focusable. 
	 * @param userDta - special user data to store in the object. We will use this for ToggleButton to store the Tool type.
	 * @param action - the lambda for setOnAction event.
	 * @return a fully initialized button.
	 */
	private < T extends ButtonBase> T createButton( Class< T> clazz, String id, boolean focusable, Object userDta,
			EventHandler< ActionEvent> action) {
		T button = null;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			button = constructor.newInstance();
		} catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException( e);
		}

		button.setFocusTraversable(focusable);
		button.setUserData(userDta);
		button.setId(id);
		button.setOnAction(action);
		return button;
	}

	/**
	 * This is helper method to create the labels in the main grid.
	 * @param rows - the number of rows in the grid.
	 * @param cols - the number of columns in the the grid.
	 */
	private void createGridContent( int rows, int cols) {
		for ( int row = 0; row < rows; row++) {
			for ( int col = 0; col < cols; col++) {
				Label l = createLabel( row, col);
				cells[row][col] = l;
				grid.add( l, col, row);
			}
		}
	}

	/**
	 * This is a utility method for creating a Label.
	 * @param row - the row at which the label is placed.
	 * @param col - the column at which the label is placed.
	 * @return a fully initialized label object.
	 */
	private Label createLabel( int row, int col) {
		Label ll = new Label();
		
		ll.setMaxSize(CELL_WIDTH, CELL_HEIGHT);
		ll.setMinSize(CELL_WIDTH, CELL_HEIGHT);
		ll.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
		
		ll.setId(CELL_DEAD_STYLE_ID);

		ll.setOnMousePressed(ee ->{
			labelMouseAction(ll, row, col);
		});
		
		ll.setOnMouseDragEntered(ee ->{
			labelMouseAction(ll, row, col);
		});
		
		
		ll.setOnDragDetected(ee->{
			ll.startFullDrag();
		});
		
		return ll;
	}

	
	/**
	 * this method is used to determine the action of the mouse on a given label.
	 * 
	 * @param l - the effected label
	 * @param row - the row at which the label is placed.
	 * @param col - the column at which the label is placed.
	 */
	private void labelMouseAction( Label l, int row, int col) {
		Toggle b1 = selectedTool.getSelectedToggle();
		if(b1!=null) {
			Object selectTool = b1.getUserData();
			
			switch((Tool)selectTool) {
			case ERASER:
				l.setId(CELL_DEAD_STYLE_ID);
				break;
			case PEN:
				l.setId(CELL_ALIVE_STYLE_ID);
				break;
			}
		}
	}

	/**
	 * create and initialize all the objects that are needed for the ToolBar at the bottom of GUI.
	 */
	private void createStatusBar() {
		Label ll = new Label("Generation: ");
		
		generationCount= new Label("0");
		
		Pane pp = new Pane();
		
		HBox.setHgrow(pp, Priority.ALWAYS);
		
		Label l2 = new Label("Press and Hold Space");

		ObservableList<Node> list = statusBar.getItems();
		list.addAll(ll,generationCount,pp,l2);
	}

	public static void main( String[] args) {
		launch(args);
	}
}
