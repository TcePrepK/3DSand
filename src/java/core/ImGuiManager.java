package core;

import display.DisplayManager;
import game.ChunkGenerationSpeed;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImInt;
import toolbox.Logger;
import toolbox.Octatree;
import toolbox.Points.Point3D;

import java.util.List;

import static core.GlobalVariables.*;

public class ImGuiManager {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public ImGuiManager() {
        ImGui.createContext();
        imGuiGlfw.init(DisplayManager.getWindow(), true);
        imGuiGl3.init("#version 450");

        Logger.out("~ ImGui Initialized Successfully");
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
                ImGui.text(" ~PointAmount: " + branch.getPointAmount());
            }
            ImGui.treePop();
        } else {
            ImGui.sameLine();
            ImGui.selectable(name);
        }
    }

    public void update(final double generationTime, final double renderTime) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        ImGui.begin("Cool Window");

        // FPS
        ImGui.text("FPS: " + DisplayManager.getFPS());
        ImGui.text("Rendering time: " + renderTime + "ms");
        ImGui.spacing();
        ImGui.spacing();
        // FPS

        // Output Control
        if (ImGui.treeNode("Output Options")) {
            final String[] options = renderer.getAttachmentManager().keys();
            int selectedOption = 0;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(outputOption)) {
                    selectedOption = i;
                    break;
                }
            }

            final ImInt selected = new ImInt(selectedOption);
            ImGui.listBox("##Options", selected, options, 5);
            outputOption = options[selected.get()];

            ImGui.treePop();
        }
        ImGui.spacing();
        ImGui.spacing();
        // Output Control

        // World Generation
        if (ImGui.treeNode("Speed Options")) {
            final String[] options = ChunkGenerationSpeed.valueNames();
            int selectedOption = 0;
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(generationSpeedOption)) {
                    selectedOption = i;
                    break;
                }
            }

            final ImInt selected = new ImInt(selectedOption);
            ImGui.listBox("##Options", selected, options, 3);
            generationSpeedOption = options[selected.get()];

            ImGui.treePop();
        }

        if (ImGui.checkbox("Generate World", generateWorld)) {
            generateWorld = !generateWorld;
        }

        ImGui.spacing();
        ImGui.spacing();
        // World Generation

        // Ray Control
        if (ImGui.checkbox("Path Tracing", pathTracing)) {
            pathTracing = !pathTracing;
        }

        if (ImGui.checkbox("Render Bitmask Borders", drawBitmaskBorders)) {
            drawBitmaskBorders = !drawBitmaskBorders;
        }

        ImGui.spacing();
        ImGui.spacing();
        // Ray Control

        // World
        final Point3D worldScale = world.getWorldScale();
        ImGui.text("World: " + worldScale.x + "x" + worldScale.y + "x" + worldScale.z);
        ImGui.text("World generation percentage: " + worldGenerationPercentage + "%");
        ImGui.text("World generation time: " + generationTime + "sec");
        // World

        // World Branch
//        for (int i = 0; i < world.getChunkList().size(); i++) {
//            final Chunk chunk = world.getChunkList().get(i);
//            final Octatree octatree = chunk.getOctaTree();
//            ImGuiManager.renderBranch(octatree, "Chunk " + chunk.getId() + " " + octatree.getPointAmount(), i);
//        }
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
