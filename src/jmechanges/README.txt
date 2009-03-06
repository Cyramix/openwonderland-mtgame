This document describes the changes that we have made to each of the
following files.

BufferUtils.java:
    Added API to serialize and de-serialize a FloatBuffer.

ColladaImporter.java:
    Fixed assignment of texture coordinates
    Add accessor for upAxis field

CollisionTree.java:
CollisionTreeManager.java:
TriMesh.java:
Quad.java:
    New API and processing to invalidate CollisionTree when vertex data changes.

GLSLShaderObjectsState.java:
    Add support for arrays of Matrix4's as a uniform

JOGLImageGraphics.java:
    Acquire and release JOGL lock in appropriate places

JOGLTextureState.java:
    Fix bug in texture size specification

TangentBinormalGenerator.java:
    Added support for assigning tangents and binormals to a SharedMesh.

SavableHashMap.java:
TextureManager.java:
    Added support to read/write the Texture cache HashMap.
    Added support to read the Texture cache HashMap for a URL

TrianglePickData.java:
    Added API to set and get the actual intersection point.

GeometricUpdateListener.java:
Spatial.java:
    Added new API to track geometric state updates

JOGLRenderer.java:
    Added support for tangents and normals in VBO's

VBOInfo.java:
    Added support for tangents and binormals

ProjectedTextureUtil.java:
    Port to JOGL

Obsolete Changes:
    JOGLDisplaySystem.java: Start of multisample support
    Geometry.java: Serialize data
    TexCoords.java: Serialize TexCoords

