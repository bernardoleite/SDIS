Mudar os comandos em initiate_win.bat se utiliza windows ou
Mudar os comandos em initiate_sol.bat se utiliza Solaris Os
Inicialmente está um exemplo em que a pasta src está localizada em C:\Users\jsaraiva\github\SDIS\Project

Os seguintes são os comandos 

1. Compilar:
  javac -d destDir *.java

2. Iniciar rmiregistry:

  Windows:
    start rmiregistry

  Solaris Os:
    rmiregistry &


3. 
Iniciar um Peer em modo Receiver:

  Windows:
    start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number>

    java -classpath classDir compile.TestApp <port_number>

  Solaris OS:
    java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number> &

    java -classpath classDir compile.TestApp <port_number>

Iniciar um Peer em modo Backup:

  Windows:
    start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number>

    java -classpath classDir compile.TestApp <port_number> BACKUP <file_name> <replication_deg>

  Solaris OS:
    java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number> &

    java -classpath classDir compile.TestApp <port_number> BACKUP <file_name> <replication_deg>


Iniciar um Peer em modo Restore:

  Windows:
    start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number>

    java -classpath classDir compile.TestApp <port_number> RESTORE <file_name>

  Solaris OS:
    java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number> &

    java -classpath classDir compile.TestApp <port_number> RESTORE <file_name>


Iniciar um Peer em modo Delete:

  Windows:
    start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number>

    java -classpath classDir compile.TestApp <port_number> DELETE <file_name>

  Solaris OS:
    java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number> &

    java -classpath classDir compile.TestApp <port_number> DELETE <file_name>


Iniciar um Peer em modo Reclaim:

  Windows:
    start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number>

    java -classpath classDir compile.TestApp <port_number> RECLAIM <disk_space>

  Solaris OS:
    java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number> &

    java -classpath classDir compile.TestApp <port_number> RECLAIM <disk_space>


Iniciar um Peer em modo State:

  Windows:
    start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number>

    java -classpath classDir compile.TestApp <port_number> STATE

  Solaris OS:
    java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ compile.Peer <port_number> &

    java -classpath classDir compile.TestApp <port_number> STATE
