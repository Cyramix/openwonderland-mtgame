/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame;

import java.awt.Canvas;
import java.util.ArrayList;

import com.jme.image.Texture;
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
    public void setTexture(Texture t) {
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
    void update(DisplaySystem display, Spatial skybox, ArrayList renderComponents) {
        GL gl = GLU.getCurrentGL();
        
        if (target == Target.ONSCREEN) {
            // Nothing to do
            return;
        }
        
        synchronized (spatialList) {
            if (!initialized) {
                createTextureRenderer(display);
                assignTextureId(gl);
                allocateTextureData(gl);
                setupState(display);
                updateRenderList(skybox, renderComponents);
                initialized = true;
            }
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
                tRtarget = TextureRenderer.Target.Texture2D;
                break;
            case TEXTURE_CUBEMAP:
                tRtarget = TextureRenderer.Target.TextureCubeMap;
                break;
        }
        renderer = display.createTextureRenderer(getWidth(), getHeight(), tRtarget);
        renderer.setBackgroundColor(backgroundColor);
    }
    
    /**
     * Manage the texture id.
     */
    void assignTextureId(GL gl) {
        IntBuffer ibuf = BufferUtils.createIntBuffer(1);

        if (texture.getTextureId() != 0) {
            ibuf.put(texture.getTextureId());
            gl.glDeleteTextures(ibuf.limit(), ibuf); // TODO Check <size>
            ibuf.clear();
        }

        // Create the texture
        gl.glGenTextures(ibuf.limit(), ibuf); // TODO Check <size>
        texture.setTextureId(ibuf.get(0));
        TextureManager.registerForCleanup(texture.getTextureKey(), texture.getTextureId());

        JOGLTextureState.doTextureBind(texture.getTextureId(), 0,
                Texture.Type.TwoDimensional);
    }
    
        
    /**
     * Allocate the texture data, based upon what we are doing.
     */
    void allocateTextureData(GL gl) {
        int components = GL.GL_RGBA8;
	int format = GL.GL_RGBA;
	int dataType = GL.GL_UNSIGNED_BYTE;
        Texture.RenderToTextureType rttType = Texture.RenderToTextureType.RGBA;
        int glTarget = GL.GL_TEXTURE_2D;
        
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
        texture.setRenderToTextureType(rttType);
        gl.glTexImage2D(glTarget, 0, components, width, height, 0,
		    format, dataType, null);
        
        // Initialize mipmapping for this texture, if requested
        if (texture.getMinificationFilter().usesMipMapLevels()) {
            gl.glGenerateMipmapEXT(glTarget);
        }

    }
    
    /**
     * Setup some state on the texture
     */
    void setupState(DisplaySystem display) {   
        // Setup filtering and wrap
        RenderContext<?> context = display.getCurrentContext();
        TextureStateRecord record = (TextureStateRecord) context
                .getStateRecord(RenderState.RS_TEXTURE);
        TextureRecord texRecord = record.getTextureRecord(texture.getTextureId(), texture.getType());

        JOGLTextureState.applyFilter(texture, texRecord, 0, record);
        JOGLTextureState.applyWrap(texture, texRecord, 0, record);
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
    void render() {
        renderer.setCamera(cameraComponent.getCamera());
        cameraComponent.getCamera().update();
        renderer.render(renderList, textureList, true);
    }
}
