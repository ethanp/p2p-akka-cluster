package ethanp.firstVersion

import akka.actor.ActorRef
import ethanp.file.{FileInfo, FileToDownload}
import ethanp.firstVersion.Master.NodeID

/**
 * Ethan Petuchowski
 * 6/4/15
 */
case class LoadFile(pathString: String, name: String)
case class TrackerLoc(trackerID: NodeID, trackerPath: ActorRef)
case class PeerLoc(peerID: NodeID, peerPath: ActorRef)
case class ListTracker(trackerID: NodeID)
case class Swarm(var seeders: Map[NodeID, ActorRef], var leechers: Map[NodeID, ActorRef])
case class TrackerKnowledge(knowledge: List[FileToDownload])
case class InformTrackerIHave(clientID: NodeID, fileInfo: FileInfo)
case class TrackerSideError(errorString: String)
case class PeerSideError(errorString: String)
case class SuccessfullyAdded(filename: String)
case class DownloadFile(trackerID: NodeID, filename: String)
case class Piece(arr: Array[Byte], pieceIdx: Int)
sealed trait ChunkStatus extends Serializable
case class ChunkComplete(chunkIdx: Int) extends ChunkStatus
case class ChunkDLFailed(chunkIdx: Int, peerLoc: PeerLoc) extends ChunkStatus
case class ChunkRequest(fileInfo: FileInfo, chunkIdx: Int)
case object ChunkSuccess
case class DownloadSpeed(numBytes: Int)
case class DownloadSuccess(filename: String)

object PeerLoc {
    def apply(peerPair: (NodeID, ActorRef)): PeerLoc = PeerLoc(peerPair._1, peerPair._2)
}