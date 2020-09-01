import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class ISMCTS {
	public static void main(String args[]) throws Exception {
		// initialize handindices and shared
		new HandIndices();
		new Shared();
		
		// default to testing values
		long max_itr = 10000;
		long max_time = 5;
		int[] player1_cards = {32,32,52,92,102,111,112};
		int[] card_counts = {7,10,5,5};
		int[] cards_played = {20,72,80,81,81,82,10,10,70,70,71,71,30,41,51,62,72,11,12,12,30,31,20,22,60,61,62,11,13,21,31,51,22,42,52,92,102};
		int[] current_hand = {22,42,52,92,102};
		int num_passes = 0;
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
		}
				
		// make sure player cards size is same as card count
		if (player1_cards.length != card_counts[0]) {
			throw new Exception("Incorrect number of player cards");
		}
		
		// make sure played cards is the correct length
		if (64 - cards_played.length != card_counts[0] + card_counts[1] + card_counts[2] + card_counts[3]) {
			throw new Exception("Incorrect number of cards played");
		}
		
		// make hand Indices
		Arrays.sort(player1_cards);

		// make root node
		Node root = new Node(player1_cards, card_counts, cards_played, current_hand, new int[0], num_passes, player_to_act);
		
		// do until max itr or max time
		long itr = 0;
		long start_time = System.currentTimeMillis();
		while ((max_itr == -1 || itr < max_itr) && (max_time == -1 || System.currentTimeMillis() - start_time < max_time * 1000)) {
			// pick a random determiniztion
			Determinization d = root.randomD();
			
			// select
			LinkedList<Node> node_chain = new LinkedList<>();
			node_chain.add(root);
			LinkedList<Node> available_nodes = new LinkedList<>();
			Node current_node = root;
			LinkedList<Cards> no_child_actions = null;
			while (!current_node.isTerminal()) {
				// make a set of the actions of children
				HashSet<Cards> action_set = new HashSet<>();  // TODO can these be stored in the nodes so they don't need to be generated every time??
				HashMap<Cards, Node> action_map = new HashMap<>();
				for (int i = 0; i < current_node.getChildren().size(); i++) {
					Node child = current_node.getChildren().get(i);
					Cards action_cards = new Cards(child.getLastAction());
					action_set.add(action_cards);
					action_map.put(action_cards, child);
				}
				
				// get all of the actions possible from the determinization
				LinkedList<Cards> current_node_actions = d.getActions();
				
				// for each action check if in actions set, break if not
				no_child_actions = new LinkedList<>();
				for (Cards action_cards : current_node_actions) {
					if (!action_set.contains(action_cards)) {
						no_child_actions.add(action_cards);
					}
				}
				
				if (no_child_actions.size() == 0) {
					//System.out.println("getting from action map...");  // testing TODO
					for (Cards action_cards : current_node_actions) {
						available_nodes.add(action_map.get(action_cards));
						
						/*if (action_cards.cards.length == 0) {  // testing TODO
							System.out.println("passing action in list, " + action_map.get(action_cards).getLastAction().length);
						}*/
					}
				} else {
					break;
				}
				
				
				// select an action
				double max_score = -1;
				Node max_node = null;
				//System.out.println("selecting cards from " + action_set.size());
				for (Cards cards : current_node_actions) {
					Node action_node = action_map.get(cards);				
					double action_node_score = action_node.getUCB(); 
					
					if (action_node_score > max_score ) {
						max_node = action_node;
						max_score = action_node_score;
					}
				}
				
				// update select vars
				node_chain.add(max_node);
				current_node = max_node;
				
				// update determinization
				d.newAction(max_node.getLastAction());
			}
			
			// expand
			// if not terminal
			if (!current_node.isTerminal()) {
				// choose an action uniformly at random
				Random rand = new Random();
				Cards rand_action = no_child_actions.get(rand.nextInt(no_child_actions.size()));
				
				// add the child node
				//System.out.println("depth " + (node_chain.size() + 1));
				Node child_node = current_node.newChild(rand_action.cards);
				node_chain.add(child_node);  // add to node_chain	
				d.newAction(rand_action.cards);  // update determinization
				
				child_node.incrementAvailability();
			}			
			
			// simulate
			int[] reward = d.simulateGame();
			
			// backpropagate
			for (Node node : node_chain) {  // node chain
				node.updateReward(reward);
				node.incrementVisits();
			}
			for (Node node : available_nodes) {  // availability
				node.incrementAvailability();
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
		// TODO
		
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
