/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.mtgame.util;

import com.jme.math.TransformMatrix;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.state.RenderState;
import com.jme.util.geom.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * Utility class for optimizig the scene graph.
 *
 * @author paulby
 */
public class GraphOptimizer {

    private HashMap<RenderStateSet, LinkedList<SharedMesh>> sharedMeshes = new HashMap();
    private final boolean print = false;

    /**
     * Process the model, combining all SharedMeshes that share the same set
     * of RenderStates into a single TriMesh.
     *
     * This method is not thread safe, use seperate instances of GraphOptimizer if
     * calling from threaded code.
     * @param model
     */
    public void removeSharedMeshes(Node model) {
        sharedMeshes.clear();

        // Update all the world transforms
        model.updateGeometricState(0, true);

        GraphMetrics metrics = new GraphMetrics();

        traverse(model, 0, metrics);
//        System.err.println("Initial Metrics "+metrics.getReport());

        for(Entry<RenderStateSet, LinkedList<SharedMesh>> entry : sharedMeshes.entrySet()) {
            combineMeshes(model, entry.getKey(), entry.getValue());
        }
        
        removeDeadNodes(model);

//        metrics.reset();
//        traverse(model, 0, metrics);
//        System.err.println("Final Metrics "+metrics.getReport());

        sharedMeshes.clear();
    }
    
    /**
     * Remove any nodes that no longer have children
     * @param node
     */
    private void removeDeadNodes(Spatial node) {
        if (node instanceof Node) {
            if (((Node)node).getQuantity()!=0) {
                Spatial[] children = ((Node)node).getChildren().toArray(new Spatial[((Node)node).getQuantity()]);
                if (children!=null) {
                    for(Spatial child : children)
                        removeDeadNodes(child);

                    if (((Node)node).getQuantity()==0)
                        node.removeFromParent();
                } else {
                    node.removeFromParent();
                }
            } else {
                node.removeFromParent();
            }
        }
    }

    private void traverse(Spatial n, int depth, GraphMetrics metrics) {
        if (n==null)
            return;
        metrics.spatialCount++;


        if (print) {
            for(int i=0; i<depth; i++)
                System.err.print("  ");
            System.err.print(n.getClass().getName()+"  ");
        }
        if (n instanceof SharedMesh) {
            RenderStateSet stateSet = gatherRenderStates(n);
            LinkedList meshList = sharedMeshes.get(stateSet);
            if (meshList==null) {
                meshList = new LinkedList();
                sharedMeshes.put(stateSet, meshList);
            }
            meshList.add((SharedMesh)n);
            metrics.sharedMeshCount++;
            if (print) {
                System.err.print(" "+n.getWorldTranslation());
            }
        } else if (n instanceof Geometry) {
            metrics.geometryCount++;
        }

        if (print) {
            System.err.println();
        }

        if (n instanceof Node) {
            List<Spatial> children = ((Node)n).getChildren();
            if (children!=null)
                for(Spatial child : children)
                    traverse(child, depth+1, metrics);

        }
    }

    /**
     * Given a leaf spatial determine all the renderstates that apply to that
     * leaf by traversing up the scene graph gathering all parent render states.
     * @param leaf
     * @return
     */
    private RenderStateSet gatherRenderStates(Spatial leaf) {
        RenderStateSet stateSet = new RenderStateSet();
        Deque<Spatial> stack = new LinkedList();

        Spatial n = leaf;
        while(n!=null) {
            stack.push(n);
            n = n.getParent();
        }

        while(stack.size()!=0) {
            n = stack.pop();
            getRenderStates(n, stateSet);
        }

        return stateSet;
    }

    /**
     * Get all the renderstates that are applied directly to the spatial
     * @param n
     * @param stateSet
     */
    private void getRenderStates(Spatial n, RenderStateSet stateSet) {
        for (int i = 0; i < RenderState.StateType.values().length; ++i) { // No more iterator creation
            RenderState.StateType type = RenderState.StateType.values()[i];
            stateSet.addRenderState(n.getRenderState(type));
        }
    }

    private void combineMeshes(Node root, RenderStateSet stateSet, LinkedList<SharedMesh> meshes) {
        TriMesh firstMeshT = meshes.get(0).getTarget();
        TriMesh firstMesh = meshes.get(0);
        if (firstMeshT.getMode()!=TriMesh.Mode.Triangles) {
            // Only handle Trianlges (not strips or fans)
            return;
        }

        boolean hasTangent = (firstMeshT.getTangentBuffer()!=null);
        boolean hasFog = (firstMeshT.getFogBuffer()!=null);
        boolean hasColor = (firstMeshT.getColorBuffer()!=null);
        int texUnitCount = firstMeshT.getNumberOfUnits();
        boolean hasTexCoords = (firstMeshT.getTextureCoords().size()>0 && firstMeshT.getTextureCoords().get(0)!=null);

        if (hasTangent) {
            Logger.getAnonymousLogger().warning("Unable to optimize SharedMesh, it uses Tangents. Please file a bug and include an example model");
            return;
        }

        if (hasFog) {
            Logger.getAnonymousLogger().warning("Unable to optimize SharedMesh, it uses Fog. Please file a bug and include an example model");
            return;
        }

        if (texUnitCount>1) {
            Logger.getAnonymousLogger().warning("Unable to optimize SharedMesh, it uses multiple TextureUnits. Please file a bug and include an example model");
            return;
        }

        ArrayList<float[]> vertices = new ArrayList();
        ArrayList<float[]> normals = new ArrayList();
        ArrayList<float[]> colors = new ArrayList();
        ArrayList<int[]> indicies = new ArrayList();
//        ArrayList<ArrayList<float[]>> texCoords = new ArrayList();
        ArrayList<float[]> texCoords = new ArrayList();
        ArrayList<TransformMatrix> transform = new ArrayList();

        int size = 0;
        int tcSize = 0;
        for(SharedMesh mesh : meshes) {
            mesh.getVertexBuffer().rewind();
            float[] v = new float[mesh.getVertexBuffer().capacity()];
            size += v.length;
            mesh.getVertexBuffer().get(v);
            vertices.add(v);

            float[] n = new float[v.length];
            mesh.getNormalBuffer().rewind();
            mesh.getNormalBuffer().get(n);
            normals.add(n);

            if (hasColor) {
                float[] c = new float[v.length];
                mesh.getColorBuffer().rewind();
                mesh.getColorBuffer().get(c);
                colors.add(c);
            }

            int[] ind = new int[v.length/3];
            mesh.getIndexBuffer().rewind();
            mesh.getIndexBuffer().get(ind);
            indicies.add(ind);

            if (hasTexCoords) {
                ArrayList<TexCoords> tcList = mesh.getTextureCoords();

                TexCoords tc = tcList.get(0);
                if (tc!=null) {
                    float[] tcf = new float[tc.coords.capacity()]; // Maybe 2D or 3D coords so use capacity()
                    tc.coords.rewind();
                    tc.coords.get(tcf);
                    texCoords.add(tcf);
                    tcSize += tcf.length;
                } 
            }

//            for(int unit=0; unit<texUnitCount; unit++) {
//                TexCoords tc = tcList.get(unit);
//                System.err.println("Got TC "+tc+" for unit "+unit);
//                if (tc!=null) {
//                    tc.coords.rewind();
//                    ArrayList<float[]> l = texCoords.get(unit);
//                    if (l==null) {
//                        l = new ArrayList();
//                        texCoords.set(unit, l);
//                    }
//                    float[] tcF = new float[v.length];
//                    tc.coords.get(tcF);
//                    l.add(tcF);
//                }
//            }

            TransformMatrix tm = new TransformMatrix(mesh.getWorldRotation(), mesh.getWorldTranslation());
            tm.setScale(mesh.getWorldScale());
            transform.add(tm);

            mesh.removeFromParent();
        }

        FloatBuffer newVertexBuf = BufferUtils.createFloatBuffer(size);
        FloatBuffer newNormalsBuf = BufferUtils.createFloatBuffer(size);
        IntBuffer newIndexBuf = BufferUtils.createIntBuffer(size/3);
        FloatBuffer newColorBuf=null;
        FloatBuffer newTexCoordsBuf=null;
        
        if (hasColor)
            newColorBuf = BufferUtils.createColorBuffer(size);

        if (hasTexCoords)
            newTexCoordsBuf = BufferUtils.createFloatBuffer(tcSize);
//        ArrayList<FloatBuffer> newTexCoords = new ArrayList();
//        for(int i=0; i<texCoords.size(); i++)
//            newTexCoords.add(BufferUtils.createFloatBuffer(size));

        Vector3f v3f = new Vector3f();  // tmp variable
        TransformMatrix trans;

        int offset = 0;
        for(int i=0; i<vertices.size(); i++) {
            trans = transform.get(i);
            int length = vertices.get(i).length;
            float[] verts = vertices.get(i);
            for(int vi = 0; vi<verts.length; vi+=3) {
                v3f.set(verts[vi], verts[vi+1], verts[vi+2]);

                trans.multPoint(v3f);       // Transform vertex by world coords of SharedMesh.

                verts[vi] = v3f.x;
                verts[vi+1] = v3f.y;
                verts[vi+2] = v3f.z;
            }
            newVertexBuf.put(verts, 0, length);

            float[] norm = normals.get(i);
            for(int ni=0; ni<verts.length; ni+=3) {
                v3f.set(norm[ni], norm[ni+1], norm[ni+2]);
                trans.multNormal(v3f);
                v3f.normalizeLocal();
                norm[ni] = v3f.x;
                norm[ni+1] = v3f.y;
                norm[ni+2] = v3f.z;
            }
            newNormalsBuf.put(norm, 0, length);

            int[] newInd = indicies.get(i);
            for(int ii=0; ii<newInd.length; ii++) {
                newInd[ii] += offset;
            }
            newIndexBuf.put(newInd, 0, newInd.length);

            if (hasColor) {
                float[] clrs = colors.get(i);
                newColorBuf.put(clrs, 0, length);
            }

//            for(int unit=0; unit<texUnitCount; unit++) {
//                ArrayList<float[]> tcList = texCoords.get(unit);
//                for(int j=0; j<tcList.size(); j++) {
//                    float[] tc = tcList.get(j);
//                    newTexCoords.get(j).put(tc, 0, tc.length);
//                }
//            }

            if (hasTexCoords) {
                float[] tcf = texCoords.get(i);
                newTexCoordsBuf.put(tcf, 0, tcf.length);
            }

            offset+=verts.length/3;
        }

        TriMesh newMesh = new TriMesh();
        newMesh.setMode(firstMesh.getMode());
        newMesh.setNormalsMode(firstMesh.getNormalsMode());
        newMesh.setDefaultColor(firstMesh.getDefaultColor());
        newMesh.setIsCollidable(firstMesh.isCollidable());
        newMesh.setCastsShadows(firstMesh.isCastsShadows());
        newMesh.setTextureCombineMode(firstMesh.getTextureCombineMode());
        newMesh.setModelBound(firstMesh.getModelBound());       
        newMesh.setRenderQueueMode(firstMesh.getRenderQueueMode());
        newMesh.setGlowColor(firstMesh.getGlowColor());
        newMesh.setGlowEnabled(firstMesh.isGlowEnabled());
        newMesh.setGlowScale(firstMesh.getGlowScale());
        newMesh.setName("combinedMesh_"+firstMesh.getName());
        newMesh.setLightCombineMode(firstMesh.getLightCombineMode());

        newMesh.setVertexBuffer(newVertexBuf);
        newMesh.setNormalBuffer(newNormalsBuf);
        newMesh.setIndexBuffer(newIndexBuf);

        if (hasColor)
            newMesh.setColorBuffer(newColorBuf);

        if (hasTexCoords) {
            TexCoords tc = new TexCoords(newTexCoordsBuf, firstMeshT.getTextureCoords(0).perVert);
            newMesh.setTextureCoords(tc, 0);
//        for(int j=0; j<newTexCoords.size(); j++) {
//            TexCoords tc = new TexCoords(newTexCoords.get(j));
//            newMesh.setTextureCoords(tc, j);
//        }
        }

        stateSet.applyStates(newMesh);

        newMesh.updateModelBound();

        root.attachChild(newMesh);
    }

    class RenderStateSet {
        private HashSet<RenderState> stateSet = new HashSet();

        public void addRenderState(RenderState state) {
            if (state==null)
                return;
            stateSet.add(state);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RenderStateSet))
                return false;

            return ((RenderStateSet)o).stateSet.equals(stateSet);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.stateSet != null ? this.stateSet.hashCode() : 0);
            return hash;
        }

        /**
         * Add all the states to the spatial
         * @param s
         */
        public void applyStates(Spatial s) {
            for(RenderState state : stateSet) {
                s.setRenderState(state);
            }
        }
    }

    class GraphMetrics {
        int spatialCount=0;     // Number of nodes in total
        int geometryCount=0;    // Number of geometry nodes
        int sharedMeshCount=0;

        public String getReport() {
            StringBuffer buf = new StringBuffer();
            buf.append("Total Node "+spatialCount+" Shared Meshes "+sharedMeshCount+" Other Geometry "+geometryCount);
            return buf.toString();
        }

        public void reset() {
            spatialCount = 0;
            geometryCount = 0;
            sharedMeshCount = 0;
        }
    }
}
