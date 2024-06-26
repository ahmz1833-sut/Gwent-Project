package org.apgrp10.gwent.model.Massage;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apgrp10.gwent.R;
import org.apgrp10.gwent.model.User;
import org.apgrp10.gwent.view.ChatMenu;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageView extends HBox {
	private final Message message;
	private final User user;
	private VBox messageBox;
	private final HBox[] reactions = new HBox[4];
	private final HBox allReactions = new HBox();

	public MessageView(Message message, User user) {
		this.message = message;
		this.user = user;
		this.setPrefWidth(ChatMenu.width - 30);
		addImage();
		addMessage();
		addUserName();
		addText();
		addBorder();
		fillReactions();
		updateReactions();
		messageBox.getChildren().add(allReactions);
	}

	private void fillReactions() {
		for(int i = 0; i < 4; i++){
			reactions[i] = new HBox();
			ImageView image = new ImageView(R.getImage("chat/emoji" + i + ".png"));
			image.setFitHeight(14);
			image.setFitWidth(14);
			reactions[i].getChildren().add(image);
			Text text = new Text(String.valueOf(0));
			text.setFill(Color.DARKMAGENTA);
			text.setStyle("-fx-font-size: 12px");
			reactions[i].getChildren().add(text);
			reactions[i].setSpacing(3);
			reactions[i].setMaxWidth(40);
		}
	}
	private void updateReactions(){
		allReactions.getChildren().clear();
		allReactions.setSpacing(7);
		List<HBox> sorted = new java.util.ArrayList<>(Arrays.stream(reactions).sorted((o1, o2) -> {
			try {
				int count1 = Integer.parseInt(((Text) (o1.getChildren().get(1))).getText());
				int count2 = Integer.parseInt(((Text) (o2.getChildren().get(1))).getText());
				return Integer.compare(count1, count2);
			} catch (Exception ignored) {
				return 0;
			}
		}).toList());
		Collections.reverse(sorted);
		for (HBox reaction : sorted) {
			if(Integer.parseInt(((Text) (reaction.getChildren().get(1))).getText()) > 0)
				allReactions.getChildren().add(reaction);
		}
	}

	public Message getMessage(){
		return message;
	}

	private void addBorder(){
		ImageView image =new ImageView();
		image.setFitHeight(80);
		this.getChildren().add(image);
	}
	private Node getImage(){
		//TODO set avatar image of message.getOwner in here instead of sample image
		ImageView imageView = new ImageView(R.getImage("icons/card_ability_frost.png"));
		imageView.setFitWidth(30);
		imageView.setFitHeight(30);
		return imageView;
	}
	private void addImage(){
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.CENTER);
		stackPane.getChildren().add(getImage());
		this.getChildren().add(stackPane);
		this.setSpacing(5);
	}
	private void addMessage(){
		Pane pane = new Pane();
		messageBox = new VBox();
		Rectangle background = new Rectangle();
		if(!user.equals(message.getOwner()))
			background.setFill(Color.rgb(238,180, 114));
		else
			background.setFill(Color.rgb(141,227, 118));
		background.setArcWidth(20);
		background.setArcHeight(20);
		background.setWidth(160);
		StackPane stackPane = new StackPane();
		stackPane.getChildren().add(background);
		stackPane.getChildren().add(messageBox);
		background.heightProperty().bind(stackPane.heightProperty());
		pane.getChildren().add(stackPane);
		messageBox.setStyle("-fx-padding: 5 5 5 5;");
		this.getChildren().add(pane);
	}
	private void addUserName(){
		Text username;
		if(user.equals(message.getOwner())) {
			username = new Text("YOU:");
			username.setFill(Color.RED);

		}
		else {
			username = new Text(message.getOwner().getUsername() + ":");
			username.setFill(Color.GREEN);
		}
		username.setWrappingWidth(150);
		username.setStyle("-fx-font-size: 14px");
		messageBox.getChildren().add(username);
	}
	private void addText(){
		Text text = new Text(message.getText() + "\n");
		text.setWrappingWidth(150);
		text.setFill(Color.BLACK);
		text.setStyle("-fx-font-size: 12px");
		messageBox.getChildren().add(text);
	}
	public void increaseReaction(int index){
		int count = Integer.parseInt(((Text) (reactions[index].getChildren().get(1))).getText());
		count++;
		((Text) (reactions[index].getChildren().get(1))).setText(String.valueOf(count));
		updateReactions();
	}
	public void decreaseReaction(int index){
		int count = Integer.parseInt(((Text) (reactions[index].getChildren().get(1))).getText());
		count--;
		if(count >= 0)
			((Text) (reactions[index].getChildren().get(1))).setText(String.valueOf(count));
		updateReactions();
	}
}
