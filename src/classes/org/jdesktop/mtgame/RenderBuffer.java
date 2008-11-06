/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame;

import java.awt.Canvas;
import java.util.ArrayList;

import com.jme.image.Texture;
import com.jme.renderer.TextureRenderer;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import com.jme.renderer.ColorRGBA;

/**
 * This class encapsultes a rendering surface in mtgame.  It can be used
 * for may different purposes.  It can be used for onscreen rendering, texture
 * rendering, and shadow map rendering.
 * 
 * @author Doug Twilleager
 */
public abstract class RenderBuffer {
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
    }
    
    /**
     * Return an object for locking.
     */
    Object getRBLock() {
        return (spatialList);
    }
    
    /**
     * Check if we are initialized
     */
    boolean isInitialized() {
        return (initialized);
    }
    
    /**
     * Set the initialized flag
     */
    void setInitialized(boolean flag) {
        initialized = flag;
    }
    
    /**
     * Sets the manage render scenes flag.  If true, the application
     * will set which spatials need to be rendered.  If it is false
     * the system will render all known spatials automatically.  The
     * default is false.
     */
    public void setManageRenderScenes(boolean manage) {
        synchronized (getRBLock()) {
            if (manageRenderScenes != manage) {
                manageRenderScenes = manage;
                // TODO: Optimize this - right now it is brute force.
                //setInitialized(false);
            }
        }
    }
    
    /**
     * This adds a spatial to the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void addRenderScene(Spatial s) {
        synchronized (getRBLock()) {
            spatialList.add(s);
            if (manageRenderScenes) {
                //setInitialized(false);
            }
        }
    }
      
    /**
     * This removes a spatial from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removeRenderScene(Spatial s) {
        synchronized (getRBLock()) {
            spatialList.remove(s);
            if (manageRenderScenes) {
                //setInitialized(false);
            }
        }
    }
           
    /**
     * This removes a spatial from the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public void removeRenderScene(int index) {
        synchronized (getRBLock()) {
            spatialList.remove(index);
            if (manageRenderScenes) {
                //setInitialized(false);
            }
        }
    }    
        
    /**
     * This adds a spatial to the list to be rendered.  This is only
     * used when manage render scenes is true.
     */
    public int numRenderScenes() {
        int size = 0;
        synchronized (getRBLock()) {
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
    
    ArrayList getSpatialList() {
        return (spatialList);
    }
    
    ArrayList getTextureList() {
        return (textureList);
    }
    
    ArrayList getRenderList() {
        return (renderList);
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
     * Get the background color
     */
    public ColorRGBA getBackgroundColor() {
        return (backgroundColor);
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
        return (texture);
    }
    
    /**
     * Initialize this RenderBuffer.  This is called from the renderer
     * before the buffer is rendered into.
     */
    abstract void update(DisplaySystem display, Spatial skybox, ArrayList renderComponents);
    
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
    abstract void render(Renderer r);
}
