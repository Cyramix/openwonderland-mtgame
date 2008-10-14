This is the MTGame (Multi-threaded game) platform.  This system depends 
upon a number of other systems.  You must have the runtime components of
the other systems to successfully build MTGame.  Those componets are as
follows:

    - A modified version of the Java Monkey Engine 2.0 platform.
      See http://www.jmonkeyengine.com/ for details on getting the
      default build.  This build must be augmented with the new files
      and changes found in src/jmechanges

    - JBullet is a physics and collision system.  While it's use in the
      system is optional, the runtime is needed to successfully build
      MTGame.  It can be found at http://jbullet.advel.cz/

Once these two systems are available, the system should build and run fine.