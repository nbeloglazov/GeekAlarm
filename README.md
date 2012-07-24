# Geek Alarm

Geek alarm is alarm application for android. It works as usual alarm application, but to turn it off you need to solve several math or computer tasks (calculate matrix determinant, calulcate integral, convert number from binary, etc).

Tasks examples can be found here [http://geekbeta-nbeloglazov.dotcloud.com](http://geekbeta-nbeloglazov.dotcloud.com).

App in android market: [GeekAlarm](http://market.android.com/details?id=com.geek_alarm.android)

## Tasks

Task represents basic operations and definitions in different fields of math and computer science.

Implemented:

* [Determinant](http://en.wikipedia.org/wiki/Determinant).
* [Inverse matrix](http://en.wikipedia.org/wiki/Inverse_matrix).
* [Matrix multiplication](http://en.wikipedia.org/wiki/Matrix_multiplication).
* Definite polynomial integral.
* Derivative.
* [Base conversation](http://en.wikipedia.org/wiki/Base_conversion#Base_conversion) (e.g. from binary to decimal).
* [Regex](http://en.wikipedia.org/wiki/Regex).
* Select [prime number](http://en.wikipedia.org/wiki/Prime_number).
* [Greatest common divisors](http://en.wikipedia.org/wiki/Greatest_common_divisor).
* [Congruence relation](http://en.wikipedia.org/wiki/Modular_arithmetic).
* [Bitwise operations](http://en.wikipedia.org/wiki/Bitwise_operation).
* Select lucky ticket.
* [Maximum matching][matching].
* Unique substrings.

Plans:

* [Eigenvalues and eigenvectors](http://en.wikipedia.org/wiki/Eigenvalue,_eigenvector_and_eigenspace).
* Definite random integral (trigonometric functions).
* Indefinite integral.
* Derivative at a given point.
* Something from graph theory (finding shortest path, etc). Must be clarified.
* [Euler's totient function](http://en.wikipedia.org/wiki/Euler%27s_totient_function).

## Architecture

To display all this tasks to user, we need to generate images from LaTeX or MathML notation. I haven't found any lightweight library for java to do it. So we can't generate tasks in android application. These tasks will be generated on server and android client will get them using http requests. Server usage requires internet access for client (not good), but it allows to add new tasks very easy (good). Server is implemented in Clojure.

Project consists of 2 parts:

 1. Android client

 2. Clojure server

## License

Copyright (C) 2011-2012 Nikita Beloglazov

[matching]: http://en.wikipedia.org/wiki/Matching_(graph_theory)