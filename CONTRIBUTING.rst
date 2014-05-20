Contributing to Casbah
======================

Casbah has a growing `community
<https://github.com/mongodb/casbah/blob/master/AUTHORS>`_ and
contributions are always encouraged. Contributions can be as simple as
minor tweaks to the documentation. Please read these guidelines before
sending a pull request.

Bugfixes and New Features
-------------------------

Before starting to write code, look for existing `tickets
<https://jira.mongodb.org/browse/CASBAH>`_ or `create one
<https://jira.mongodb.org/browse/CASBAH>`_ for your specific
issue or feature request. That way you avoid working on something
that might not be of interest or that has already been addressed.

Supported Versions
------------------

Casbah currently supports Scala 2.11.X, 2.10.X & 2.9.3

Style Guide
-----------

Casbah aims to follow the
`Scala style conventions <http://docs.scala-lang.org/style/>`_ including 2
space indents and 79 character line limits.

General Guidelines
------------------

- Avoid backward breaking changes if at all possible.
- Write inline documentation for new classes and methods.
- Write tests and make sure they pass (make sure you have a mongod
  running on the default port, then execute ``./sbt test``
  from the cmd line to run the test suite).
- Add yourself to AUTHORS.rst :)

Documentation
-------------

To contribute to the `API documentation <http://mongodb.github.com/casbah/>`_
just make your changes to the inline documentation of the appropriate
`source code <https://github.com/mongodb/casbah>`_ or `rst file
<https://github.com/mongodb/casbah/tree/master/tutorial_src>`_ in a
branch and submit a `pull request <https://help.github.com/articles/using-pull-requests>`_.
You might also use the github `Edit <https://github.com/blog/844-forking-with-the-edit-button>`_
button.

