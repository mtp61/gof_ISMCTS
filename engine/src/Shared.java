import java.util.LinkedList;
import java.util.Random;

public class Shared {
	public static double UCB_k = Math.sqrt(2);  // k value in ucb1 alg
	
	// all the cards in the deck
	public static int[] cards = {
		10, 10, 11, 11, 12, 12, 13,
		20, 20, 21, 21, 22, 22,
		30, 30, 31, 31, 32, 32,
		40, 40, 41, 41, 42, 42,
		50, 50, 51, 51, 52, 52,
		60, 60, 61, 61, 62, 62,
		70, 70, 71, 71, 72, 72,
		80, 80, 81, 81, 82, 82,
		90, 90, 91, 91, 92, 92,
		100, 100, 101, 101, 102, 102,
		110, 111, 112
	};
	
	public static int[] unique_cards = {
		10, 11, 12, 13,
		20, 21, 22,
		30, 31, 32,
		40, 41, 42,
		50, 51, 52,
		60, 61, 62,
		70, 71, 72,
		80, 81, 82,
		90, 91, 92,
		100, 101, 102,
		110, 111, 112
	};
	
	// Implementing Fisher–Yates shuffle
	public static void shuffleArray(int[] ar)
	{
	    Random rand = new Random();
	    for (int i = ar.length - 1; i > 0; i--) {
		      int index = rand.nextInt(i + 1);
		      // Simple swap
		      int a = ar[index];
		      ar[index] = ar[i];
		      ar[i] = a;
	    }
	}
	
	// return the reward array from the array of cards remaining
	public static int[] calcReward(int[] remaining_cards) {
		int[] reward_array = new int[4];
		
		for (int i = 0; i < 4; i++) {
			if (remaining_cards[i] == 0) {
				reward_array[i] = 1;
			} else {
				reward_array[i] = 0;
			}
		}
		
		return reward_array;
	}
	
	public static LinkedList<Hand> getActions(HandIndices handIndices, int[] player_cards, int num_cards) {
		// assumes the cards are sorted
		LinkedList<Hand> actions = new LinkedList<>();
		
		if (player_cards.length < 16) {
			int[] player_cards_n = new int[16];
			for (int i = 0; i < 16; i++) {
				if (i < player_cards.length) {
					player_cards_n[i] = player_cards[i];
				} else {
					player_cards_n[i] = 0;
				}
			}
			player_cards = player_cards_n;
		}
		
		switch (num_cards) {
			case 1:
				for (int i = 0; i < handIndices.i1.length; i++) {
					int card = handIndices.i1[i][0];
					// if its a legal hand add it
					if (player_cards[card] != 0) {
						actions.add(new Hand(handIndices.i1[i], 1));
					}
				}
				break;
			case 2:
				for (int i = 0; i < handIndices.i2.length; i++) {
					// make cards array
					int[] cards = new int[2];
					for (int j = 0; j < 2; j++) {
						cards[j] = player_cards[handIndices.i2[i][j]];
					}
					
					// if its a legal hand add it
					if (cards[0] != 0 && cards[1] != 0
						&& Shared.getValue(cards[0]) == Shared.getValue(cards[1])
						&& cards[0] != 112 && cards[1] != 112) {
						actions.add(new Hand(handIndices.i2[i], 2));
					}
				}
				break;
			case 3:
				for (int i = 0; i < handIndices.i3.length; i++) {
					// make cards array
					int[] cards = new int[3];
					for (int j = 0; j < 3; j++) {
						cards[j] = player_cards[handIndices.i3[i][j]];
					}
					
					// if its a legal hand add it
					if (cards[0] != 0 && cards[1] != 0 && cards[2] != 0
						&& Shared.getValue(cards[0]) == Shared.getValue(cards[1]) 
						&& Shared.getValue(cards[0]) == Shared.getValue(cards[2])
						&& Shared.getValue(cards[0]) != 11) {
						actions.add(new Hand(handIndices.i3[i], 3));
					}
				}
				break;
			case 4:
				for (int i = 0; i < handIndices.i4.length; i++) {
					// make cards array
					int[] cards = new int[4];
					for (int j = 0; j < 4; j++) {
						cards[j] = player_cards[handIndices.i4[i][j]];
					}
					
					// if its a legal hand add it
					if (cards[0] != 0 && cards[1] != 0 && cards[2] != 0 && cards[3] != 0
						&& Shared.getValue(cards[0]) == Shared.getValue(cards[1]) 
						&& Shared.getValue(cards[0]) == Shared.getValue(cards[2])
						&& Shared.getValue(cards[0]) == Shared.getValue(cards[3])) {
						actions.add(new Hand(handIndices.i4[i], 4));
					}
				}
				break;
			case 5:
				for (int i = 0; i < handIndices.i5.length; i++) {
					// make cards array and check for zeros
					boolean should_continue = false;
					int[] cards = new int[5];
					for (int j = 0; j < 5; j++) {
						cards[j] = player_cards[handIndices.i5[i][j]];
						if (cards[j] == 0) {
							should_continue = true;
							break;
						}
					}
					if (should_continue) {
						continue;
					}
					
					// straight
					boolean is_straight = true;
					for (int j = 1; j < 5; j++) {
						if (Shared.getValue(cards[j]) != Shared.getValue(cards[0]) + j) {
							is_straight = false;
							break;
						}
					}
					if (is_straight && Shared.getValue(cards[4]) != 11) {
						actions.add(new Hand(handIndices.i5[i], 5));
						continue;
					}
					
					// flush
					int hand_color = Shared.getColor(cards[4]);
					boolean is_flush = true;
					for (int j = 0; j < 4; j++) {
						int card_color = Shared.getColor(cards[j]);
						if (card_color != hand_color && card_color != 3) {
							is_flush = false;
							break;
						}
					}
					if (is_flush) {
						actions.add(new Hand(handIndices.i5[i], 5));
						continue;
					}
					
					// full house
					if (Shared.getValue(cards[0]) == Shared.getValue(cards[1])
						&& Shared.getValue(cards[3]) == Shared.getValue(cards[4])
						&& (Shared.getValue(cards[1]) == Shared.getValue(cards[2])
						|| Shared.getValue(cards[2]) == Shared.getValue(cards[3]))
						&& cards[3] != 112 && cards[4] != 112) {
						actions.add(new Hand(handIndices.i5[i], 5));
						continue;
					}
				}
				break;
		}
		
		return actions;
	}
	
	public static int getValue(int card) {
		return card / 10;
	}
	
	public static int getColor(int card) {
		return card % 10;
	}
	
	public static int getScore(int[] hand) {
		// assumes we have a legal hand
		switch (hand.length) {
			case 0:
				return 0;
			case 1:
				return hand[0];
			case 2:				
				return 100 * Shared.getValue(hand[1]) + 10 * Shared.getColor(hand[1]) + Shared.getColor(hand[0]);
			case 3:
				return 1000 * Shared.getValue(hand[2]) + 100 * Shared.getColor(hand[2]) + 10 * Shared.getColor(hand[1]) + Shared.getColor(hand[0]);
			case 4:
				return Shared.getValue(hand[0]) + 100000000;
			case 5:
				// if straight
				boolean is_straight = true;
				for (int i = 1; i < 5; i++) {
					if (Shared.getValue(hand[i]) != Shared.getValue(hand[0]) + i) {
						is_straight = false;
						break;
					}
				}
				
				// if flush
				boolean is_flush = true;
				int hand_color = Shared.getColor(hand[4]);
				for (int i = 0; i < 4; i++) {
					int card_color = Shared.getColor(hand[i]);
					if (card_color != 3 && card_color != hand_color) {
						is_flush = false;
						break;
					}
				}
				
				int score;
				int ten_power;
				if (is_straight && is_flush) {  // straight flush
					return hand[4] + 90000000;				
				} else if (is_straight) {  // straight
					score = 100000 * Shared.getValue(hand[4]);
					ten_power = 10000;
					for (int j = 4; j >= 0; j--) {
						score += ten_power * Shared.getColor(hand[j]);
						ten_power /= 10;
					}
					return score;
				} else if (is_flush) {  // flush
					score = 100000 * Shared.getColor(hand[4]) + 2000000;
					ten_power = 10000;
					for (int j = 4; j >= 0; j--) {
						score += ten_power * Shared.getValue(hand[j]);
						ten_power /= 10;
					}
					return score;
				}
				
				// must be a full house
				if (Shared.getValue(hand[1]) == Shared.getValue(hand[2])) {  // 3 is first 3
					score = 1000000 * Shared.getValue(hand[0]) + 100000 * Shared.getValue(hand[4]) + 3000000;
				} else {  // 3 is last 3
					score = 1000000 * Shared.getValue(hand[4]) + 100000 * Shared.getValue(hand[0]) + 3000000;
				}
				ten_power = 10000;
				for (int j = 4; j >= 0; j--) {
					score += ten_power * Shared.getColor(hand[j]);
					ten_power /= 10;
				}
				return score;

		}
		
		return -1;
	}
	
	public static void initializeActions(HandIndices handIndices, LinkedList<Hand>[] player_actions, int player_to_act, int num_cards, int[] player_cards) {
		player_actions[num_cards - 1] = Shared.getActions(handIndices, player_cards, num_cards);
	}
	
	public static void printArray(int[] a) {
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i] + " ");
		}
		System.out.println();
	}
	
	public static int nextPlayer(int current_player) {
		if (current_player != 4) {
			return current_player + 1;
		}
		return 1;
	}
	
	public static int lastPlayer(int current_player) {
		if (current_player != 1) {
			return current_player - 1;
		}
		return 4;
	}
}
