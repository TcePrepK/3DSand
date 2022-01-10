package core;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import toolbox.Octatree;
import toolbox.Point3D;
import toolbox.Vector3D;

import java.util.HashMap;
import java.util.List;

import static core.GlobalVariables.rand;

public class ImGuiManager {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private final HashMap<String, Boolean> enabledOnes = new HashMap<>();

    private final Octatree test;

    public ImGuiManager() {
        ImGui.createContext();
        imGuiGlfw.init(DisplayManager.getWindow(), true);
        imGuiGl3.init("#version 450");

        test = new Octatree(new Vector3D(0), 100, 3);
        for (int i = 0; i <= 50; i++) {
            final Point3D randomPos = new Point3D(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));
            test.addPoint(randomPos);
        }
    }

    public static void renderBranch(final Octatree branch, final String name, final int id) {
        if (ImGui.treeNode("##" + id)) {
            ImGui.sameLine();
            ImGui.selectable(name, true);
            if (branch.isBranched()) {
                final List<Octatree> branches = branch.getBranches();
                for (int i = 0; i < branches.size(); i++) {
                    ImGuiManager.renderBranch(branches.get(i), "Branch " + (i + 1), i);
                }
            } else {
                ImGui.text("PointAmount: " + branch.getPointAmount());
            }
            ImGui.treePop();
        } else {
            ImGui.sameLine();
            ImGui.selectable(name);
        }
    }

    public void update() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        ImGui.begin("Cool Window");

        // FPS
        ImGui.text("FPS: " + DisplayManager.getFPS());
        // FPS

        // World Branch
        ImGuiManager.renderBranch(test, "OctaTree", 0);
        // World Branch

        ImGui.end();
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void cleanUp() {
        imGuiGlfw.dispose();
        imGuiGl3.dispose();
        ImGui.destroyContext();
    }
}
