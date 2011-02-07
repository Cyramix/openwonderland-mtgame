/*
 * Copyright (c) 2011, Open Wonderland Foundation. All rights reserved.
 *
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions
 *    are met:
 *
 *  . Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  . Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  . Neither the name of Open Wonderland Foundation, nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Copyright (c) 2009, Sun Microsystems, Inc. All rights reserved.
 *
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions
 *    are met:
 *
 *  . Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  . Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  . Neither the name of Sun Microsystems, Inc., nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.mtgame;

import com.jme.scene.Node;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.CullState;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import com.jmex.model.collada.ColladaImporter;
import java.util.zip.GZIPInputStream;


/**
 * This is the Config Instance that implements the loading of a Collada file
 * 
 * @author Doug Twilleager
 */
public class ColladaInstance implements ConfigInstance {
    /**
     * The Model for this collada file
     */
    Node model = null;

    /**
     * The Entity for this collada file
     */
    Entity entity = null;

    /**
     * This method is called when the instance is initialized
     */
    public void init(WorldManager wm, ConfigManager cm, String name, Vector3f location, Quaternion rotation, Vector3f scale, String[] args) {
        loadColladaFile(wm, cm, name, args[0]);
        if (model != null) {
            model.setLocalTranslation(location);
            model.setLocalRotation(rotation);
            model.setLocalScale(scale);
        }
    }

    /**
     * This method is called to get the scene graph for this instance
     */
    public Node getSceneGraph() {
        return (model);
    }

    /**
     * This method is called to get the scene graph for this instance
     */
    public Entity getEntity() {
        return (entity);
    }
        /**
     * Load a collada file with the given name - it can be found in the
     * data directory
     */
    void loadColladaFile(WorldManager wm, ConfigManager cm, String name, String colladaFile) {
        InputStream fileStream = null;

        try {
            URL url;
            url = new URL(cm.getDataDirectory() + "/" + colladaFile);
            fileStream = url.openStream();

            // handle gzipped COLLADA files
            if (colladaFile.endsWith(".gz")) {
                fileStream = new GZIPInputStream(fileStream);
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }

        // Now load the model
        ColladaImporter.load(fileStream, "Collada Model (" + name + ")");
        model = ColladaImporter.getModel();
        model.setName("Collada Model (" + name + ")");
        createEntity(wm, "Collada Model (" + name + ")");
    }


    void createEntity(WorldManager wm, String name) {
        JMECollisionSystem cs = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        model.setRenderState(buf);

        CullState culls = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        culls.setCullFace(CullState.Face.Back);
        model.setRenderState(culls);

        entity = new Entity(name);
        RenderComponent sc = wm.getRenderManager().createRenderComponent(model);
        JMECollisionComponent cc = cs.createCollisionComponent(model);
        entity.addComponent(JMECollisionComponent.class, cc);
        entity.addComponent(RenderComponent.class, sc);
    }
}
