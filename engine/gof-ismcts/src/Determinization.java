import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Determinization {
	private int[][] player_cards;
	private int[] current_hand;
	private int num_passes;
	private int player_to_act;
	private HandIndices handIndices;
	
	public Determinization(HandIndices handIndices, int[][] player_cards, int[] current_hand, int num_passes, int player_to_act) {
		this.handIndices = handIndices;
		
		this.player_cards = player_cards;
		this.current_hand = current_hand;
		this.num_passes = num_passes;
		this.player_to_act = player_to_act;
		
		// sort player cards
		for (int i = 0; i < 4; i++) {
			Arrays.sort(this.player_cards[i]);
		}	
	}
	
	public int[] simulateGame() {  // returns the reward vector after simulating a game		
		int player_to_act = this.player_to_act;
		int num_passes = this.num_passes;
		
		int[] current_hand = new int[this.current_hand.length];
		for (int i = 0; i < this.current_hand.length; i++) {
			current_hand[i] = this.current_hand[i];
		}
		
		int current_hand_score = Shared.getScore(current_hand);
						
		int[][] player_cards = new int[4][16];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 16; j++) {
				if (j < this.player_cards[i].length) {
					player_cards[i][j] = this.player_cards[i][j];
				} else {
					player_cards[i][j] = 0;
				}
			}
		}
		
		LinkedList<Hand>[][] actions = new LinkedList[4][5];
		
		Random rand = new Random();
		
		/*// printing
		System.out.print("running sim with current hand - ");
		Shared.printArray(current_hand);
		System.out.println("player cards");
		for (int i = 0; i < 4; i++) {
			Shared.printArray(player_cards[i]);
		}
		System.out.println();*/
		
		// main loop
		while (hasCards(player_cards[Shared.lastPlayer(player_to_act) - 1])) {		
			// address edge cases
			// 3 passes
			if (num_passes == 3) {
				current_hand = new int[0];
				num_passes = 0; // todo reset num passes when doing a non passing action
			}
			
			// next player only has 1 card
			// todo
			
			int[] possible_play_cards;  // array of the possible number of cards
			switch (current_hand.length) {
			case 0:
				possible_play_cards = new int[] { 1, 2, 3, 4, 5 };
				break;
			case 4:
				possible_play_cards = new int[] { 4 };
				break;
			default:
				possible_play_cards = new int[] { current_hand.length, 4 };
			}
			
			// see if actions are defined, update if needed
			for (int i = 0; i < possible_play_cards.length; i++) {
				if (actions[player_to_act - 1][possible_play_cards[i] - 1] == null) {
					// need to initialize
					Shared.initializeActions(handIndices, actions[player_to_act - 1], player_to_act, possible_play_cards[i], player_cards[player_to_act - 1]);
				}
			}
			
			LinkedList<Hand> candidate_actions = new LinkedList<>();
			candidate_actions.add(new Hand(new int[0]));  // add passing hand
			
			// for each possible number of cards for each possible action
			for (int i = 0; i < possible_play_cards.length; i++) {
				LinkedList<Integer> to_remove_indices = new LinkedList<>();
				
				for (int j = 0; j < actions[player_to_act - 1][possible_play_cards[i] - 1].size(); j++) {
					Hand action = actions[player_to_act - 1][possible_play_cards[i] - 1].get(j);
					
					// if the action is not possible because of missing cards remove it
					if (!this.actionPossible(action.hand, player_cards[player_to_act - 1])) {
						to_remove_indices.add(j);
						continue;
					}
					
					// if the score of the action is high enough add it to the list of possible actions
					if (action.getScore(player_cards[player_to_act - 1]) > current_hand_score) {
						candidate_actions.add(action);
					}
				}
				
				// remove actions that wernt possible
				for (int j = to_remove_indices.size() - 1; j >= 0; j--) {					
					actions[player_to_act - 1][possible_play_cards[i] - 1].remove(to_remove_indices.get(j));
				}
			}
			
			// select random action
			Hand random_action = candidate_actions.get(rand.nextInt(candidate_actions.size()));
			
			// update game state variables
			// update current hand
			current_hand = new int[random_action.num_cards];
			for (int i = 0; i < random_action.num_cards; i++) {
				current_hand[i] = player_cards[player_to_act - 1][random_action.hand[i]];
			}
			current_hand_score = Shared.getScore(current_hand);
			
			// remove cards from hand
			for (int i = 0; i < random_action.num_cards; i++) {
				player_cards[player_to_act - 1][random_action.hand[i]] = 0;
			}
			
			// update player to act
			player_to_act = Shared.nextPlayer(player_to_act);
			
			// update num passes
			if (random_action.num_cards == 0) {
				num_passes++;
			} else {
				num_passes = 0;
			}
			
			
			
			/*// printing
			System.out.println("player had " + candidate_actions.size() + " possible actions");
			for (int i = 0; i < candidate_actions.size(); i++) {
				Shared.printArray(candidate_actions.get(i).hand);
			}
			System.out.println();
			
			System.out.print("new sim round, player acted " + Shared.lastPlayer(player_to_act) + ", current action ");
			Shared.printArray(random_action.hand);
			System.out.print("current hand ");
			Shared.printArray(current_hand);
			System.out.println("player cards");
			for (int i = 0; i < 4; i++) {
				Shared.printArray(player_cards[i]);
			}
			System.out.println();*/
		}	
		
		// return reward vector
		int[] remaining_card_count = new int[4];
		for (int i = 0; i < 4; i++) {
			int player_card_count = 0;
			for (int j = 0; j < 16; j++) {
				if (player_cards[i][j] != 0) {
					player_card_count++;
				}
			}
			remaining_card_count[i] = player_card_count;
		}
		return Shared.calcReward(remaining_card_count);
	}
	
	private boolean actionPossible(int[] indices, int[] player_hand) {
		for (int i = 0; i < indices.length; i++) {
			if (player_hand[indices[i]] == 0) {
				return false;
			}
		}
		return true;
	}
	
	private boolean hasCards(int[] hand) {
		for (int i = 0; i < hand.length; i++) {
			if (hand[i] != 0) {
				return true;
			}
		}
		return false;
	}
}
