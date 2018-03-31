package compile;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_Interface extends Remote {
    void backup(String file_name, int replication_deg, int port_number) throws RemoteException;
    void restore(String file_name, int port_numbe) throws RemoteException;
    void delete(String file_name, int port_number) throws RemoteException;
    void reclaim(String value, int port_number) throws RemoteException;
    void state() throws RemoteException;
    void receiver(int port_number) throws RemoteException;

}
