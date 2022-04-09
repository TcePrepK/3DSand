package core;

import elements.ElementPlacer;
import game.*;
import renderers.MasterRenderer;
import toolbox.MousePicker;

import java.util.Random;

public class GlobalVariables {
    public static int currentFrame = 0;

    // Debugging
    public static boolean mouseLocked = false;
    public final static boolean creativeMode = true;
    public static boolean freePlayMode = true;
    public static boolean showDirtyRect = true;
    public static boolean renderChunks = true;
    public static boolean noisyWorld = true;
    public static boolean pathTracing = true;
    public static boolean drawBitmaskBorders = false;
    public static boolean updateSun = false;
    // Debugging

    // Core
    public static ImGuiManager imGuiManager = new ImGuiManager();
    public static Loader loader = new Loader();
    public static ThreadManager threadManager = new ThreadManager();
    // Core

    // Elements
//    public final static ElementRegistry elementRegistery = new ElementRegistry();
    public static ElementPlacer elementPlacer = new ElementPlacer();
    public static String currentMat = "Sand";
    // Elements

    // World
    public final static double mapSeed = new Random().nextGaussian() * 65536;

    public final static int mapChunkSize = 32;
    public final static int mapBitmaskSize = 4;

    public final static int chunkViewDistance = 8;
    public static boolean generateWorld = true;
    public static boolean updateWorld = false;

    public final static World world = new World();
    public final static Sun sun = new Sun();
    public final static ChunkManager chunkManager = new ChunkManager(2 * GlobalVariables.chunkViewDistance, 2 * GlobalVariables.chunkViewDistance, 2 * GlobalVariables.chunkViewDistance);
    // World

    // User
    public static MousePicker mousePicker;
    public final static Random rand = new Random((long) GlobalVariables.mapSeed);
    public static Camera camera = new Camera();
    public static Player player;
    // User

    // Output
    public static MasterRenderer renderer = new MasterRenderer();
    public static String outputOption = "color";
    public static int lightBounceAmount = 1;
    // Output
}
