
public class Cards {
	public int[] cards;
	
	public Cards(int[] cards) {
		this.cards = cards;
	}
	
	// TODO is this the best way to do it?
	@Override
	public int hashCode() {
		int hash = 0;
		int power_34 = 1;
		
		for (int card : this.cards) {
			hash += power_34 * Shared.card_index.get(card);
			power_34 *= 34;
		}
		
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (!(o instanceof Cards)) { 
            return false; 
        }
		
		Cards c = (Cards) o;
		
		int num_cards = this.cards.length;
		if (num_cards != c.cards.length) {
			return false;
		}
		
		for (int i = 0; i < num_cards; i++) {
			if (this.cards[i] != c.cards[i]) {
				return false;
			}
		}
		
		return true;
	}
}
