package packt.java9.by.example.mastermind.integration;

import packt.java9.by.example.mastermind.*;
import packt.java9.by.example.mastermind.lettered.LetteredColorFactory;

public class SimpleGamePlayer {
    final int nrColors = 12;
    final int nrColumns = 7;
    final ColorManager manager = new ColorManager(nrColors, new LetteredColorFactory());
    private Guess createSecret() {
        Color[] colors = new Color[nrColumns];
        int count = 0;
        Color color = manager.firstColor();
        while (count < nrColors - nrColumns) {
            color = manager.nextColor(color);
            count++;
        }
        for (int i = 0; i < nrColumns; i++) {
            colors[i] = color;
            color = manager.nextColor(color);
        }
        return new Guess(colors);
    }

    public void play() {
        Table table = new Table(nrColumns, manager);
        Guess secret = createSecret();
        Game game = new Game(table, secret);

        Guesser guesser = new UniqueGuesser(table);
        while (!game.isFinished()) {
            Guess guess = guesser.guess();
            game.addNewGuess(guess);
        }
    }
}
