/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame;

import java.util.ArrayList;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.renderer.TextureRenderer;

import java.nio.IntBuffer;
import com.jme.util.geom.BufferUtils;
import com.jme.util.TextureManager;
import com.jme.scene.state.jogl.JOGLTextureState;
import com.jme.scene.state.RenderState;
import com.jme.scene.Spatial;
import com.jme.renderer.RenderContext;
import com.jme.scene.state.jogl.records.TextureRecord;
import com.jme.scene.state.jogl.records.TextureStateRecord;
import com.jme.system.DisplaySystem;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;

import com.jme.image.Texture.DepthTextureCompareFunc;
import com.jme.image.Texture.DepthTextureCompareMode;
import com.jme.image.Texture.DepthTextureMode;
import com.jme.renderer.Camera;
import com.jme.renderer.AbstractCamera;
import com.jme.renderer.ColorRGBA;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public class ShadowMapRenderBuffer extends RenderBuffer { 
    /**
     * The direction for the camera
     */
    private Vector3f cameraDirection = new Vector3f();
    
    /**
     * The position for the camera
     */
    private Vector3f cameraPosition = new Vector3f();
    
    /**
     * The Look At for the camera
     */
    private Vector3f cameraLookAt = new Vector3f();
    
    /**
     * The up axis for the camera
     */
    private Vector3f cameraUp = new Vector3f();
    
    /**
     * The camera used for rendering
     */
    private Camera camera = null;
    
    /**
     * A flag that indicates something in the camera changed
     */
    private boolean cameraChanged = true;
    
    /**
     * A flag indicating parallel projection
     */
    private boolean cameraIsParallel = true;
    
    private static Matrix4f biasMatrix = new Matrix4f(0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.5f, 0.5f, 0.5f,
            1.0f);
    
    ColorRGBA bgColor = new ColorRGBA();

    /**
     * The constructor
     */
    public ShadowMapRenderBuffer(Target target, int width, int height) {
        super(target, width, height);
        Texture2D shadowMapTexture = new Texture2D();
        setTexture(shadowMapTexture);
        shadowMapTexture.setApply(Texture.ApplyMode.Modulate);
        shadowMapTexture.setMinificationFilter(Texture.MinificationFilter.NearestNeighborNoMipMaps);
        shadowMapTexture.setWrap(Texture.WrapMode.Clamp);
        shadowMapTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        
        shadowMapTexture.setRenderToTextureType(Texture.RenderToTextureType.Depth);
        shadowMapTexture.setMatrix(new Matrix4f());
        shadowMapTexture.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.EyeLinear);
        shadowMapTexture.setDepthCompareMode(DepthTextureCompareMode.RtoTexture);
        shadowMapTexture.setDepthCompareFunc(DepthTextureCompareFunc.GreaterThanEqual);
        shadowMapTexture.setDepthMode(DepthTextureMode.Intensity);   
    }
    
    /**
     * Set the camera direction
     */
    public void setCameraDirection(Vector3f dir) {
        synchronized (getRBLock()) {
            cameraDirection.x = dir.x;
            cameraDirection.y = dir.y;
            cameraDirection.z = dir.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraDirection(Vector3f dir) {
        synchronized (getRBLock()) {
            dir.x = cameraDirection.x;
            dir.y = cameraDirection.y;
            dir.z = cameraDirection.z;
        }
    }
    
    /**
     * Set the camera direction
     */
    public void setCameraLookAt(Vector3f pos) {
        synchronized (getRBLock()) {
            cameraLookAt.x = pos.x;
            cameraLookAt.y = pos.y;
            cameraLookAt.z = pos.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraLookAt(Vector3f pos) {
        synchronized (getRBLock()) {
            pos.x = cameraLookAt.x;
            pos.y = cameraLookAt.y;
            pos.z = cameraLookAt.z;
        }
    }
    
    /**
     * Set the camera direction
     */
    public void setCameraUp(Vector3f up) {
        synchronized (getRBLock()) {
            cameraUp.x = up.x;
            cameraUp.y = up.y;
            cameraUp.z = up.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraUp(Vector3f up) {
        synchronized (getRBLock()) {
            up.x = cameraUp.x;
            up.y = cameraUp.y;
            up.z = cameraUp.z;
        }
    }
    
    /**
     * Set the camera direction
     */
    public void setCameraPosition(Vector3f pos) {
        synchronized (getRBLock()) {
            cameraPosition.x = pos.x;
            cameraPosition.y = pos.y;
            cameraPosition.z = pos.z;
            cameraChanged = true;
        }
    }

    /**
     * Get the camera direction
     */
    public void getCameraPosition(Vector3f pos) {
        synchronized (getRBLock()) {
            pos.x = cameraPosition.x;
            pos.y = cameraPosition.y;
            pos.z = cameraPosition.z;
        }
    }
    
    /**
     * Initialize this RenderBuffer.  This is called from the renderer
     * before the buffer is rendered into.
     */
    void update(DisplaySystem display, Spatial skybox, ArrayList renderComponents) {
        GL gl = GLU.getCurrentGL();

        synchronized (getRBLock()) {
            if (!isInitialized()) {
                createTextureRenderer(display);
                getTextureRenderer().setupTexture((Texture2D)getTexture());
                //createTextureObjects(gl, display);
                setInitialized(true);
            }
            updateRenderList(skybox, renderComponents);
        }
    }
    
    /**
     * Create the jME texture objects, and prep them for rendering
     */
    private void createTextureObjects(GL gl, DisplaySystem display) {
        // First do the common render target
        assignTextureId(gl, getTexture(), Texture.Type.TwoDimensional);
        allocateTextureData(gl, getTexture(), Texture.Type.TwoDimensional);
        setupState(display, getTexture());
    }
        
    private void createTextureRenderer(DisplaySystem display) {
        TextureRenderer.Target tRtarget = TextureRenderer.Target.Texture2D;
        setTextureRenderer(display.createTextureRenderer(getWidth(), getHeight(), tRtarget));
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
        int components = GL.GL_DEPTH_COMPONENT;
	int format = GL.GL_DEPTH_COMPONENT;
	int dataType = GL.GL_UNSIGNED_BYTE;
        
        Texture.RenderToTextureType rttType = Texture.RenderToTextureType.Depth;

        t.setRenderToTextureType(rttType);

        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, components, getWidth(), getHeight(), 0,
                format, dataType, null);
        if (t.getMinificationFilter().usesMipMapLevels()) {
            gl.glGenerateMipmapEXT(GL.GL_TEXTURE_2D);
        }
    }
    
    /**
     * Setup some state on the texture
     */
    void setupState(DisplaySystem display, Texture t) {   
        // Setup filtering and wrap
        RenderContext<?> context = display.getCurrentContext();
        TextureStateRecord record = (TextureStateRecord) context
                .getStateRecord(RenderState.RS_TEXTURE);
        TextureRecord texRecord = record.getTextureRecord(t.getTextureId(), t.getType());

        JOGLTextureState.applyFilter(t, texRecord, 0, record);
        JOGLTextureState.applyWrap(t, texRecord, 0, record);
    }
    
    /**
     * Set the camera attributes
     */
    void updateCamera() {
        camera.setLocation(cameraPosition);
        //camera.setDirection(cameraDirection);
        //camera.setLeft(cameraLeft);
        //camera.setUp(cameraUp);
        
        camera.lookAt(cameraLookAt, cameraUp);
        
        //System.out.println("Position: " + cameraPosition);
        //System.out.println("Up: " + cameraUp);
        if (cameraIsParallel) {
            camera.setParallelProjection(true);
            camera.setFrustum(1.0f, 3000.0f, -75, 75, -75, 75);
        } else {
            camera.setParallelProjection(false);
            camera.setFrustumPerspective(60.0f, getWidth()/getHeight(), 1.0f, 1000.0f);
        }
        camera.update();
        
        
        Matrix4f proj = new Matrix4f();
        Matrix4f view = new Matrix4f();
        proj.set(((AbstractCamera)camera).getProjectionMatrix());
        //System.out.println("PROJ MATRIX: " + proj);
        view.set(((AbstractCamera)camera).getModelViewMatrix());
        //System.out.println("VIEW MATRIX: " + view);
        view.multLocal(proj).multLocal(biasMatrix).transposeLocal();
        //System.out.println("MATRIX: " + view);
        getTexture().getMatrix().set(view);
    }
    
    /**
     * Render the current RenderList into this buffer
     */
    void render(Renderer r) {
        GL gl = GLU.getCurrentGL();
        com.jme.renderer.Renderer jmeRenderer = r.getJMERenderer();
        int width = getWidth();
        int height = getHeight();
        
        synchronized (getRBLock()) {
            if (cameraChanged) {
                camera = getTextureRenderer().getCamera();
                updateCamera();
                cameraChanged = false;
            }
        }
        camera.update();
        getBackgroundColor(bgColor);
        getTextureRenderer().setBackgroundColor(bgColor);
        //System.out.println("Camera loc: " + camera.getLocation());
        //System.out.println("Camera dir: " + camera.getDirection()); 
        //System.out.println("RL: " + getRenderList().size());
        //System.out.println("TL: " + getTextureList().size());
        getTextureRenderer().render(getRenderList(), getTextureList(), true);
        
        /*
        Camera saveCamera = jmeRenderer.getCamera();
        jmeRenderer.setCamera(camera);
        JOGLTextureState.doTextureBind(getTexture().getTextureId(), 0, Texture.Type.TwoDimensional);

        //r.renderScene(getSpatialList());
        r.renderScene(null);
        r.swapAndWait(5000); 
        //gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL.GL_RGBA, 0, 0, width, height, 0);
        jmeRenderer.setCamera(saveCamera);
        */
    }
}
