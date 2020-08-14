package controllers;

import Utilities.GeneralUtils;
import View.CustomLabel;
import View.PlayView;
import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.IFactory;
import model.NumbersFactory;

public class PlayController implements EventHandler {
		
	private PlayView playView;
	private Pane root;
	private Stage stage;
	private GraphicsContext gc;
	private Canvas canvas;

	public boolean isLeftKeyPressed;
	public boolean isRightKeyPressed;

	private static final int GAME_WIDTH = 600;
	private static final int GAME_HEIGHT = 800;

	public GridPane gridPane;
	public GridPane gridPane2;


	private int angle;
	private ImageView ship;

	private ImageView[] greyMeteors;

	// points and lives. make static capital
	private ImageView star;
	private CustomLabel customLabel;
	private ImageView[] playerLives;
	private int playerLife;
	private int points;

	//collision
	private static final int STAR_RADIUS  = 12;
	private static final int SHIP_RADIUS  = 27;
	private static final int METEOR_RADIUS  = 20;

	private Group group;

	//factory for objects
	private IFactory factory;


	public GeneralUtils generalUtils;

	// TODO
	// logic for points greater then 20
	// much better collision calculation
	// try add fonts to make it better
	// it doesn't close after life ended
	public PlayController(Stage stage)
		{
			root = new Pane();
			generalUtils = new GeneralUtils();
			canvas = new Canvas(GAME_WIDTH,GAME_HEIGHT);

			gc = canvas.getGraphicsContext2D();

			gc.setFill(Color.WHITE);

			gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());


			this.stage = stage;

			factory = new NumbersFactory();


			createBackground();
			createShip();
			createGameElements();
			createOperands();

			playView = new PlayView(this, stage, root, GAME_WIDTH, GAME_HEIGHT);
		}

	@Override
	public void handle(Event event) {

		KeyEvent keyEvent = (KeyEvent) event;

		if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED){

			if (keyEvent.getCode() == KeyCode.LEFT)
			{
				isLeftKeyPressed = true;
			}
			if (keyEvent.getCode() == KeyCode.RIGHT)
			{
				isRightKeyPressed = true;
			}
		} else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED){
			if (keyEvent.getCode() == KeyCode.LEFT)
			{
				isLeftKeyPressed = false;
			}
			if (keyEvent.getCode() == KeyCode.RIGHT)
			{
				isRightKeyPressed = false;
			}
		}
	}


	public void moveShip()
	{
		if (isLeftKeyPressed && !isRightKeyPressed){
			if (angle > -30){
				angle -= 5;
			}
			ship.setRotate(angle);
			if (ship.getLayoutX() > -20)
			{
				ship.setLayoutX(ship.getLayoutX() -3);
			}
		}
		if (!isLeftKeyPressed && isRightKeyPressed){
			if (angle < 30){
				angle += 5;
			}
			ship.setRotate(angle);
			if (ship.getLayoutX() < 522)
			{
				ship.setLayoutX(ship.getLayoutX() +3);
			}
		}
		if (!isLeftKeyPressed && !isRightKeyPressed){
			if (angle < 0) {
				angle += 5;
			} else if (angle > 0){
				angle -= 0;
			}
			ship.setRotate(angle );
		}
		if (isLeftKeyPressed && isRightKeyPressed){
			if (angle < 0) {
				angle += 5;
			} else if (angle > 0){
				angle -= 0;
			}
			ship.setRotate(angle);
		}
	};

	public void createBackground(){
		Node[] gridPanes = generalUtils.createGridPanesForLayout();
		gridPane = (GridPane) gridPanes[0];
		gridPane2 = (GridPane) gridPanes[1];
		root.getChildren().addAll(gridPane, gridPane2);
	};

	public void createGameElements(){
		playerLife = 2;
		star = (ImageView) factory.createObject("star");
		root.getChildren().add(star);
		createPointsLabel();
		playerLives = (ImageView[]) factory.createObject("livesStatus");
		addElementsToPane(playerLives);
		greyMeteors = (ImageView[]) factory.createObject("meteors");
		addElementsToPane(greyMeteors);
	}

	private void addElementsToPane(Node[] node) {
		for (int i = 0; i < node.length; i++) {
			root.getChildren().add(node[i]);
		}
	}

	private void createPointsLabel() {
		customLabel = new CustomLabel("POINTS: 00");
		customLabel.setLayoutX(460);
		customLabel.setLayoutY(20);
		root.getChildren().add(customLabel);
	}

	;

	public void moveGameElement(){
		star.setLayoutY(star.getLayoutY() + 5);

		for (int i = 0; i < greyMeteors.length; i++) {
			greyMeteors[i].setLayoutY(greyMeteors[i].getLayoutY()+7);
			greyMeteors[i].setRotate(greyMeteors[i].getRotate()+4);
		}
	};

	public void relocateElementToTheTop(){
		if (star.getLayoutY()>1200){
			generalUtils.setRandomElementPosition(star);
		}

		for (int i = 0; i < greyMeteors.length; i++) {
			if (greyMeteors[i].getLayoutY() > 900){
				generalUtils.setRandomElementPosition(greyMeteors[i]);
			}
		}
	};

	public void checkIfElementCollide(){
		if (SHIP_RADIUS + STAR_RADIUS > generalUtils.calculateDistance(ship.getLayoutX() + 49, star.getLayoutX() + 15, ship.getLayoutY() + 37, star.getLayoutY() + 15)){
			generalUtils.setRandomElementPosition(star);
			points++;
			String textToSet = "POINTS : ";
			if (points < 10){
				textToSet = textToSet + "0";
			}
			customLabel.setText(textToSet + points);
		}

		for (int i = 0; i < greyMeteors.length; i++) {
			if (METEOR_RADIUS + SHIP_RADIUS > generalUtils.calculateDistance(ship.getLayoutX() + 49, greyMeteors[i].getLayoutX() + 20, ship.getLayoutY() + 37, greyMeteors[i].getLayoutY()+ 20)){
				removeLife();
				generalUtils.setRandomElementPosition(greyMeteors[i]);
			}
		}
	};

	private void removeLife(){
		root.getChildren().remove(playerLives[playerLife]);
		playerLife--;
		if (playerLife < 0){
			stage.close();
			playView.animationTimer.stop();

		};
	};

	private void createOperands(){
		group = (Group) factory.createObject("operands");
		root.getChildren().add(group);
	}

	private void createShip() {
		ship = (ImageView) factory.createObject("ship");
		root.getChildren().add(ship);
	}
}