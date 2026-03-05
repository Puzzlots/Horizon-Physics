package me.zombii.horizon;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import me.zombii.horizon.entity.Cube;
import me.zombii.horizon.rendering.mesh.IMeshInstancer;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.IItemRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class HorizonConstants {

    public static Consumer<Cube> EXEC;
    public static IMeshInstancer MESHER_INSTANCE;
    public static IItemRegistrar ITEM_REGISTRAR_INSTANCE;
    public static final String MOD_ID = "horizon";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

}
