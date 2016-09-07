package packt.java9.by.example.mastermind.integration;


import org.junit.Assert;
import org.junit.Test;
import packt.java9.by.example.mastermind.*;
import packt.java9.by.example.mastermind.lettered.LetteredColor;
import packt.java9.by.example.mastermind.lettered.LetteredColorFactory;

public class IntegrationTest {

    final int nrColors = 6;
    final int nrColumns = 4;
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

    @Test
    public void testSimpleGame() {
        Table table = new Table(nrColumns, manager);
        Guess secret = createSecret();
        System.out.println(PrettyPrintRow.pprint(new Row(secret, 4, 0)));
        System.out.println();
        Game game = new Game(table, secret);

        Guesser guesser = new UniqueGuesser(table);
        while (!game.isFinished()) {
            Guess guess = guesser.guess();
            if (guess == Guess.none) {
                Assert.fail();
            }
            Row row = game.addNewGuess(guess);
            System.out.println(PrettyPrintRow.pprint(row));
        }
    }
}
