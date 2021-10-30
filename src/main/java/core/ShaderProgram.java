package core;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import toolbox.Color;
import toolbox.Vector3D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ShaderProgram {
    private final int programID;
    private final int stopID = 0;

    private final int vertexShaderID;
    private final int fragmentShaderID;

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram(final String vertexFile, final String fragmentFile) {
        vertexShaderID = ShaderProgram.loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        fragmentShaderID = ShaderProgram.loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        programID = GL20.glCreateProgram();
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);
        getAllUniformLocations();
    }

    protected abstract void getAllUniformLocations();

    protected int getUniformLocation(final String uniformName) {
        return GL20.glGetUniformLocation(programID, uniformName);
    }

    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(stopID);
    }

    public void cleanUp() {
        stop();
        GL20.glDetachShader(programID, vertexShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
        GL20.glDeleteProgram(programID);
    }

    protected abstract void bindAttributes();

    protected void bindAttribute(final int attribute, final String variableName) {
        GL20.glBindAttribLocation(programID, attribute, variableName);
    }

    protected static void loadFloat(final int location, final float value) {
        GL20.glUniform1f(location, value);
    }

    protected static void loadInt(final int location, final int value) {
        GL20.glUniform1i(location, value);
    }

    protected static void load2DVector(final int location, final Vector2f vector) {
        GL20.glUniform2f(location, vector.x(), vector.y());
    }

    protected static void load3DVector(final int location, final Vector3D vector) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected static void load4DVector(final int location, final Vector4f vector) {
        GL20.glUniform4f(location, vector.x(), vector.y(), vector.z(), vector.w());
    }

    protected static void load4DVector(final int location, final Color vector) {
        GL20.glUniform4f(location, vector.getR(), vector.getG(), vector.getB(), vector.getA());
    }

    static void loadBoolean(final int location, final boolean value) {
        float toLoad = 0;
        if (value) {
            toLoad = 1;
        }
        GL20.glUniform1f(location, toLoad);
    }

    protected static void loadMatrix(final int location, final Matrix4f matrix) {
        matrix.get(ShaderProgram.matrixBuffer);
        ShaderProgram.matrixBuffer.flip();
        GL20.glUniformMatrix4fv(location, false, ShaderProgram.matrixBuffer);
    }

    private static void shaderReader(final StringBuilder shaderSource, final String file, final List<String> includeList) {
        try {
            final BufferedReader reader = new BufferedReader(new FileReader("src/main/java" + file));
            String line;
            while ((line = reader.readLine()) != null) {
                final Pattern commentPattern = Pattern.compile("\\/\\/[^\\n\\r]+");
                final Matcher commentMatcher = commentPattern.matcher(line);
                if (commentMatcher.find()) {
                    continue;
                }

                final Pattern includePattern = Pattern.compile("(?<=#include )[\\/\\w+.]+");
                final Matcher includeMatcher = includePattern.matcher(line);
                if (includeMatcher.find()) {
                    final String includePath = includeMatcher.group(0);

                    if (includeList.contains(includePath)) {
                        throw new IOException("Circular Dependency!");
                    }

                    includeList.add(includePath);
                    ShaderProgram.shaderReader(shaderSource, includePath, includeList);
                } else {
                    shaderSource.append(line).append("//\n");
                }
            }
            reader.close();
        } catch (final IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static int loadShader(final String file, final int type) {
        final StringBuilder shaderSource = new StringBuilder();
        ShaderProgram.shaderReader(shaderSource, file, new ArrayList<>());

        final int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }
        return shaderID;
    }
}
