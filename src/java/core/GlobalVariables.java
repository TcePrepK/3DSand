package core;

import elements.ElementPlacer;
import elements.ElementRegistry;
import game.Camera;
import game.ChunkManager;
import game.Player;
import game.World;
import renderers.MasterRenderer;
import toolbox.MousePicker;

import java.util.Random;

public class GlobalVariables {
    public static int currentFrame = 0;

    public static boolean mouseLocked = false;
    public final static boolean creativeMode = true;
    public static boolean freePlayMode = true;
    public static boolean showDirtyRect = true;
    public static boolean renderChunks = true;
    public static boolean noisyWorld = true;
    public static boolean pathTracing = true;
    public static boolean drawBitmaskBorders = false;

    public static String outputOption = "color";

    public final static ElementRegistry elementRegistery = new ElementRegistry();
    public static ElementPlacer elementPlacer = new ElementPlacer();
    public static String currentMat = "Sand";

    public final static int mapChunkSize = 32;
    public final static int chunkViewDistance = 1;
    public static float worldGenerationPercentage = 0;

    public final static World world = new World();
    public final static ChunkManager chunkManager = new ChunkManager(2 * GlobalVariables.chunkViewDistance, 2 * GlobalVariables.chunkViewDistance, 2 * GlobalVariables.chunkViewDistance);

    public static MousePicker mousePicker;
    public final static Random rand = new Random();
    public static Camera camera = new Camera();
    public static Player player;


    public static ImGuiManager imGuiManager;
    public static Loader loader;
    public static MasterRenderer renderer;
}
