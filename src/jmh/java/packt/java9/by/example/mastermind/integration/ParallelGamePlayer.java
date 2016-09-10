package packt.java9.by.example.mastermind.integration;


import packt.java9.by.example.mastermind.*;
import packt.java9.by.example.mastermind.lettered.LetteredColorFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelGamePlayer {

    private static final int NR_COLORS = 10;
    final ColorManager manager = new ColorManager(NR_COLORS, new LetteredColorFactory());
    private static final int NR_COLUMNS = 6;
    private final int nrThreads;
    private final BlockingQueue<Guess> guessQueue;

    public ParallelGamePlayer(int nrThreads, int queueSize) {
        guessQueue = new ArrayBlockingQueue<Guess>(nrThreads * queueSize);
        this.nrThreads = nrThreads;
    }

    public void playParallel() {
        Table table = new Table(NR_COLUMNS, manager);
        Guess secret = createSecret();
        Game game = new Game(table, secret);
        final IntervalGuesser[] guessers = createGuessers(table);
        startAsynchronousGuessers(guessers);
        final Guesser finalCheckGuesser = new UniqueGuesser(table);
        int serial = 1;
        try {
            while (!game.isFinished()) {
                final Guess guess = guessQueue.take();
                if (finalCheckGuesser.guessMatch(guess)) {
                    game.addNewGuess(guess);
                    serial++;
                }
            }
        } catch (InterruptedException ie) {

        } finally {
            stopAsynchronousGuessers(guessers);
        }
    }

    private ExecutorService executorService;

    private void startAsynchronousGuessers(IntervalGuesser[] guessers) {
        executorService = Executors.newFixedThreadPool(nrThreads);
        for (IntervalGuesser guesser : guessers) {
            executorService.execute(guesser);
        }
    }

    private void stopAsynchronousGuessers(IntervalGuesser[] guessers) {
        executorService.shutdown();
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
        final IntervalGuesser[] guessers = new IntervalGuesser[nrThreads];
        for (int i = 0; i < nrThreads - 1; i++) {
            Guess end = nextIntervalStart(colors);
            guessers[i] = new IntervalGuesser(table, start, end, guessQueue);
            start = end;
        }
        guessers[nrThreads - 1] = new IntervalGuesser(table, start, Guess.none, guessQueue);
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
        int step = NR_COLORS / nrThreads;
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
