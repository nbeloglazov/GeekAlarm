# Geek Alarm

Geek alarm is alarm application for android. It works as usual alarm application, but to turn it off you need to solve several math or computer tasks (calculate matrix determinant, calulcate integral, convert number from binary, etc).

## Architecture

To display all this tasks to user, we need to generate images from LaTeX or MathML notation. I haven't found any lightweight library for java to do it. So we can't generate tasks in android application. These tasks will be generated on server and android client will get them using http requests. Server usage requires internet access for client (not good), but it allows to add new tasks very easy (good). Server will be implemented in Clojure.

Project consists of 2 parts:

 1. Android client

 2. Clojure server

## License

Copyright (C) 2011 Nikita Beloglazov

