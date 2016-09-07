package packt.java9.by.example.mastermind.integration;


import org.junit.Assert;
import org.junit.Test;
import packt.java9.by.example.mastermind.*;
import packt.java9.by.example.mastermind.lettered.LetteredColorFactory;

import java.util.concurrent.*;

public class ParallelGameTest {

    private final int NR_COLORS = 6;
    final ColorManager manager = new ColorManager(NR_COLORS, new LetteredColorFactory());
    private final int NR_COLUMNS = 4;
    private final int NR_THREADS = 4;
    private BlockingQueue<Guess> guessQueue = new ArrayBlockingQueue<Guess>(NR_THREADS*2);

    @Test
    public void testSimpleGame() throws ExecutionException, InterruptedException {
        Table table = new Table(NR_COLUMNS, manager);
        Guess secret = createSecret();
        System.out.println(PrettyPrintRow.pprint(new Row(secret, NR_COLUMNS, 0)));
        System.out.println();
        Game game = new Game(table, secret);
        final IntervalGuesser[] guessers = createGuessers(table);
        startGuessers(guessers);
        int serial = 1;
        while (!game.isFinished()) {
            Guess guess = guessQueue.take();
            if (guess == Guess.none) {
                Assert.fail();
            }
            Row row = game.addNewGuess(guess);
            System.out.print("" + serial + ". ");
            serial++;
            System.out.println(PrettyPrintRow.pprint(row));
        }
    }

    private void startGuessers(IntervalGuesser[] guessers) {
        Executor executor = Executors.newFixedThreadPool(NR_THREADS);
        for (IntervalGuesser guesser : guessers) {
            executor.execute(guesser);
        }
    }

    private Guess createSecret() {
        Color[] colors = new Color[NR_COLUMNS];
        int count = 0;
        Color color = manager.firstColor();
        while (count < NR_COLORS - NR_COLUMNS) {
            color = manager.nextColor(color);
            count++;
        }
        for (int i = 0; i < NR_COLUMNS; i++) {
            colors[i] = color;
            color = manager.nextColor(color);
        }
        return new Guess(colors);
    }

    private IntervalGuesser[] createGuessers(Table table) {
        final Color[] colors = new Color[NR_COLUMNS];
        Guess start = firstIntervalStart(colors);
        final IntervalGuesser[] guessers = new IntervalGuesser[NR_THREADS];
        for (int i = 0; i < NR_THREADS - 1; i++) {
            Guess end = nextIntervalStart(colors);
            guessers[i] = new IntervalGuesser(table, start, end, guessQueue);
            start = end;
        }
        guessers[NR_THREADS - 1] = new IntervalGuesser(table, start, Guess.none, guessQueue);
        for (Guesser guesser : guessers) {
            System.out.println(guesser.toString());
        }
        return guessers;
    }


    private Guess firstIntervalStart(Color[] colors) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = manager.firstColor();
        }
        return new Guess(colors);
    }

    private Guess nextIntervalStart(Color[] colors) {
        final int index = colors.length - 1;
        int step = NR_COLORS / NR_THREADS;
        if (step == 0) {
            step = 1;
        }
        while (step > 0) {
            if (manager.thereIsNextColor(colors[index])) {
                colors[index] = manager.nextColor(colors[index]);
                step--;
            } else {
                return Guess.none;
            }
        }
        Guess guess = new Guess(colors);
        while (!guess.isUnique()) {
            guess = guess.nextGuess(manager);
        }
        return guess;
    }
}
