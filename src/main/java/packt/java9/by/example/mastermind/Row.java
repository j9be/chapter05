package packt.java9.by.example.mastermind;

/**
 * One row on the table. It contains the guesses at getColor and also the
 * number of matching colors and getColor.
 */
public class Row {
    final protected Guess guess;
    final protected int full;
    final protected int partial;
    public static final Row none = new Row(Guess.none,0,0);

    public Row(Guess guess,int full, int partial) {
        this.guess = guess;
        this.full = full;
        this.partial = partial;
    }

    protected Row(Row cloneFrom) {
        this(cloneFrom.guess,cloneFrom.full,cloneFrom.partial);
    }

    public boolean matches(Guess guess) {
        return this.guess.nrOfPartialMatches(guess) == partial &&
                this.guess.nrOfFullMatches(guess) == full;
    }

    public int nrOfColumns() {
        return guess.nrOfColumns();
    }

}
