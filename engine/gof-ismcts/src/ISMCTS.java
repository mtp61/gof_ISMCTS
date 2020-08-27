import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class ISMCTS {
	public static void main(String args[]) throws Exception {
		// default to testing values
		long max_itr = 10000;
		long max_time = 5;
		int[] player1_cards = {11,12,32,40,41,41,50,52,60,61,62,90,90,91};
		int[] card_counts = {14,12,16,14};
		int[] cards_played = {10,11,31,32,100,100,102,102};
		int[] current_hand = { }; //{};
		int num_passes = 2;  // 0
		int player_to_act = 1;
		if (args.length != 0) {  // load test vars
			max_itr = Long.valueOf(args[0]);
			max_time = Long.valueOf(args[1]);
			
			if (args[2].length() > 2) {
				String[] player1_cards_s = (args[2].substring(1, args[2].length() - 1).split(","));
				player1_cards = new int[player1_cards_s.length];
				for (int i = 0; i < player1_cards_s.length; i++) {
					player1_cards[i] = Integer.valueOf(player1_cards_s[i]);
				}
			} else {
				player1_cards = new int[0];
			}
			
			if (args[3].length() > 2) {
				String[] card_counts_s = (args[3].substring(1, args[3].length() - 1).split(","));
				card_counts = new int[card_counts_s.length];
				for (int i = 0; i < card_counts_s.length; i++) {
					card_counts[i] = Integer.valueOf(card_counts_s[i]);
				}
			} else {
				card_counts = new int[0];
			}
			
			if (args[4].length() > 2) {
				String[] cards_played_s = (args[4].substring(1, args[4].length() - 1).split(","));
				cards_played = new int[cards_played_s.length];
				for (int i = 0; i < cards_played_s.length; i++) {
					cards_played[i] = Integer.valueOf(cards_played_s[i]);
				}
			} else {
				cards_played = new int[0];
			}
			
			if (args[5].length() > 2) {
				String[] current_hand_s = (args[5].substring(1, args[5].length() - 1).split(","));
				current_hand = new int[current_hand_s.length];
				for (int i = 0; i < current_hand_s.length; i++) {
					current_hand[i] = Integer.valueOf(current_hand_s[i]);
				}
			} else {
				current_hand = new int[0];
			}
			
			num_passes = Integer.valueOf(args[6]);
			player_to_act = Integer.valueOf(args[7]);;
		}
		// todo change
		player_to_act = 1;
		
		Arrays.sort(player1_cards);
		
		// make sure player cards size is same as card count
		if (player1_cards.length != card_counts[0]) {
			throw new Exception("Incorrect number of player cards");
		}
		
		// make sure played cards is the correct length
		if (64 - cards_played.length != card_counts[0] + card_counts[1] + card_counts[2] + card_counts[3]) {
			throw new Exception("Incorrect number of cards played");
		}
		
		int current_hand_score = Shared.getScore(current_hand);

		// make hand Indices
		HandIndices handIndices = new HandIndices();
		
		// make root node
		Node root = new Node(handIndices, player1_cards, card_counts, cards_played, current_hand, new int[0], num_passes, player_to_act);

		// make nodes for each possible player1 action		
		// get possible actions
		// this code is straight from randomD, can probably do a lot better... thought it is only run once so it shouldn't matter much
		LinkedList<Hand>[] actions = new LinkedList[5];
		
		int[] possible_play_cards;  // array of the possible number of cards
		
		// check if we need to play highest single
		if (current_hand.length == 1 && card_counts[Shared.nextPlayer(player_to_act)] == 1) {
			// get highest single
			int highest_single = player1_cards[player1_cards.length - 1];
			if (highest_single > current_hand[0]) {
				// add highest single node and gangs
				actions[0].add(new Hand(new int[] { highest_single }, 1));
				
				possible_play_cards = new int[] { 4 };

			} else {
				// add passing node and gangs
				if (num_passes < 3) {
					root.getChildren().add(new Node(handIndices, player1_cards, card_counts, cards_played, current_hand, new int[0], num_passes + 1, Shared.nextPlayer(player_to_act)));
				} else {
					root.getChildren().add(new Node(handIndices, player1_cards, card_counts, cards_played, new int[0], new int[0], 0, Shared.nextPlayer(player_to_act)));
				}
				
				possible_play_cards = new int[] { 4 };
			}
		} else if (current_hand.length == 0 && card_counts[Shared.nextPlayer(player_to_act)] == 1) {
			int highest_single = player1_cards[player1_cards.length - 1];
			if (highest_single > current_hand[0]) {
				// add highest single and non singles
				actions[0].add(new Hand(new int[] { highest_single }, 1));

				possible_play_cards = new int[] { 2, 3, 4, 5 };
			} else {
				// add passing node and non-singles
				if (num_passes < 3) {
					root.getChildren().add(new Node(handIndices, player1_cards, card_counts, cards_played, current_hand, new int[0], num_passes + 1, Shared.nextPlayer(player_to_act)));
				} else {
					root.getChildren().add(new Node(handIndices, player1_cards, card_counts, cards_played, new int[0], new int[0], 0, Shared.nextPlayer(player_to_act)));
				}
				
				possible_play_cards = new int[] { 2, 3, 4, 5 };
			}
		} else {
			// add passing node
			if (num_passes < 3) {
				root.getChildren().add(new Node(handIndices, player1_cards, card_counts, cards_played, current_hand, new int[0], num_passes + 1, Shared.nextPlayer(player_to_act)));
			} else {
				root.getChildren().add(new Node(handIndices, player1_cards, card_counts, cards_played, new int[0], new int[0], 0, Shared.nextPlayer(player_to_act)));
			}
			
			// add other nodes
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
		}
		
		// initialize the required actions
		for (int i = 0; i < possible_play_cards.length; i++) {
			if (actions[possible_play_cards[i] - 1] == null) {
				// need to initialize
				Shared.initializeActions(handIndices, actions, player_to_act, possible_play_cards[i], player1_cards);
			}
		}
		
		// for each possible number of cards for each possible action
		for (int i = 0; i < possible_play_cards.length; i++) {
			for (int j = 0; j < actions[possible_play_cards[i] - 1].size(); j++) {
				Hand action = actions[possible_play_cards[i] - 1].get(j);
				
				// if the score is high enough add a new node
				if (action.getScore(player1_cards) > current_hand_score) {
					// make temp arrays for the creation of the node
					int[] card_list = new int[action.num_cards];
					for (int k = 0; k < action.num_cards; k++) {
						card_list[k] = player1_cards[action.hand[k]];
					}
					
					int[] p1c_t = new int[player1_cards.length];
					for (int k = 0; k < player1_cards.length; k++) {
						p1c_t[k] = player1_cards[k];
					}
					for (int k = 0; k < action.num_cards; k++) {
						p1c_t[action.hand[k]] = 0;
					}
					
					int[] cc_t = new int[card_counts.length];
					for (int k = 0; k < card_counts.length; k++) {
						cc_t[k] = card_counts[k];
					}
					cc_t[0] -= action.num_cards;
					
					int[] cp_t = new int[cards_played.length + action.num_cards];
					for (int k = 0; k < cards_played.length; k++) {
						cp_t[k] = cards_played[k];
					}
					for (int k = 0; k < action.num_cards; k++) {
						cp_t[k + cards_played.length] = player1_cards[action.hand[k]];
					}
					
					root.getChildren().add(new Node(handIndices, p1c_t, cc_t, cp_t, card_list, card_list, 0, Shared.nextPlayer(player_to_act)));
				}
			}
		}
		
		/*// 1 card only testing
		for (int i = 0; i < player1_cards.length; i++) {
			int card = player1_cards[i];
			int[] card_list = {card};
			if (Shared.getScore(card_list) > current_hand_score) {
				// add a new node
				int[] p1c_t = new int[player1_cards.length];
				for (int j = 0; j < player1_cards.length; j++) {
					p1c_t[j] = player1_cards[j];
				}
				p1c_t[i] = 0;
				
				int[] cp_t = new int[cards_played.length + 1];
				for (int j = 0; j < cards_played.length; j++) {
					cp_t[j] = cards_played[j];
				}
				cp_t[cards_played.length] = card;
				
				int[] cc_t = new int[card_counts.length];
				for (int j = 0; j < card_counts.length; j++) {
					cc_t[j] = card_counts[j];
				}
				cc_t[0]--;
				
				root.getChildren().add(new Node(handIndices, p1c_t, cc_t, cp_t, card_list, card_list, 0, Shared.nextPlayer(player_to_act)));
			}
		}*/
		
		// do until max itr or max time
		long itr = 0;
		long start_time = System.currentTimeMillis();
		while ((max_itr == -1 || itr < max_itr) && (max_time == -1 || System.currentTimeMillis() - start_time < max_time * 1000)) {
			// select an action node
			double max_score = -1;
			Node max_child = null; 
			for (int i = 0; i < root.getChildren().size(); i++) {
				Node child = root.getChildren().get(i);
				double child_score = child.getUCB(); 
				
				if (child_score == Double.MAX_VALUE) {
					max_child = child;
					break;
				} else if (child_score > max_score) {
					max_child = child;
					max_score = child_score;
				}
			}
			Node action_node = max_child;
			
			// choose a random determinization from the action node
			Determinization d = action_node.randomD();
			
			// simulate game
			int[] reward = d.simulateGame();
			
			// update action node 
			action_node.updateReward(reward);
			action_node.incrementVisits();
			for (int i = 0; i < root.getChildren().size(); i++) {
				root.getChildren().get(i).incrementAvailability();
			}
			
			itr++;
		}
		
		// return best action
		int most_visits = -1;
		int[] most_visited_action = null;
		for (int i = 0; i < root.getChildren().size(); i++) {
			Node child = root.getChildren().get(i);	
			int node_visits = child.getVisitCount();
			if (node_visits > most_visits) {
				most_visited_action = child.getLastAction();
				most_visits = node_visits;
			}
		}
		
		
		// printing
		LinkedList<PrintObj> print_objs = new LinkedList<>();
		for (int i = 0; i < root.getChildren().size(); i++) {  // add print objs to list
			Node child = root.getChildren().get(i);
			int[] hand = child.getLastAction();
			int win_percentage = (int) (100 * ((double) root.getChildren().get(i).getTotalReward()[0] / root.getChildren().get(i).getVisitCount()));
			
			print_objs.add(new PrintObj(hand, win_percentage));
		}
		
		// sort list
		Collections.sort(print_objs, new Comparator<PrintObj>() {
			@Override
			public int compare(PrintObj p1, PrintObj p2) {
				return p1.win_percentage - p2.win_percentage;
			}
		});
		
		// print list complete
		System.out.println(root.getChildren().size() + " options for root player with " + itr + " games simulated");
		for (int i = 0; i < print_objs.size(); i++) {			
			PrintObj print_obj = print_objs.get(i);
			if (print_obj.win_percentage < 10) {
				System.out.print("   " + print_obj.win_percentage + " % - ");
			} else {
				System.out.print("  " + print_obj.win_percentage + " % - ");
			}
			Shared.printArray(print_obj.hand);
		}
		System.out.println();
		
		// print list small
		// todo
		
		// print action
		System.out.print("action ");
		String action_string = "";
		if (most_visited_action.length == 0) {  // pass
			System.out.print("0");
		} else {
			for (int i = 0; i < most_visited_action.length; i++) {
				action_string += String.valueOf(most_visited_action[i]) + " ";
			}
			System.out.print(action_string.substring(0, action_string.length() - 1));
		}
		System.out.println();
	}
}
