package br.edu.ifce.surakarta.lib

import br.edu.ifce.surakarta.Connection
import br.edu.ifce.surakarta.Player
import java.rmi.Remote
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry

interface ISurakartaPlayer : Remote {

    @Throws(RemoteException::class)
    fun start(host: String, port: Int)

    @Throws(RemoteException::class)
    fun sendMessage(text: String)

    @Throws(RemoteException::class)
    fun moveMouse(positionX: Float, positionY: Float)

    @Throws(RemoteException::class)
    fun changeBoard(board: HashMap<Int, Player>)

    @Throws(RemoteException::class)
    fun selectCell(cell: Int)

    @Throws(RemoteException::class)
    fun changeTurn()

    @Throws(RemoteException::class)
    fun finishGame()

    @Throws(RemoteException::class)
    fun surrender()

}

fun findSurakartaPlayer(connection: Connection) : ISurakartaPlayer {
    return LocateRegistry.getRegistry(connection.host, connection.port).lookup("SurakartaPlayer") as ISurakartaPlayer
}

fun exportSurakartaPlayer(port: Int, player: ISurakartaPlayer) {
    LocateRegistry.createRegistry(port).bind("SurakartaPlayer", player)
}