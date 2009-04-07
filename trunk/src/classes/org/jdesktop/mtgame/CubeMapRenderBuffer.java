/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame;

import java.awt.Canvas;
import java.util.ArrayList;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.image.TextureCubeMap;
import com.jme.renderer.TextureRenderer;

import java.nio.IntBuffer;
import com.jme.util.geom.BufferUtils;
import com.jme.util.TextureManager;
import com.jme.scene.state.jogl.JOGLTextureState;
import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.renderer.RenderContext;
import com.jme.renderer.Camera;
import com.jme.scene.state.jogl.records.TextureRecord;
import com.jme.scene.state.jogl.records.TextureStateRecord;
import com.jme.system.DisplaySystem;
import com.jme.renderer.ColorRGBA;
import com.jme.math.Vector3f;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class CubeMapRenderBuffer extends RenderBuffer {
    /**
     * A camera for the cubemap render
     */
    private Camera cubeMapCamera = null;
    private Vector3f negativeX = new Vector3f(-1.0f, 0.0f, 0.0f);
    private Vector3f positiveX = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f negativeY = new Vector3f(0.0f, -1.0f, 0.0f);
    private Vector3f positiveY = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f negativeZ = new Vector3f(0.0f, 0.0f, -1.0f);
    private Vector3f positiveZ = new Vector3f(0.0f, 0.0f, 1.0f);
    ColorRGBA bgColor = new ColorRGBA();

    /**
     * The constructor
     */
    public CubeMapRenderBuffer(Target target, int width, int height) {
        super(target, width, height);
        setTexture(new TextureCubeMap());
    }

    /**
     * Initialize this RenderBuffer.  This is called from the renderer
     * before the buffer is rendered into.
     */
    void update(DisplaySystem display, Spatial skybox, ArrayList renderComponents) {
        GL gl = GLU.getCurrentGL();

        synchronized (getRBLock()) {
            if (!isInitialized()) {
                createTextureObjects(gl, display);
                updateRenderList(skybox, renderComponents);
                setInitialized(true);
            }
        }
    }
    
    /**
     * Create the jME texture objects, and prep them for rendering
     */
    private void createTextureObjects(GL gl, DisplaySystem display) {
        // First do the common render target
        assignTextureId(gl, getTexture(), Texture.Type.CubeMap);
        allocateTextureData(gl, getTexture(), Texture.Type.CubeMap);
        setupState(display, getTexture());
    }
    
    /**
     * Manage the texture id.
     */
    void assignTextureId(GL gl, Texture t, Texture.Type type) {
        IntBuffer ibuf = BufferUtils.createIntBuffer(1);

        if (t.getTextureId() != 0) {
            ibuf.put(t.getTextureId());
            gl.glDeleteTextures(ibuf.limit(), ibuf); // TODO Check <size>
            ibuf.clear();
        }

        // Create the texture
        gl.glGenTextures(ibuf.limit(), ibuf); // TODO Check <size>
        t.setTextureId(ibuf.get(0));
        TextureManager.registerForCleanup(t.getTextureKey(), t.getTextureId());

        JOGLTextureState.doTextureBind(t.getTextureId(), 0, type);
    }
    
        
    /**
     * Allocate the texture data, based upon what we are doing.
     */
    void allocateTextureData(GL gl, Texture t, Texture.Type type) {
        int components = GL.GL_RGBA8;
	int format = GL.GL_RGBA;
	int dataType = GL.GL_UNSIGNED_BYTE;
        int width = getWidth();
        int height = getHeight();

        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, components, width, height, 0,
                format, dataType, null);
        gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, components, width, height, 0,
                format, dataType, null);
        if (t.getMinificationFilter().usesMipMapLevels()) {
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        }

    }
    
    /**
     * Setup some state on the texture
     */
    void setupState(DisplaySystem display, Texture t) {   
        // Setup filtering and wrap
        RenderContext<?> context = display.getCurrentContext();
        TextureStateRecord record = (TextureStateRecord) context
                .getStateRecord(RenderState.StateType.Texture);
        TextureRecord texRecord = record.getTextureRecord(t.getTextureId(), t.getType());

        JOGLTextureState.applyFilter(t, texRecord, 0, record);
        JOGLTextureState.applyWrap(t, texRecord, 0, record);
    }
    
    /**
     * Render the current RenderList into this buffer
     */
    void render(Renderer r) {
        GL gl = GLU.getCurrentGL();
        com.jme.renderer.Renderer jmeRenderer = r.getJMERenderer();
        int width = getWidth();
        int height = getHeight();

        CameraComponent cc = getCameraComponent();
        Camera ccCamera = cc.getCamera();
        ccCamera.update();

        if (cubeMapCamera == null) {
            cubeMapCamera = r.createJMECamera(getWidth(), getHeight());
            cubeMapCamera.setFrustumPerspective(cc.getFieldOfView(), cc.getAspectRatio(),
                    cc.getNearClipDistance(), cc.getFarClipDistance());
        }
        Camera saveCamera = jmeRenderer.getCamera();
        jmeRenderer.setCamera(cubeMapCamera);
        getBackgroundColor(bgColor);
        jmeRenderer.setBackgroundColor(bgColor);
        cubeMapCamera.setLocation(ccCamera.getLocation());
        JOGLTextureState.doTextureBind(getTexture().getTextureId(), 0, Texture.Type.CubeMap);

        // Render Negative X
        cubeMapCamera.setDirection(negativeX);
        cubeMapCamera.setUp(new Vector3f(0.0f, 1.0f, 0.0f));
        cubeMapCamera.setLeft(new Vector3f(0.0f, 0.0f, 1.0f));
        cubeMapCamera.update();
        jmeRenderer.setCamera(cubeMapCamera);
        r.renderScene(getSpatialList());
        //r.swapAndWait(3000); 
        gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL.GL_RGBA, 0, 0, width, height, 0);

        // Render Positive X
        cubeMapCamera.setDirection(positiveX);
        cubeMapCamera.setLeft(new Vector3f(0.0f, 0.0f, -1.0f));
        cubeMapCamera.update();
        jmeRenderer.setCamera(cubeMapCamera);
        r.renderScene(getSpatialList());
        //r.swapAndWait(3000);   
        gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL.GL_RGBA, 0, 0, width, height, 0);

        // Render Negative Y
        cubeMapCamera.setDirection(negativeY);
        cubeMapCamera.setUp(new Vector3f(0.0f, 0.0f, -1.0f));
        cubeMapCamera.setLeft(new Vector3f(1.0f, 0.0f, 0.0f));
        cubeMapCamera.update();
        jmeRenderer.setCamera(cubeMapCamera);
        r.renderScene(getSpatialList());
        //r.swapAndWait(3000); 
        gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL.GL_RGBA, 0, 0, width, height, 0);

        // Render Positive Y
        cubeMapCamera.setDirection(positiveY);
        cubeMapCamera.setUp(new Vector3f(0.0f, 0.0f, 1.0f));
        cubeMapCamera.update();
        jmeRenderer.setCamera(cubeMapCamera);
        r.renderScene(getSpatialList());
        //r.swapAndWait(3000); 
        gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL.GL_RGBA, 0, 0, width, height, 0);

        // Render Negative Z
        cubeMapCamera.setDirection(negativeZ);
        cubeMapCamera.setUp(new Vector3f(0.0f, 1.0f, 0.0f));
        cubeMapCamera.setLeft(new Vector3f(-1.0f, 0.0f, 0.0f));
        cubeMapCamera.update();
        jmeRenderer.setCamera(cubeMapCamera);
        r.renderScene(getSpatialList());
        //r.swapAndWait(3000); 
        gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL.GL_RGBA, 0, 0, width, height, 0);

        // Render Positive Z
        cubeMapCamera.setDirection(positiveZ);
        cubeMapCamera.setLeft(new Vector3f(1.0f, 0.0f, 0.0f));
        cubeMapCamera.update();
        jmeRenderer.setCamera(cubeMapCamera);
        r.renderScene(getSpatialList());
        //r.swapAndWait(3000); 
        gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL.GL_RGBA, 0, 0, width, height, 0);

        jmeRenderer.setCamera(saveCamera);
    }
}
