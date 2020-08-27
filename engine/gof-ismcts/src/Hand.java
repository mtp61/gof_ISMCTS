
public class Hand {
	public int[] hand;  // note that these are indices not 
	public int num_cards;
	public int score;
	
	public Hand(int[] hand) {
		this.hand = hand;
		this.score = -1;

		// set num cards
		int card_count = 0;
		for (int i = 0; i < hand.length; i++) {
			if (hand[i] != 0) {
				card_count++;
			}
		}
		this.num_cards = card_count;
	}
	
	public Hand(int[] hand, int num_cards) {
		this.hand = hand;
		this.num_cards = num_cards;
		this.score = -1;
	}
	
	public Hand(int[] hand, int num_cards, int score) {
		this.hand = hand;
		this.num_cards = num_cards;
		this.score = score;
	}
	
	public int getScore(int[] player_hand) {
		if (this.score == -1) {  // if havent generated score
			int[] hand_cards = new int[this.num_cards];
			for (int i = 0; i < this.num_cards; i++) {
				hand_cards[i] = player_hand[this.hand[i]];
			}
			
			this.score = Shared.getScore(hand_cards);			
		}
		
		return this.score;
	}
}
