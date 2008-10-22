/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.mtgame.test;

import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.FPSCameraProcessor;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import com.jme.light.LightNode;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.Box;
import com.jme.bounding.BoundingBox;
import com.jme.math.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JLabel;

/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class WorldTest {
    /**
     * The constructor
     */
    
    /**
     * The CameraNode
     */
    private CameraNode cameraNode = null;
    
    /**
     * The desired frame rate
     */
    private int desiredFrameRate = 60;
    
    /**
     * The width and height of our 3D window
     */
    private int width = 800;
    private int height = 600;
    private float aspect = 800.0f/600.0f;
    
    public WorldTest(String[] args) {
        WorldManager wm = new WorldManager("TestWorld");
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        createUI(wm);  
        createGlobalLight(wm);
        createTestSpaces(wm);
        createTestEntities(wm);
        createCameraEntity(wm);        
    }
                  
    private void createGlobalLight(WorldManager wm) {
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setEnabled(true);
        LightNode ln = new LightNode();
        ln.setLight(light);
        ln.setLocalTranslation(new Vector3f(100, 100, 100));
        wm.getRenderManager().addLight(ln); 
    }
    
    
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 1.0f, 1000.0f, true);
        camera.addComponent(CameraComponent.class, cc);

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent eventListener = (AWTInputComponent)wm.getInputManager().createInputComponent(eventMask);
        FPSCameraProcessor eventProcessor = new FPSCameraProcessor(eventListener, cameraNode, wm, camera);
        eventProcessor.setRunInRenderer(true);
        camera.addComponent(ProcessorComponent.class, eventProcessor);   
        wm.addEntity(camera);         
    }
    
    
    private void createTestEntities(WorldManager wm) {
        ColorRGBA color = new ColorRGBA();

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // The center teapot
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createTeapotEntity("Center ", 0.0f, 0.0f, 0.0f, buf, color, wm);

        color.r = 0.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createTeapotEntity("North ", 0.0f, 0.0f, 100.0f, buf, color, wm);
        
        color.r = 1.0f; color.g = 0.0f; color.b = 0.0f; color.a = 1.0f;
        createTeapotEntity("South ", 0.0f, 0.0f, -100.0f, buf, color, wm);
        
        color.r = 1.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createTeapotEntity("East ", 100.0f, 0.0f, 0.0f, buf, color, wm);
        
        color.r = 0.0f; color.g = 1.0f; color.b = 1.0f; color.a = 1.0f;
        createTeapotEntity("West ", -100.0f, 0.0f, 0.0f, buf, color, wm);        
    }
    
    public void createTeapotEntity(String name, float xoff, float yoff, float zoff, 
            ZBufferState buf, ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;
        
        // The center teapot
        Node node = new Node();
        Teapot teapot = new Teapot();
        teapot.resetData();
        node.attachChild(teapot);

        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        node.setRenderState(buf);
        node.setLocalTranslation(xoff, yoff, zoff);

        Entity te = new Entity(name + "Teapot");
        RenderComponent sc = wm.getRenderManager().createRenderComponent(node);
        te.addComponent(RenderComponent.class, sc);
        
        RotationProcessor rp = new RotationProcessor(name + "Teapot Rotator", wm, 
                node, (float) (6.0f * Math.PI / 180.0f));
        //rp.setRunInRenderer(true);
        te.addComponent(ProcessorComponent.class, rp);
        wm.addEntity(te);        
    }

    private void createTestSpaces(WorldManager wm) {
        ColorRGBA color = new ColorRGBA();
        Vector3f center = new Vector3f();

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // First create the geometry
        center.x = 0.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createSpace("Center ", center, buf, color, wm);
        
        center.x = 0.0f; center.y = 25.0f; center.z = 98.0f;
        color.r = 0.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createSpace("North ", center, buf, color, wm);

        center.x = 0.0f; center.y = 25.0f; center.z = -98.0f;
        color.r = 1.0f; color.g = 0.0f; color.b = 0.0f; color.a = 1.0f;
        createSpace("South ", center, buf, color, wm);

        center.x = 98.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 1.0f; color.g = 1.0f; color.b = 0.0f; color.a = 1.0f;
        createSpace("East ", center, buf, color, wm);

        center.x = -98.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 0.0f; color.g = 1.0f; color.b = 1.0f; color.a = 1.0f;
        createSpace("West ", center, buf, color, wm);
    }
    
    public void createSpace(String name, Vector3f center, ZBufferState buf,
            ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;

        Box cube = null;
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        
        // Create the root for the space
        Node node = new Node();
        
        // Now the walls
        Box box = new Box(name + "Box", center, 50.0f, 50.0f, 50.0f);
        node.attachChild(box);
       
        // Now some rotating cubes - all confined within the space (not entities)
        createCube(center, -25.0f, 15.0f,  25.0f, pcc, node, wm);
        createCube(center,  25.0f, 15.0f,  25.0f, pcc, node, wm);
        createCube(center,  25.0f, 15.0f, -25.0f, pcc, node, wm);
        createCube(center, -25.0f, 15.0f, -25.0f, pcc, node, wm);
     
        // Add bounds and state for the whole space
        BoundingBox bbox = new BoundingBox(center, 50.0f, 50.0f, 50.0f);
        node.setModelBound(bbox);
        node.setRenderState(buf);
        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        
        // Create a scene component for it
        RenderComponent sc = wm.getRenderManager().createRenderComponent(node);
        
        // Finally, create the space and add it.
        Entity e = new Entity(name + "Space");
        e.addComponent(ProcessorCollectionComponent.class, pcc);
        e.addComponent(RenderComponent.class, sc);
        wm.addEntity(e);        
    }
    
    private void createCube(Vector3f center, float xoff, float yoff, float zoff, 
            ProcessorCollectionComponent pcc, Node parent, WorldManager wm) {
        Vector3f cubeCenter = new Vector3f();
        Vector3f c = new Vector3f();
        
        cubeCenter.x = center.x + xoff;
        cubeCenter.y = center.y + yoff;
        cubeCenter.z = center.z + zoff;
        Box cube = new Box("Space Cube", c, 5.0f, 5.0f, 5.0f);
        Node cubeNode = new Node();
        cubeNode.setLocalTranslation(cubeCenter);
        cubeNode.attachChild(cube);  
        parent.attachChild(cubeNode);
        
        RotationProcessor rp = new RotationProcessor("Cube Rotator", wm, cubeNode, 
                (float) (6.0f * Math.PI / 180.0f));
        //rp.setRunInRenderer(true);
        pcc.addProcessor(rp);
    }
    
    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");        
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);
        
        return (cameraSG);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WorldTest worldTest = new WorldTest(args);
        
    }
    
    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
            }
        }
    }
    
    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {             
        SwingFrame frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }
    
    class SwingFrame extends JFrame implements FrameRateListener {

        JPanel contentPane;
        JPanel mainPanel = new JPanel();
        Canvas canvas = null;
        JLabel fpsLabel = new JLabel("FPS: ");

        // Construct the frame
        public SwingFrame(WorldManager wm) {
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            mainPanel.setLayout(new GridBagLayout());
            setTitle("DUCK!");

            // make the canvas:
            canvas = wm.getRenderManager().createCanvas(width, height);
            canvas.setVisible(true);
            wm.getRenderManager().setFrameRateListener(this, 100);
            wm.getRenderManager().setCurrentCanvas(canvas);

            contentPane.add(mainPanel, BorderLayout.NORTH);
            mainPanel.add(fpsLabel,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0,
                    5), 0, 0));

            canvas.setBounds(0, 0, width, height);
            contentPane.add(canvas, BorderLayout.CENTER);

            pack();
        }
        
        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }
    }

}
