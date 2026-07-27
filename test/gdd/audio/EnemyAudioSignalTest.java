package gdd.audio;

import static gdd.Global.secondsToTicks;
import gdd.RunState;
import gdd.sprite.Player;
import gdd.sprite.enemy.Jellyfish;
import gdd.sprite.enemy.Snake;

public class EnemyAudioSignalTest {

    public static void main(String[] args) {
        Player player = new Player(new RunState());

        Jellyfish jellyfish = new Jellyfish(player, 500, 300);
        for (int tick = 0; tick < secondsToTicks(2); tick++) {
            jellyfish.act();
        }
        assert jellyfish.consumeAttackStarted();
        assert !jellyfish.consumeAttackStarted();

        Snake snake = new Snake(player, 500, true);
        for (int tick = 0; tick < secondsToTicks(1); tick++) {
            snake.act();
        }
        assert snake.consumeAttackStarted();
        assert !snake.consumeAttackStarted();

        System.out.println("EnemyAudioSignalTest passed");
    }
}
