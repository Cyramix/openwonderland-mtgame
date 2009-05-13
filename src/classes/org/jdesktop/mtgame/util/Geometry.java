/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.mtgame.util;

import org.jdesktop.mtgame.*;

import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.SharedMesh;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import com.jme.util.geom.BufferUtils;
import com.jme.bounding.BoundingBox;

/**
 * This class contains geometry utilities
 * 
 * @author Doug Twilleager
 */
public class Geometry {
    private static Object utilLock = new Object();

    /**
     * The constructor
     */
    public Geometry() {
    }

    public static Node explodeIntoSpatials(WorldManager wm, TriMesh mesh) {
        synchronized (utilLock) {
            TriMesh srcMesh = mesh;
            SharedMesh sharedMesh = null;

            if (mesh instanceof SharedMesh) {
                sharedMesh = (SharedMesh)mesh;
                srcMesh = sharedMesh.getTarget();
            }

            FloatBuffer normals = srcMesh.getNormalBuffer();
            FloatBuffer verts = srcMesh.getVertexBuffer();
            FloatBuffer colors = srcMesh.getColorBuffer();
            FloatBuffer tangents = srcMesh.getTangentBuffer();
            FloatBuffer binormals = srcMesh.getBinormalBuffer();
            IntBuffer indicies = srcMesh.getIndexBuffer();
            ArrayList texcs = srcMesh.getTextureCoords();

            indicies.rewind();
            Node node = new Node();
            for (int i=0; i<mesh.getTriangleCount(); i++) {
                int i1 = indicies.get();
                int i2 = indicies.get();
                int i3 = indicies.get();
                node.attachChild(createTriMesh(wm, srcMesh, i1, i2, i3, verts, normals, colors,
                                               texcs, tangents, binormals, sharedMesh));
            }
            return (node);
        }

    }

    private static TriMesh createTriMesh(WorldManager wm, TriMesh srcMesh, int oi1, int oi2, int oi3,
                                         FloatBuffer verts, FloatBuffer normals, FloatBuffer colors,
                                         ArrayList texcs, FloatBuffer tangents, FloatBuffer binormals,
                                         SharedMesh sharedMesh) {
        TriMesh newMesh = null;
        FloatBuffer v = null;
        FloatBuffer n = null;
        FloatBuffer c = null;
        TexCoords tc = null;
        FloatBuffer tan = null;
        FloatBuffer bin = null;

        if (verts != null) {
            float[] data = new float[3*3];
            int i1 = oi1*3;
            int i2 = oi2*3;
            int i3 = oi3*3;
            data[0] = verts.get(i1); data[1] = verts.get(i1+1); data[2] = verts.get(i1+2);
            data[3] = verts.get(i2); data[4] = verts.get(i2+1); data[5] = verts.get(i2+2);
            data[6] = verts.get(i3); data[7] = verts.get(i3+1); data[8] = verts.get(i3+2);
            v = BufferUtils.createFloatBuffer(data);
        }

        if (normals != null) {
            float[] data = new float[3*3];
            int i1 = oi1*3;
            int i2 = oi2*3;
            int i3 = oi3*3;
            data[0] = normals.get(i1); data[1] = normals.get(i1+1); data[2] = normals.get(i1+2);
            data[3] = normals.get(i2); data[4] = normals.get(i2+1); data[5] = normals.get(i2+2);
            data[6] = normals.get(i3); data[7] = normals.get(i3+1); data[8] = normals.get(i3+2);
            n = BufferUtils.createFloatBuffer(data);
        }

        if (colors != null) {
            float[] data = new float[4*3];
            int i1 = oi1*3;
            int i2 = oi2*3;
            int i3 = oi3*3;
            data[0] = colors.get(i1); data[1] = colors.get(i1+1); data[2] = colors.get(i1+2); data[3] = colors.get(i1+3);
            data[4] = colors.get(i2); data[5] = colors.get(i2+1); data[6] = colors.get(i2+2); data[7] = colors.get(i2+3);
            data[8] = colors.get(i3); data[9] = colors.get(i3+1); data[10] = colors.get(i3+2); data[11] = colors.get(i3+3);
            c = BufferUtils.createFloatBuffer(data);
        }

        if (texcs.size() != 0) {
            // Just one for now
            TexCoords tcs = (TexCoords)texcs.get(0);
            float[] data = new float[tcs.perVert*3];
            int i1 = oi1*tcs.perVert;
            int i2 = oi2*tcs.perVert;
            int i3 = oi3*tcs.perVert;
            int index = 0;
            for (int i=0; i<tcs.perVert; i++) {
                data[index++] = tcs.coords.get(i1+i);
            }
            for (int i=0; i<tcs.perVert; i++) {
                data[index++] = tcs.coords.get(i2+i);
            }
            for (int i=0; i<tcs.perVert; i++) {
                data[index++] = tcs.coords.get(i3+i);
            }
            FloatBuffer tcb = BufferUtils.createFloatBuffer(data);
            tc = new TexCoords(tcb, tcs.perVert);
        }

        int[] ind = new int[3];
        ind[0] = 0; ind[1] = 1; ind[2] = 2;
        IntBuffer ib = BufferUtils.createIntBuffer(ind);

        newMesh = new TriMesh("", v, n, c, tc, ib);

        if (tangents != null) {
            float[] data = new float[3*3];
            int i1 = oi1*3;
            int i2 = oi2*3;
            int i3 = oi3*3;
            data[0] = tangents.get(i1); data[1] = tangents.get(i1+1); data[2] = tangents.get(i1+2);
            data[3] = tangents.get(i2); data[4] = tangents.get(i2+1); data[5] = tangents.get(i2+2);
            data[6] = tangents.get(i3); data[7] = tangents.get(i3+1); data[8] = tangents.get(i3+2);
            tan = BufferUtils.createFloatBuffer(data);
            newMesh.setTangentBuffer(tan);
        }

        if (binormals != null) {
            float[] data = new float[3*3];
            int i1 = oi1*3;
            int i2 = oi2*3;
            int i3 = oi3*3;
            data[0] = binormals.get(i1); data[1] = binormals.get(i1+1); data[2] = binormals.get(i1+2);
            data[3] = binormals.get(i2); data[4] = binormals.get(i2+1); data[5] = binormals.get(i2+2);
            data[6] = binormals.get(i3); data[7] = binormals.get(i3+1); data[8] = binormals.get(i3+2);
            bin = BufferUtils.createFloatBuffer(data);
            newMesh.setBinormalBuffer(bin);
        }

        // Copy render states
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Blend));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Clip));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.ColorMask));
//        CullState cs = (CullState)sharedMesh.getRenderState(RenderState.StateType.Cull);
//        if (cs == null) {
//            System.out.println("======================== CS");
//            cs = (CullState)wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
//        }
//        cs.setCullFace(CullState.Face.None);
//        cs.setEnabled(true);
//        newMesh.setRenderState(cs);
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Cull));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Fog));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.FragmentProgram));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.GLSLShaderObjects));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Light));
//        MaterialState ms = (MaterialState)sharedMesh.getRenderState(RenderState.StateType.Material);
//        if (ms == null) {
//            System.out.println("======================== MS");
//            ms = (MaterialState)wm.getRenderManager().createRendererState(RenderState.StateType.Material);
//        }
//        ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
//        ms.setEnabled(true);
//        newMesh.setRenderState(ms);
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Material));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Shade));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Stencil));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Texture));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.VertexProgram));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.Wireframe));
        newMesh.setRenderState(sharedMesh.getRenderState(RenderState.StateType.ZBuffer));

        newMesh.setLocalScale(sharedMesh.getLocalScale());
        newMesh.setLocalRotation(sharedMesh.getLocalRotation());
        newMesh.setLocalTranslation(sharedMesh.getLocalTranslation());

        // Finally, the bounds
        BoundingBox bbox = new BoundingBox();
        bbox.computeFromTris(ind, newMesh, 0, 2);
        newMesh.setModelBound(bbox);
        return (newMesh);
    }
}
