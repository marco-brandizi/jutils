# jutils

A miscellanea of small and handy general purpose utilities.
They should be fairly stable and bug-free, despite the fact there aren't many unit tests
(the code is rather simple).  

See [this post](http://www.marcobrandizi.info/jutils) for a description of what the library offers.  
See [Javadocs](https://marco-brandizi.github.io/jutils) for details (or, of course, sources!).  

**WARNING**: since version 10.0, jutils requires Java >= 11. Namely, the Maven project will build on 1.8 for a while, 
and only if you change the Maven `<source>` and `<target>` options for the compiler plug-in, however that's not officially 
supported and is likely to stop working in future (ie, when we start using recent Java features).  

**WARNING**: starting with version 11.0, jutils is a single Maven module with a number of optional dependencies, 
which **you need to declare as needed** in your own projects. see the [POM](pom.xml) for details.

