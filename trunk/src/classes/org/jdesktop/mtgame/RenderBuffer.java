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
public class RenderBuffer {
    /**
     * The types of rendering supported
     */
    public enum Target {
        TEXTURE_1D,
        TEXTURE_2D,
        TEXTURE_CUBEMAP,
        SHADOWMAP,
        ONSCREEN
    }
    
    /**
     * The target for this render buffer.
     */
    private Target target = Target.ONSCREEN;
    
    /**
     * The width and height of this buffer
     */
    private int height = 0;
    private int width = 0;
    
    /**
     * The Camera Component used to render into this buffer
     */
    private CameraComponent cameraComponent = null;
    
    /**
     * The Canvas used for ONSCREEN rendering
     */
    private Canvas canvas = null;
    
    /**
     * The texture data for offscreen rendering
     */
    private Texture texture = null;
    private TextureRenderer renderer = null;
    
    /**
     * The cube map texture
     */
    private TextureCubeMap cubeMap = null;
    
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
    
    /**
     * A boolean indicating that this RenderBuffer has been initialized
     */
    private boolean initialized = false;
    
    /**
     * A flag that indicates whether we are rendering all scenes or
     * managing manually.
     */
    private boolean manageRenderScenes = false;
     
    /**
     * The list of Spatials to render into this buffer
     * This list is only accessed if manage render scenes is true.
     */
    private ArrayList<Spatial> spatialList = new ArrayList();
    
    /**
     * The list of Spatials to render into this buffer
     */
    private ArrayList<Spatial> renderList = new ArrayList();
    
    /**
     * The list of textures to render into
     */
    private ArrayList<Texture> textureList = new ArrayList();
    
    /**
     * The background color
     */
    private ColorRGBA backgroundColor = new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f);
    
    /**
     * The constructor
     */
    public RenderBuffer(Target target, int width, int height) {
        this.target = target;
        this.height = height;
        this.width = width;
        setTexture(new Texture2D());
        if (target == Target.TEXTURE_CUBEMAP) {
            cubeMap = new TextureCubeMap();
        }
    }
    
    /**
     * Sets the manage render scenes flag.  If true, the application
     * will set which spatials need to be rendered.  If it is false
     * the system will render all known spatials automatically.  The
     * default is false.
     */
    public void setManageRenderScenes(boolean manage) {
        synchronized (spatialList) {
            if (manageRenderScenes != manage) {
                manageRenderScenes = manage;
                // TODO: Optimize this - right now it is brute force.
                initialized = false;
            }
        }
    }
    
    /**
     * This adds a spatial to the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void addRenderScene(Spatial s) {
        synchronized (spatialList) {
            spatialList.add(s);
            if (manageRenderScenes) {
                initialized = false;
            }
        }
    }
      
    /**
     * This removes a spatial from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removeRenderScene(Spatial s) {
        synchronized (spatialList) {
            spatialList.remove(s);
            if (manageRenderScenes) {
                initialized = false;
            }
        }
    }
           
    /**
     * This removes a spatial from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removeRenderScene(int index) {
        synchronized (spatialList) {
            spatialList.remove(index);
            if (manageRenderScenes) {
                initialized = false;
            }
        }
    }    
        
    /**
     * This adds a spatial to the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public int numRenderScenes() {
        int size = 0;
        synchronized (spatialList) {
            size = spatialList.size();
        }
        return (size);
    }
    
    /**
     * Dynamically set the width of this render target
     */
    public void setWidth(int width) {
        this.width = width;
        // TODO: actually change the surface
    }
    
    /**
     * Get the width
     */
    public int getWidth() {
        return (width);
    }
      
    /**
     * Get the height
     */
    public int getHeight() {
        return (height);
    }
    
    /**
     * Dynamically set the height of this render target
     */
    public void setHeight(int height) {
        this.height = height;
        // TODO: actually change the surface
    }
    
    /**
     * Set the canvas in onscreen mode
     */
    void setCanvas(Canvas c) {
        canvas = c;
    }
    
    /**
     * Get the onscreen canvas.
     */
    public Canvas getCanvas() {
        return (canvas);
    }
    
    /**
     * Get the target
     */
    public Target getTarget() {
        return (target);
    }
    
    /**
     * Set the CameraComponent
     */
    public void setCameraComponent(CameraComponent cc) {
        cameraComponent = cc;
        // TODO: propogate dynamic change
    }
    
    /**
     * Get the current CameraComponent
     */
    public CameraComponent getCameraComponent() {
        return (cameraComponent);
    }
    
    /**
     * Set the texture renderer
     */
    void setTextureRenderer(TextureRenderer r) {
        renderer = r;
    }
    
    /**
     * Get the texture renderer
     */
    TextureRenderer getTextureRenderer() {
        return (renderer);
    }
    
    /**
     * Set the target texture
     */
    void setTexture(Texture t) {
        texture = t;
        if (textureList.size() == 0) {
            textureList.add(t);
        } else {
            textureList.set(0, t);
        }
    }
    
    /**
     * Get the texture used for offscreen rendering
     */
    public Texture getTexture() {
        Texture t = null;
        
        if (target == Target.TEXTURE_2D) {
            t = texture;
        } else if (target == Target.TEXTURE_CUBEMAP) {
            t = cubeMap;
        }
        return (t);
    }
    
    /**
     * Initialize this RenderBuffer.  This is called from the renderer
     * before the buffer is rendered into.
     */
    void update(DisplaySystem display, Spatial skybox, ArrayList renderComponents) {
        GL gl = GLU.getCurrentGL();
        
        if (target == Target.ONSCREEN) {
            // Nothing to do
            return;
        }
        
        synchronized (spatialList) {
            if (!initialized) {
                createTextureRenderer(display);
                createTextureObjects(gl, display);
                updateRenderList(skybox, renderComponents);
                initialized = true;
            }
        }
    }
    
    /**
     * Create the jME texture objects, and prep them for rendering
     */
    private void createTextureObjects(GL gl, DisplaySystem display) {
        // First do the common render target
        if (target == Target.TEXTURE_2D) {
            assignTextureId(gl, texture, Texture.Type.TwoDimensional);
            allocateTextureData(gl, texture, Texture.Type.TwoDimensional);
            setupState(display, texture);
        } else if (target == Target.TEXTURE_CUBEMAP) {
            assignTextureId(gl, cubeMap, Texture.Type.CubeMap);
            allocateTextureData(gl, cubeMap, Texture.Type.CubeMap);
            setupState(display, cubeMap);
        }
    }
        
    private void createTextureRenderer(DisplaySystem display) {
        TextureRenderer.Target tRtarget = TextureRenderer.Target.Texture2D;
        
        switch (target) {
            case TEXTURE_1D:
                tRtarget = TextureRenderer.Target.Texture1D;
                break;
            case TEXTURE_2D:
            case SHADOWMAP:
            case TEXTURE_CUBEMAP:
                tRtarget = TextureRenderer.Target.Texture2D;
                break;
        }
        renderer = display.createTextureRenderer(getWidth(), getHeight(), tRtarget);
        renderer.setBackgroundColor(backgroundColor);
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
        Texture.RenderToTextureType rttType = Texture.RenderToTextureType.RGBA;
        
        switch (target) {
            case TEXTURE_1D:
                break;
            case TEXTURE_2D:
                // The dafaults work for this case
                break;
            case SHADOWMAP:
                break;
            case TEXTURE_CUBEMAP:
                break;
        }
        t.setRenderToTextureType(rttType);
        
        if (type == Texture.Type.TwoDimensional) {
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, components, width, height, 0,
                	    format, dataType, null);
            if (t.getMinificationFilter().usesMipMapLevels()) {
                gl.glGenerateMipmapEXT(GL.GL_TEXTURE_2D);
            }
        } else if (type == Texture.Type.CubeMap) {
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
     * Update the renderlist
     */
    void updateRenderList(Spatial skybox, ArrayList renderComponents) {
        renderList.clear();
        if (manageRenderScenes) {
            for (int i = 0; i < spatialList.size(); i++) {
                renderList.add(spatialList.get(i));
            }
        } else {
            if (skybox != null) {
                renderList.add(skybox);
            }
            for (int i = 0; i < renderComponents.size(); i++) {
                RenderComponent rc = (RenderComponent) renderComponents.get(i);
                renderList.add(rc.getSceneRoot());
            }
        }
    }
    
    /**
     * Render the current RenderList into this buffer
     */
    void render(Renderer r) {
        GL gl = GLU.getCurrentGL();
        com.jme.renderer.Renderer jmeRenderer = r.getJMERenderer();
        
        if (target == Target.TEXTURE_2D) {
            renderer.setCamera(cameraComponent.getCamera());
            cameraComponent.getCamera().update();
            renderer.render(renderList, textureList, true);
        } else if (target == Target.TEXTURE_CUBEMAP) {
            Camera ccCamera = cameraComponent.getCamera();
            ccCamera.update();
               
            if (cubeMapCamera == null) {
                cubeMapCamera = r.createJMECamera(width, height);
                cubeMapCamera.setFrustumPerspective(cameraComponent.getFieldOfView(), cameraComponent.getAspectRatio(),
                                                    cameraComponent.getNearClipDistance(), cameraComponent.getFarClipDistance());
            }       
            Camera saveCamera = jmeRenderer.getCamera();
            jmeRenderer.setCamera(cubeMapCamera);
            cubeMapCamera.setLocation(ccCamera.getLocation());
            JOGLTextureState.doTextureBind(cubeMap.getTextureId(), 0, Texture.Type.CubeMap);
            
            // Render Negative X
            cubeMapCamera.setDirection(negativeX);
            cubeMapCamera.update();
            r.renderScene(spatialList);
            gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL.GL_RGBA, 0, 0, width, height, 0);

            // Render Positive X
            cubeMapCamera.setDirection(positiveX);
            cubeMapCamera.update();
            r.renderScene(spatialList);
            gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL.GL_RGBA, 0, 0, width, height, 0);

            // Render Negative Y
            cubeMapCamera.setDirection(negativeY);
            cubeMapCamera.update();
            r.renderScene(spatialList);
            gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL.GL_RGBA, 0, 0, width, height, 0);

            // Render Positive Y
            cubeMapCamera.setDirection(positiveY);
            cubeMapCamera.update();
            r.renderScene(spatialList);
            gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL.GL_RGBA, 0, 0, width, height, 0);

            // Render Negative Z
            cubeMapCamera.setDirection(negativeZ);
            cubeMapCamera.update();
            r.renderScene(spatialList);
            gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL.GL_RGBA, 0, 0, width, height, 0);

            // Render Positive Z
            cubeMapCamera.setDirection(positiveZ);
            cubeMapCamera.update();
            r.renderScene(spatialList);
            gl.glCopyTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL.GL_RGBA, 0, 0, width, height, 0);
            
            jmeRenderer.setCamera(saveCamera);
        }
    }
}
