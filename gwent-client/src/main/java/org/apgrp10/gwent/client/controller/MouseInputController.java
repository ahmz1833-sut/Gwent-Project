package org.apgrp10.gwent.client.controller;

import org.apgrp10.gwent.controller.GameController;
import org.apgrp10.gwent.controller.InputController;
import org.apgrp10.gwent.model.Command;
import org.apgrp10.gwent.model.card.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MouseInputController implements InputController {
	private final List<Object> listeners = new ArrayList<>();
	private GameController controller;
	private int player;

	@Override
	public void start(GameController controller, int player) {
		this.controller = controller;
		this.player = player;
	}

	private void addListeners() {
		listeners.add(controller.getGameMenu().addButtonListener(this::buttonAction));
		listeners.add(controller.getGameMenu().addCardListener(this::cardAction));
		listeners.add(controller.getGameMenu().addRowListener(this::rowAction));
		listeners.add(controller.getGameMenu().addBgListener(this::bgAction));
	}

	private void removeListeners() {
		for (Object listener : listeners)
			controller.getGameMenu().removeListener(listener);
		listeners.clear();
	}

	@Override
	public void beginTurn() {
		controller.setActivePlayer(player);
		addListeners();
	}

	@Override
	public void endTurn() {removeListeners();}

	@Override
	public void pauseTurn() {removeListeners();}

	@Override
	public void resumeTurn() {addListeners();}

	@Override
	public void endGame() {}

	@Override
	public void veto() {
		// TODO: this function is pure shit! maybe do something better?
		// also note that if it's a local game player 2 needs to wait for player 1 before choosing

		Consumer<Card> pickCb = card1 -> {
			if (card1 == null) {
				controller.waitExec.run(1000, () -> {
					controller.sendCommand(new Command.VetoCard(player, -1));
					controller.sendCommand(new Command.Sync(player));
				});
				return;
			}
			controller.sendCommand(new Command.VetoCard(player, card1.getGameId()));
			controller.sendCommand(new Command.Sync(player));

			controller.getGameMenu().pickCard(controller.getPlayer(player).handCards, card2 -> {
				if (card2 != null) {
					controller.sendCommand(new Command.VetoCard(player, card2.getGameId()));
					controller.sendCommand(new Command.Sync(player));
				}

				controller.waitExec.run(1000, () -> {
					controller.sendCommand(new Command.VetoCard(player, -1));
					controller.sendCommand(new Command.Sync(player));
				});
			}, true);
		};

		if (player == 1 && controller.getPlayer(0).controller instanceof MouseInputController) {
			controller.addCommandListener(new Consumer<Command>() {
				@Override
				public void accept(Command cmd) {
					if (!controller.getPlayer(0).vetoDone)
						return;
					controller.removeCommandListener(this);
					controller.setActivePlayer(player);
					controller.getGameMenu().pickCard(controller.getPlayer(player).handCards, pickCb, true);
				}
			});
			return;
		}

		controller.setActivePlayer(player);
		controller.getGameMenu().pickCard(controller.getPlayer(player).handCards, pickCb, true);
	}

	@Override
	public void pick(List<Card> list, String what) {
		boolean nullPossible = what.equals("view_enemy_hand") || what.equals("cheat_enemy_hand");
		controller.getGameMenu().pickCard(list, card -> {
			controller.sendCommand(new Command.PickResponse(player, card != null ? card.getGameId() : -1, what));
			controller.sendCommand(new Command.Sync(player));
		}, nullPossible);
	}

	private void bgAction(Object obj) {
		controller.sendCommand(new Command.SetActiveCard(player, -1));
		controller.sendCommand(new Command.Sync(player));
	}

	private void rowAction(Object obj) {
		// when we get here the card can actually be placed there so no need to check
		int row = (int) obj;
		Card card = controller.getActiveCard();
		controller.sendCommand(new Command.PlayCard(player, card.getGameId(), row));
		controller.sendCommand(new Command.SetActiveCard(player, -1));
		controller.sendCommand(new Command.Sync(player));
	}

	private void cardAction(Object obj) {
		Card card = (Card) obj;
		GameController.PlayerData data = controller.getPlayer(player);
		Card active = controller.getActiveCard();

		if (data.deck.getLeader() == card && !data.leaderUsed) {
			controller.sendCommand(new Command.PlayLeader(player));
			controller.sendCommand(new Command.SetActiveCard(player, -1));
			controller.sendCommand(new Command.Sync(player));
			return;
		}

		if (active != null && controller.canSwap(player, active, card)) {
			controller.sendCommand(new Command.SwapCard(player, active.getGameId(), card.getGameId()));
			controller.sendCommand(new Command.SetActiveCard(player, -1));
			controller.sendCommand(new Command.Sync(player));
			return;
		}

		if (!data.handCards.contains(card))
			return;

		if (active != card) {
			controller.sendCommand(new Command.SetActiveCard(player, card.getGameId()));
			controller.sendCommand(new Command.Sync(player));
			return;
		}
	}

	private void buttonAction(Object obj) {
		String str = (String) obj;
		if (str.equals("pass"))
			controller.sendCommand(new Command.Pass(player));
		if (str.startsWith("cheat_"))
			controller.sendCommand(new Command.Cheat(player, Integer.parseInt(str.substring(6))));
		controller.sendCommand(new Command.Sync(player));
	}
}
