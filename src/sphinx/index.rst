=========================
Casbah User Documentation
=========================

Welcome to the Casbah Documentation.  Casbah is a Scala toolkit for MongoDB ---
We use the term "toolkit" rather than "driver", as Casbah is a layer on top of
the official `mongo-java-driver <http://github.com/mongodb/mongo-java-driver>`_
for better integration with Scala.  This is as opposed to a native
implementation of the MongoDB wire protocol, which the Java driver does
exceptionally well. Rather than a complete rewrite, Casbah uses implicits, and
`Pimp My Library` code to enhance the existing Java code.

:doc:`tutorial`
  A quick tutorial to get you started using Casbah.

:doc:`guide/index`
  The full guide to Casbah - covering installation, connecting, the query dsl
  , gridfs, and *everything* between.

`ScalaDocs <http://mongodb.github.io/casbah/api/#com.mongodb.casbah.package>`_
  The complete ScalaDocs for Casbah along with SXR cross referenced source.

:doc:`changelog`
  The recent changes to Casbah

:doc:`whats_new`
  An indepth review of new features in MongoDB and Casbah

Help and support
----------------

For help and support using casbah please send emails / questions to the
`Casbah Mailing List <http://groups.google.com/group/mongodb-casbah-users>`_ on
Google Groups.   Also be sure to subscribe to the usergroup to get the latest
news and information about Casbah.

Stackoverflow is also a great resource for getting answers to your casbah
questions - just be sure to tag any questions with
"`casbah <http://stackoverflow.com/questions/tagged/casbah>`_".  Just don't
forget to mark any answered questions as answered!

Contributing
------------

The source is available on `GitHub <http://github.com/mongodb/casbah>`_
and contributions are always encouraged. Contributions can be as simple as
minor tweaks to the documentation, feature improments or updates to the core.

To contribute, fork the project on `GitHub <http://github.com/mongodb/casbah>`_
and send a pull request.

Offline Reading
---------------

Download the docs in either `PDF <CasbahDocumentation.pdf>`_ or
`ePub <CasbahDocumentation.epub>`_ formats fot offline reading.


.. toctree::
    :maxdepth: 1
    :titlesonly:
    :numbered:

    tutorial
    guide/index
    examples
    changelog
    whats_new
    upgrade

Indices and tables
------------------

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
