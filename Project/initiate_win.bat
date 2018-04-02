javac -d C:\Users\jsaraiva\github\SDIS\Project\src *.java
start rmiregistry
start java -classpath C:\Users\jsaraiva\github\SDIS\Project\src -Djava.rmi.server.codebase=file:C:\Users\jsaraiva\github\SDIS\Project\src\ compile.Peer 1923
java -classpath C:\Users\jsaraiva\github\SDIS\Project\src compile.TestApp 1923
