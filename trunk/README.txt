This is the MTGame (Multi-threaded game) platform.  This system depends 
upon a number of other systems.  You must have the runtime components of
the other systems to successfully build MTGame.  Those componets are as
follows:

    - A modified version of the Java Monkey Engine 2.0 platform.
      The svn branch with all the nessesary changes for this system
      can be found at:
      http://jmonkeyengine.googlecode.com/svn/branches/2.0.x-wonderland

    - JBullet is a physics and collision system.  While it's use in the
      system is optional, the runtime is needed to successfully build
      MTGame.  It can be found at http://jbullet.advel.cz/

    - JOGL is the official Java bindings for OpenGL.  It can be downloaded
      at jogl.dev.java.net.

    - javolution is a set of fast utilities that the system uses.  It can
      be downloaded from http://javolution.org/

Once these two systems are available, the system should build and run fine.