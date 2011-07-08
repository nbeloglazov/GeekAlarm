# Geek Alarm

Geek alarm is alarm application for android. It works as usual alarm application, but to turn it off you need to solve several math or computer tasks (calculate matrix determinant, calulcate integral, convert number from binary, etc).

Example of tasks can be found [here](http://7133305a.dotcloud.com/).

## Categories

Task represents basic operations and definitions in different fields of math and computer science.  
Tasks, with '+' sign already implemented.

* Linear algebra
  * \+ [Determinant](http://en.wikipedia.org/wiki/Determinant).
  * \+ [Inverse matrix](http://en.wikipedia.org/wiki/Inverse_matrix).
  * \+ [Matrix multiplication](http://en.wikipedia.org/wiki/Matrix_multiplication).
  * [Eigenvalues and eigenvectors](http://en.wikipedia.org/wiki/Eigenvalue,_eigenvector_and_eigenspace).
* Mathematical analysis
  * \+ Definite polynomial integral.
  * Definite random integral (trigonometric functions).
  * Indefinite integral.
  * Derivative.
  * Derivative at a given point.
* Computer science (or better name?)
  * \+ [Base conversation](http://en.wikipedia.org/wiki/Base_conversion#Base_conversion) (e.g. from binary to decimal).
  * Something from graph theory (finding shortest path, etc). Must be clarified.
* Number theory
  * [Euler's totient function](http://en.wikipedia.org/wiki/Euler%27s_totient_function).
  * Select prime number.
  * [Greatest common divisors](http://en.wikipedia.org/wiki/Greatest_common_divisor).
  * [Congruence relation](http://en.wikipedia.org/wiki/Modular_arithmetic).
  
## Architecture

To display all this tasks to user, we need to generate images from LaTeX or MathML notation. I haven't found any lightweight library for java to do it. So we can't generate tasks in android application. These tasks will be generated on server and android client will get them using http requests. Server usage requires internet access for client (not good), but it allows to add new tasks very easy (good). Server will be implemented in Clojure.

Project consists of 2 parts:

 1. Android client

 2. Clojure server

## License

Copyright (C) 2011 Nikita Beloglazov

