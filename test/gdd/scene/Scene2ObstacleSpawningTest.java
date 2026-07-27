package gdd.scene;

import static gdd.Global.secondsToTicks;
import gdd.RunState;
import gdd.sprite.obstacle.Coral;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class Scene2ObstacleSpawningTest {

    public static void main(String[] args) throws Exception {
        doesNotAddPlaceholderCoralInScriptedMode();
    }

    private static void doesNotAddPlaceholderCoralInScriptedMode()
            throws Exception {
        Scene2 scene = new Scene2(null, new RunState());
        setStageTick(scene, secondsToTicks(11));

        invoke(scene, "spawnPlaceholderObstacles");

        List<Coral> corals = corals(scene);
        if (!corals.isEmpty()) {
            throw new AssertionError(
                    "scripted mode added a coral outside the event schedule");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Coral> corals(Scene2 scene) throws Exception {
        Field field = Scene2.class.getDeclaredField("corals");
        field.setAccessible(true);
        return (List<Coral>) field.get(scene);
    }

    private static void setStageTick(Scene2 scene, int stageTick)
            throws Exception {
        Field field = Scene2.class.getDeclaredField("stageTick");
        field.setAccessible(true);
        field.setInt(scene, stageTick);
    }

    private static void invoke(Scene2 scene, String methodName)
            throws Exception {
        Method method = Scene2.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(scene);
    }
}
