import java.util.Arrays;
import java.util.LinkedList;

public class Node {
	private HandIndices handIndices;
	
	private int[] player1_cards;
	private int[] card_counts;
	private int[] cards_played;
	private int[] current_hand;
	private int[] last_action;
	private int num_passes;
	private int player_to_act;
	
	private int[] cards_to_play;
	
	private int visit_count = 0;
	private int availability_count = 0;
	private int[] total_reward = { 0, 0, 0, 0 };
	private LinkedList<Node> children;
	
	public Node(HandIndices handIndices, int[] player1_cards, int[] card_counts, int[] cards_played, int[] current_hand, int[] last_action, int num_passes, int player_to_act) {
		this.handIndices = handIndices;
		
		this.player1_cards = player1_cards;
		this.card_counts = card_counts;
		this.cards_played = cards_played;
		this.current_hand = current_hand;
		this.last_action = last_action;
		this.num_passes = num_passes;
		this.player_to_act = player_to_act;
		
		// make array of cards to play
		// this is a bit inefficient but it doesnt matter much, can use a hashset if need more speed
		this.cards_to_play = new int[card_counts[1] + card_counts[2] + card_counts[3]];
		int counter = 0;
		for (int i = 0; i < 34; i++) {			
			int card = Shared.unique_cards[i];
			int card_count = 0;
			
			// check cards played
			for (int j = 0; j < cards_played.length; j++) {
				if (cards_played[j] == card) {
					card_count++;
				}
			}
			
			// check player1 cards
			for (int j = 0; j < player1_cards.length; j++) {
				if (player1_cards[j] == card) {
					card_count++;
				}
			}
						
			if (card % 10 == 3 || card >= 110) {  // if there should only be one card
				if (card_count == 0) {  // add the card
					this.cards_to_play[counter] = card;
					counter++;
				}
			} else {
				if (card_count == 0) {  // add 2 cards
					this.cards_to_play[counter] = card;
					this.cards_to_play[counter + 1] = card;					
					counter += 2;
				} else if (card_count == 1) {  // add 1 card
					this.cards_to_play[counter] = card;					
					counter++;
				}
			}
		}
		
		this.children = new LinkedList<>();
	}
	
	public Determinization randomD() {
		// shuffle cards to play
		Shared.shuffleArray(this.cards_to_play);
		
		int[][] player_cards = new int[4][];

		// add player1 cards to player cards
		player_cards[0] = new int[card_counts[0]];
		int counter = 0;
		for (int i = 0; i < card_counts[0]; i++) {
			int card = this.player1_cards[i];
			if (card != 0) {
				player_cards[0][i] = card;
				counter++;
			}
		}
		
		// randomly assign player cards
		counter = 0;
		for (int i = 1; i < 4; i++) {
			int num_player_cards = card_counts[i];
			player_cards[i] = new int[num_player_cards];
			
			for (int j = 0; j < num_player_cards; j++) {
				player_cards[i][j] = this.cards_to_play[counter];
				counter++;
			}
		}
		
		for (int i = 0; i < 4; i++) {
			Arrays.sort(player_cards[i]);
		}
		
		/*for (int i = 0; i < 4; i++) {
			for (int j = 0; j < player_cards[i].length; j++) {
				System.out.print(player_cards[i][j] + " ");
			}
			System.out.println();
		}*/
		
		return new Determinization(this.handIndices, player_cards, this.current_hand, this.num_passes, this.player_to_act);
	}

	public void updateReward(int[] reward) {
		for (int i = 0; i < 4; i++) {
			this.total_reward[i] += reward[i];
		}
	}

	public void incrementVisits() {
		this.visit_count++;
	}
	
	public void incrementAvailability() {
		this.availability_count++;
	}
	
	public LinkedList<Node> getChildren() {
		return this.children;
	}
	
	public int[] getCurrentHand() {
		return this.current_hand;
	}
	
	public int getVisitCount() {
		return this.visit_count;
	}
	
	public double getUCB() {
		if (this.visit_count == 0) {
			return Double.MAX_VALUE;
		}
		
		double exploit_term = (double) this.total_reward[Shared.lastPlayer(this.player_to_act) - 1] / this.visit_count;
		double explore_term = Shared.UCB_k * Math.sqrt(Math.log(this.availability_count) / this.visit_count);
		
		return exploit_term + explore_term;
	}

	public int[] getTotalReward() {
		return this.total_reward;
	}
	
	public int[] getLastAction() {
		return this.last_action;
	}
}
