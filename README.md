# jutils

A miscellanea of small and handy general purpose utilities.
They should be fairly stable and bug-free, despite the fact there aren't many unit tests
(the code is rather simple).  

See [this post](http://www.marcobrandizi.info/mysite/jutils) for a description of what the library offers.  

See [Javadocs](https://marco-brandizi.github.io/jutils/) for details (or, of course, sources!).  

**WARNING**: since version 10.0, jutils requires Java >= 11. Namely, the Maven project will build on 1.8 for a while, 
and only if you change the Maven `<source>` and `<target>` options for the compiler plug-in, however that's not officially 
supported and is likely to stop working in future (ie, when we start using recent Java features).  

**WARNING**: since 6.0, jutils has been split into multiple Maven modules. What it used to be the root of the whole 
project is now the jutils/ module/folder, some more specific components were moved out of there and sent to 
their own modules (include them in place of jutils, if you need their functionality, the core dependency will be 
pulled up automatically).  
